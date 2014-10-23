package sinetja;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class Response {
  protected static final ByteBuf INTERNAL_SERVER_ERROR = Unpooled.copiedBuffer("Server error".getBytes());

  protected FullHttpResponse response;

  public Response() {
    // Create default response
    response = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      routed.notFound404() ? HttpResponseStatus.NOT_FOUND : HttpResponseStatus.OK
    );
  }

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
