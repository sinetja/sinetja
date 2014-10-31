package sinetja;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.router.Routed;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;

public class Response implements FullHttpResponse {
  // Creating ObjectMapper is time consuming but once created it's thread-safe and fast
  public static final ObjectMapper jsonObjectMapper = new ObjectMapper();

  private final Server  server;
  private final Channel channel;
  private final Routed  routed;

  private final Request request;
  private final FullHttpResponse response;

  private boolean nonChunkedResponseOrFirstChunkSent = false;
  private boolean doneResponding                     = false;

  //----------------------------------------------------------------------------

  public Response(Server server, Channel channel, Routed routed, Request request) {
    this.server  = server;
    this.channel = channel;
    this.routed  = routed;
    this.request = request;

    response = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      routed.notFound() ? HttpResponseStatus.NOT_FOUND : HttpResponseStatus.OK
    );
  }

  public boolean doneResponding() {
    return doneResponding;
  }

  //----------------------------------------------------------------------------

  private void throwDoubleResponseError() throws IllegalStateException {
    throw new IllegalStateException("Double response error. See stack trace to know where to fix the error.");
  }

  private void throwDoubleResponseError(Object text) throws IllegalStateException {
     throw new IllegalStateException("Double response error. See stack trace to know where to fix the error. You're trying to respond: " + text + "\n");
  }

  /** If Content-Type header is not set, it is set to "application/octet-stream" */
  private void respondHeadersOnlyForFirstChunk() throws Exception {
    // doneResponding is set to true by respondLastChunk
    if (doneResponding) throwDoubleResponseError();

    if (nonChunkedResponseOrFirstChunkSent) return;

    if (!response.headers().contains(CONTENT_TYPE))
      HttpHeaders.setHeader(response, CONTENT_TYPE, "application/octet-stream");

    // There should be no CONTENT_LENGTH header
    HttpHeaders.removeHeader(response, CONTENT_LENGTH);

    setNoClientCache();

    // Env2Response will respond only only headers
    respond();
  }

  //----------------------------------------------------------------------------

  public void setClientCacheAggressively() {
    NotModified.setClientCacheAggressively(response.headers());
  }

  public void setNoClientCache() {
    NotModified.setNoClientCache(response.headers());
  }

  //----------------------------------------------------------------------------

  public ChannelFuture respond() throws Exception {
    // For chunked response, this method is only called to respond the 1st chunk,
    // next chunks are responded directly by respondXXX

    if (nonChunkedResponseOrFirstChunkSent) throwDoubleResponseError();

    // Run after filter
    final Action after = (Action) Routed.instanceFromTarget(server.after());
    if (after != null) after.run(request, this);

    ChannelFuture future = channel.writeAndFlush(response);

    // Do not handle keep alive:
    // * If XSendFile or XSendResource is used, because they will handle keep alive in their own way
    // * If the response is chunked, because respondLastChunk will be handle keep alive
    if (//!XSendFile.isHeaderSet(response) &&
        //!XSendResource.isHeaderSet(response) &&
        !HttpHeaders.isTransferEncodingChunked(response)) {
      NoRealPipelining.if_keepAliveRequest_then_resumeReading_else_closeOnComplete(routed.request(), channel, future);
    }

    nonChunkedResponseOrFirstChunkSent = true;
    if (!HttpHeaders.isTransferEncodingChunked(response)) {
      doneResponding = true;
    }

    return future;
  }

  //----------------------------------------------------------------------------

  public ChannelFuture respondText(Object text) throws Exception {
    return respondText(text, null);
  }

  /**
   * @param fallbackContentType Only used if Content-Type header has not been set.
   * If not given and Content-Type header is not set, it is set to
   * "application/xml" if text param is Node or NodeSeq, otherwise it is
   * set to "text/plain".
   */
  public ChannelFuture respondText(Object text, String fallbackContentType) throws Exception {
    if (doneResponding) throwDoubleResponseError(text);

    final String respondedText = text.toString();

    if (!nonChunkedResponseOrFirstChunkSent && !response.headers().contains(CONTENT_TYPE)) {
      // Set content type
      if (fallbackContentType != null) {
        // https://developers.google.com/speed/docs/best-practices/rendering#SpecifyCharsetEarly
        final String withCharset =
          fallbackContentType.toLowerCase().contains("charset") ?
          fallbackContentType :
          fallbackContentType + "; charset=" + server.charset();

        response.headers().set(CONTENT_TYPE, withCharset);
      } else {
        response.headers().set(CONTENT_TYPE, "text/plain; charset=" + server.charset());
      }
    }

    ByteBuf buf = Unpooled.copiedBuffer(respondedText, server.charset());
    if (HttpHeaders.isTransferEncodingChunked(response)) {
      respondHeadersOnlyForFirstChunk();
      return channel.writeAndFlush(new DefaultHttpContent(buf));
    } else {
      // Pitfall: Content-Length is number of bytes, not characters
      response.headers().set(CONTENT_LENGTH, buf.readableBytes());
      response.content().writeBytes(buf);
      return respond();
    }
  }

  /** Content-Type header is set to "application/xml". */
  public ChannelFuture respondXml(Object any) throws Exception {
    return respondText(any, "application/xml");
  }

  /** Content-Type header is set to "text/html". */
  public ChannelFuture respondHtml(Object any) throws Exception {
    return respondText(any, "text/html");
  }

  /** Content-Type header is set to "application/javascript". */
  public ChannelFuture respondJs(Object any) throws Exception {
    return respondText(any, "application/javascript");
  }

  /** Content-Type header is set to "application/json". */
  public ChannelFuture respondJsonText(Object any) throws Exception {
    return respondText(any, "application/json");
  }

  /**
   * Converts the given Java object to JSON object using Jackson ObjectMapper,
   * and responds it.
   * If you just want to respond a text with "application/json" as content type,
   * use respondJsonText(text).
   *
   * Content-Type header is set to "application/json".
   * "text/json" would make the browser download instead of displaying the content.
   * It makes debugging a pain.
   */
  public ChannelFuture respondJson(Object ref) throws Exception {
    final String json = jsonObjectMapper.writeValueAsString(ref);
    return respondText(json, "application/json");
  }

  /**
   * Converts the given Java object to JSON object using Jackson ObjectMapper,
   * wraps it with the given JavaScript function name, and responds.
   * If you already have a JSON text, thus no conversion is needed, use respondJsonPText.
   *
   * Content-Type header is set to "application/javascript".
   */
  public ChannelFuture respondJsonP(Object ref, String function) throws Exception {
    final String json = jsonObjectMapper.writeValueAsString(ref);
    final String text = function + "(" + json + ");\r\n";
    return respondJs(text);
  }

  /**
   * Wraps the text with the given JavaScript function name, and responds.
   *
   * Content-Type header is set to "application/javascript".
   */
  public ChannelFuture respondJsonPText(Object text, String function) throws Exception {
    return respondJs(function + "(" + text + ");\r\n");
  }

  //----------------------------------------------------------------------------

  /** If Content-Type header is not set, it is set to "application/octet-stream". */
  public ChannelFuture respondBinary(byte[] bytes) throws Exception {
    return respondBinary(Unpooled.wrappedBuffer(bytes));
  }

  /**
   * If Content-Type header is not set, it is set to "application/octet-stream".
   *
   * @param byteBuf Will be released
   */
  public ChannelFuture respondBinary(ByteBuf byteBuf) throws Exception {
    if (HttpHeaders.isTransferEncodingChunked(response)) {
      respondHeadersOnlyForFirstChunk();
      return channel.writeAndFlush(new DefaultHttpContent(byteBuf));
    } else {
      if (!response.headers().contains(CONTENT_TYPE))
          response.headers().set(CONTENT_TYPE, "application/octet-stream");

      response.headers().set(CONTENT_LENGTH, byteBuf.readableBytes());
      response.content().writeBytes(byteBuf);
      return respond();
    }
  }

  //----------------------------------------------------------------------------

  public String renderEventSource(Object data) {
    return renderEventSource(data, "message");
  }

  public String renderEventSource(Object data, String event) {
    final StringBuilder builder = new StringBuilder();

    if (event != "message") {
      builder.append("event: ");
      builder.append(event);
      builder.append("\n");
    }

    final String[] lines = data.toString().split("\n");
    final int      n     = lines.length;
    for (int i = 0; i < n; i++) {
      if (i < n - 1) {
        builder.append("data: ");
        builder.append(lines[i]);
        builder.append("\n");
      } else {
        builder.append("data: ");
        builder.append(lines[i]);
      }
    }

    builder.append("\r\n\r\n");
    return builder.toString();
  }

  /**
   * To respond event source, call this method as many time as you want.
   * Event Source response is a special kind of chunked response, data must be UTF-8.
   * See:
   * - http://sockjs.github.com/sockjs-protocol/sockjs-protocol-0.3.3.html#section-94
   * - http://dev.w3.org/html5/eventsource/
   *
   * No need to call setChunked() before calling this method.
   */
  public ChannelFuture respondEventSource(Object data) throws Exception {
      return respondEventSource(data, "message");
  }

  /**
   * To respond event source, call this method as many time as you want.
   * Event Source response is a special kind of chunked response, data must be UTF-8.
   * See:
   * - http://sockjs.github.com/sockjs-protocol/sockjs-protocol-0.3.3.html#section-94
   * - http://dev.w3.org/html5/eventsource/
   *
   * No need to call setChunked() before calling this method.
   */
  public ChannelFuture respondEventSource(Object data, String event) throws Exception {
    if (!nonChunkedResponseOrFirstChunkSent) {
      HttpHeaders.setTransferEncodingChunked(response);
      HttpHeaders.setHeader(response, CONTENT_TYPE, "text/event-stream; charset=UTF-8");
      return respondText("\r\n");  // Send a new line prelude, due to a bug in Opera
    }
    return respondText(renderEventSource(data, event));
  }

  //----------------------------------------------------------------------------
  // Implement FullHttpResponse

  @Override
  public HttpResponseStatus getStatus() {
    return response.getStatus();
  }

  @Override
  public HttpVersion getProtocolVersion() {
    return response.getProtocolVersion();
  }

  @Override
  public HttpHeaders headers() {
    return response.headers();
  }

  @Override
  public DecoderResult getDecoderResult() {
    return response.getDecoderResult();
  }

  @Override
  public void setDecoderResult(DecoderResult arg0) {
    response.setDecoderResult(arg0);
  }

  @Override
  public HttpHeaders trailingHeaders() {
    return response.trailingHeaders();
  }

  @Override
  public HttpContent duplicate() {
    return response.duplicate();
  }

  @Override
  public ByteBuf content() {
    return response.content();
  }

  @Override
  public int refCnt() {
    return response.refCnt();
  }

  @Override
  public boolean release() {
    return response.release();
  }

  @Override
  public boolean release(int arg0) {
    return response.release(arg0);
  }

  @Override
  public FullHttpResponse copy() {
    return response.copy();
  }

  @Override
  public FullHttpResponse retain() {
    return response.retain();
  }

  @Override
  public FullHttpResponse retain(int arg0) {
    return response.retain(arg0);
  }

  @Override
  public FullHttpResponse setProtocolVersion(HttpVersion arg0) {
    return response.setProtocolVersion(arg0);
  }

  @Override
  public FullHttpResponse setStatus(HttpResponseStatus arg0) {
    return response.setStatus(arg0);
  }

  @Override
  public String toString() {
    return response.toString();
  }
}
