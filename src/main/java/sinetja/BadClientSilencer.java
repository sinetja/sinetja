package sinetja;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * This handler should be put at the last position of the inbound pipeline to
 * catch all exception caused by bad client (closed connection, malformed request etc.).
 */
@Sharable
public class BadClientSilencer extends SimpleChannelInboundHandler<Object> {
  @Override
  public void channelRead0(ChannelHandlerContext ctx, Object msg) {
    // This handler is the last inbound handler.
    // This means msg has not been handled by any previous handler.
    ctx.channel().close();
    Log.trace("Unknown msg: {}" + msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
    ctx.channel().close();

    if (e instanceof java.io.IOException                            ||  // Connection reset by peer, Broken pipe
        e instanceof java.nio.channels.ClosedChannelException       ||
        e instanceof io.netty.handler.codec.DecoderException        ||
        e instanceof io.netty.handler.codec.CorruptedFrameException ||  // Bad WebSocket frame
        e instanceof java.lang.IllegalArgumentException             ||  // Use https://... URL to connect to HTTP server
        e instanceof javax.net.ssl.SSLException                     ||  // Use http://... URL to connect to HTTPS server
        e instanceof io.netty.handler.ssl.NotSslRecordException)
      Log.trace("Caught exception", e);  // Maybe client is bad
    else
      Log.warn("Caught exception", e);   // Maybe server is bad
  }
}
