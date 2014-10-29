package sinetja;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.router.DualAbstractHandler;
import io.netty.handler.codec.http.router.Routed;

public class RouterHandler extends DualAbstractHandler<Action, Server> {
  private final Server server;

  public RouterHandler(Server server) {
    super(server);
    this.server = server;
  }

  @Override
  protected void routed(final ChannelHandlerContext ctx, final Routed routed) throws Exception {
    // Log time taken to process the request
    final long beginNano = System.nanoTime();

    final Channel  channel  = ctx.channel();
    final Request  request  = new Request (server, channel, routed);
    final Response response = new Response(server, channel, routed, request);

    // Release request and response when the connection is closed, just in case
    channel.closeFuture().addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture arg0) throws Exception {
        if (request .refCnt() > 0) request .release(request .refCnt());
        if (response.refCnt() > 0) response.release(response.refCnt());
      }
    });

    try {
      final Action before = (Action) Routed.instanceFromTarget(server.before());
      if (before != null) before.run(request, response);

      if (!response.doneResponding()) {
        // After filter is run at Response#respond
        final Action action = (Action) routed.instanceFromTarget();
        action.run(request, response);

        // If this is async, pause reading and resume at Response#respond
        if (!response.doneResponding()) NoRealPipelining.pauseReading(channel);
      }
    } catch (Exception e1) {
      ErrorHandler errorHandler = (ErrorHandler) Routed.instanceFromTarget(server.error());
      if (errorHandler == null) {
        handleError(request, response, e1);
      } else {
        try {
          response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
          errorHandler.run(request, response, e1);
        } catch (Exception e2) {
          handleError(request, response, e2);
        }
      }
    }

    // Access log; the action can be async
    final long   endNano               = System.nanoTime();
    final long   dt                    = endNano - beginNano;
    final Object asyncOrResponseStatus = response.doneResponding() ? response.getStatus() : "async";
    if (dt >= 1000000L) {
      Log.info("[{}] {} {} - {} {} [ms]", request.remoteIp(), request.getMethod(), request.getUri(), asyncOrResponseStatus, dt / 1000000L);
    } else if (dt >= 1000) {
      Log.info("[{}] {} {} - {} {} [us]", request.remoteIp(), request.getMethod(), request.getUri(), asyncOrResponseStatus, dt / 1000);
    } else {
      Log.info("[{}] {} {} - {} {} [ns]", request.remoteIp(), request.getMethod(), request.getUri(), asyncOrResponseStatus, dt);
    }
  }

  protected void handleError(Request request, Response response, Exception e) throws Exception {
    if (e instanceof MissingParam) {
      MissingParam mp = (MissingParam) e;
      response.setStatus(HttpResponseStatus.BAD_REQUEST);
      response.respondText("Missing param: " + mp.param());
    } else {
      Log.error("Server error: {}\nWhen processing request: {}", e, request);
      response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
      response.respondText("Internal Server Error");
    }
  }
}
