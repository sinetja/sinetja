package sinetja;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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

public class ActionRunner extends SimpleChannelInboundHandler<Routed> {
  protected final Action action;

  public ActionRunner(Action action) {
    this.action = action;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Routed routed) {
    // Log time taken to process the request
    long beginNano = System.nanoTime();

    Channel         channel = ctx.channel();
    FullHttpRequest request = (FullHttpRequest) routed.request();

    // Release request and response when the connection is closed, just in case
    channel.closeFuture().addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture arg0) throws Exception {
        if (request .refCnt() > 0) request.release (request .refCnt());
        if (response.refCnt() > 0) response.release(response.refCnt());
      }
    });

    try {
      // Call execute after all the preparation
      action.run(request, response);
    } catch (MissingParam e) {
      response.setStatus(HttpResponseStatus.BAD_REQUEST);
      response.respondMissingParam(e);
    } catch (Exception e) {
      Log.error("Server error: {}\nWhen processing request: {}", e, request);
      response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
      response.respondServerError(e);
    }

    // Access log; the action can be async
    long endNano = System.nanoTime();
    long dt      = endNano - beginNano;
    if (dt >= 1000000L) {
      Log.info("[{}] {} {} - {} [ms]", request.remoteIp(), request.getMethod(), request.getUri(), dt / 1000000L);
    } else if (dt >= 1000) {
      Log.info("[{}] {} {} - {} [us]", request.remoteIp(), request.getMethod(), request.getUri(), dt / 1000);
    } else {
      Log.info("[{}] {} {} - {} [ns]", request.remoteIp(), request.getMethod(), request.getUri(), dt);
    }
  }
}
