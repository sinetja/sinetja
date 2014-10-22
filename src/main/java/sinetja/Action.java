package sinetja;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Method <code>execute</code> should be implemented to handle request.
 *
 * If <code>execute</code> throws <code>MissingParam</code> exception when
 * calling method <code>param</code>, <code>respondMissingParam</code> will
 * be called, which by default respond "400 Bad Request".
 *
 * If <code>execute</code> throws other exception, <code>respondServerError</code>
 * will be called, which by default respond "500 Internal Server Error".
 */
public abstract class Action extends SimpleChannelInboundHandler<Routed> {
  protected static final ByteBuf INTERNAL_SERVER_ERROR = Unpooled.copiedBuffer("Server error".getBytes());

  protected final Logger log = LoggerFactory.getLogger(Log.class);

  /** The default charset is UTF-8. Override if you want to use other charset. */
  protected Charset charset = CharsetUtil.UTF_8;

  protected Channel channel;

  protected Routed routed;

  /**
   * Will be released after method <code>execute</code> is run. If you want to
   * keep it, call <code>request.retain()</code>.
   */
  protected FullHttpRequest request;

  protected FullHttpResponse response;

  /**
   * Set if the request content type is
   * <code>application/x-www-form-urlencoded</code>.
   */
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
      response.setStatus(HttpResponseStatus.BAD_REQUEST);
      respondMissingParam(e);
    } catch (Exception e) {
      Log.error("Server error: {}", e);
      response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
      respondServerError(e);
    }
  }

  //----------------------------------------------------------------------------

  /**
   * Order: path, body, query
   *
   * When there's no param, this method will throw exception <code>MissingParam</code>.
   * If you don't handle this exception, response "400 Bad Request" will be automatically
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
   * Order: path, body, query
   *
   * When there's no param, this method will return null. See also method <code>param</code>.
   */
  protected String paramo(String name) {
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
    respondText("Missing param: " + e.param());
  }

  protected void respondServerError(Exception e) {
    respondText(INTERNAL_SERVER_ERROR);
  }

  //----------------------------------------------------------------------------

  protected ChannelFuture respondText(Object text) {
    byte[]        bytes = text.toString().getBytes(charset);
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
