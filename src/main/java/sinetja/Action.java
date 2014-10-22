package sinetja;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.Routed;
import io.netty.util.CharsetUtil;

public abstract class Action extends SimpleChannelInboundHandler<Routed> {
  protected static final ByteBuf INTERNAL_SERVER_ERROR = Unpooled.copiedBuffer("Server error".getBytes());

  /** The default charset is UTF-8. Override if you want to use other charset. */
  protected Charset charset = CharsetUtil.UTF_8;

  protected Channel channel;

  protected Routed routed;

  /**
   * Will be released after method "execute" is run. If you want to keep it,
   * call "request.retain()".
   */
  protected FullHttpRequest request;

  protected FullHttpResponse response;

  /** Set if the request content type is "application/x-www-form-urlencoded". */
  protected Map<String, List<String>> bodyParams;

  //----------------------------------------------------------------------------

  protected abstract void execute() throws Exception;

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Routed routed) {
    try {
      this.routed = routed;
      channel     = ctx.channel();
      request     = (FullHttpRequest) routed.request();

      Log.info("{} {}", request.getMethod(), request.getUri());

      response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

      // Release request and response when the connection is closed, just in case
      channel.closeFuture().addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture arg0) throws Exception {
          if (request .refCnt() > 0) request.release (request .refCnt());
          if (response.refCnt() > 0) response.release(response.refCnt());
        }
      });

      String contentTye = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
      if (HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED.equals(contentTye)) {
        String             content = request.content().toString(charset);
        QueryStringDecoder qsd     = new QueryStringDecoder("?" + content);
        bodyParams                 = qsd.parameters();
      }

      execute();
    } catch (MissingParam e) {
      respondMissingParam(e);
    } catch (Exception e) {
      respondServerError(e);
    }
  }

  //----------------------------------------------------------------------------

  /**
   * Order: path -> body -> query
   *
   * When there's no param, this method will throw MissingParam. If you don't
   * handle this exception, response 400 Bad Request will be automatically
   * responded by method respondMissingParam (you can override it if you want).
   * If you want "null" instead, please use method "paramo".
   */
  protected String param(String name) throws MissingParam {
    String ret = routed.pathParams().get(name);
    if (ret != null) return ret;

    if (bodyParams != null && bodyParams.containsKey(name)) return bodyParams.get(name).get(0);

    ret = routed.queryParam(name);
    if (ret != null) return ret;

    throw new MissingParam(name);
  }

  /**
   * Order: path -> body -> query
   *
   * When there's no param, this method will return null. See also method "param".
   */
  protected String paramo(String name) throws MissingParam {
    String ret = routed.pathParams().get(name);
    if (ret != null) return ret;

    if (bodyParams != null && bodyParams.containsKey(name)) return bodyParams.get(name).get(0);

    ret = routed.queryParam(name);
    if (ret != null) return ret;

    return null;
  }

  protected List<String> params(String name) {
    List<String> ret = routed.params(name);
    if (bodyParams.containsKey(name)) ret.addAll(bodyParams.get(name));
    return ret;
  }

  //----------------------------------------------------------------------------

  protected void respondMissingParam(MissingParam e) {
    response.setStatus(HttpResponseStatus.BAD_REQUEST);
    respondText("Missing param: " + e.param());
  }

  protected void respondServerError(Exception e) {
    Log.error("Server error: {}", e);
    response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    respondText(INTERNAL_SERVER_ERROR);
  }

  //----------------------------------------------------------------------------

  protected ChannelFuture respondText(String text) {
    byte[]        bytes = text.getBytes(charset);
    ByteBuf       buf   = Unpooled.copiedBuffer(bytes);
    ChannelFuture ret   = respondText(buf);
    buf.release();
    return ret;
  }

  protected ChannelFuture respondText(ByteBuf buf) {
    HttpHeaders headers = response.headers();
    if (headers.contains(HttpHeaders.Names.CONTENT_TYPE)) headers.set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
    response.content().writeBytes(buf);
    headers.set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
    return Util.keepAliveWriteAndFlush(channel, request, response);
  }
}
