package sinetja;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.router.KeepAliveWrite;
import io.netty.handler.codec.http.router.Routed;

public class Response implements FullHttpResponse {
  protected static final ByteBuf INTERNAL_SERVER_ERROR = Unpooled.copiedBuffer("Internal Server Error".getBytes());

  private final Server  server;
  private final Channel channel;
  private final Routed  routed;

  private final FullHttpResponse response;

  public Response(Server server, Channel channel, Routed routed) {
    this.server  = server;
    this.channel = channel;
    this.routed  = routed;

    response = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      routed.notFound() ? HttpResponseStatus.NOT_FOUND : HttpResponseStatus.OK
    );
  }

  public void respondMissingParam(MissingParam e) {
    respondText("Missing param: " + e.param());
  }

  public void respondServerError(Exception e) {
    respondText(INTERNAL_SERVER_ERROR);
  }

  //----------------------------------------------------------------------------

  public ChannelFuture respondText(Object text) {
    byte[]        bytes = text.toString().getBytes(server.charset());
    ByteBuf       buf   = Unpooled.copiedBuffer(bytes);
    ChannelFuture ret   = respondText(buf);
    buf.release();
    return ret;
  }

  public ChannelFuture respondText(ByteBuf buf) {
    HttpHeaders headers = response.headers();
    if (!headers.contains(HttpHeaders.Names.CONTENT_TYPE)) headers.set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
    response.content().writeBytes(buf);
    headers.set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
    return KeepAliveWrite.flush(channel, routed.request(), response);
  }

  public ChannelFuture respondHtml(Object text) {
    response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html");
    return respondText(text);
  }

  public ChannelFuture respondHtml(ByteBuf buf) {
    response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html");
    return respondText(buf);
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
