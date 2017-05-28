package sinetja;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.router.RouteResult;

@Sharable
class RouterHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final Server server;

    RouterHandler(Server server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        // Log time taken to process the request
        final long beginNano = System.nanoTime();

        final Channel channel = ctx.channel();
        final RouteResult<Action> routeResult = server.route(msg.method(), msg.uri());
        final Request request = new Request(server, channel, msg, routeResult);
        final Response response = new Response(server, channel, request, routeResult);

        // Release request and response when the connection is closed, just in case
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture arg0) throws Exception {
                if (request.refCnt() > 0) request.release(request.refCnt());
                if (response.refCnt() > 0) response.release(response.refCnt());
            }
        });

        try {
            final Action before = (Action) server.instantiator().instantiate(server.before());
            if (before != null) before.run(request, response);

            if (!response.doneResponding()) {
                // After filter is run at Response#respond
                final Action action = (Action) server.instantiator().instantiate(routeResult.target());
                action.run(request, response);

                // If this is async, pause reading and resume at Response#respond
                if (!response.doneResponding()) NoRealPipelining.pauseReading(channel);
            }
        } catch (Exception e1) {
            try {
                ErrorHandler errorHandler = (ErrorHandler) server.instantiator().instantiate(server.error());
                errorHandler.run(request, response, e1);
            } catch (Exception e2) {
                new DefaultErrorHandler().run(request, response, e2);
            }
        }

        // Access log; the action can be async
        final long endNano = System.nanoTime();
        final long dt = endNano - beginNano;
        final Object asyncOrResponseStatus = response.doneResponding() ? response.getStatus() : "async";
        if (dt >= 1000000L) {
            Log.info("[{}] {} {} - {} {} [ms]", request.remoteIp(), request.getMethod(), request.getUri(), asyncOrResponseStatus, dt / 1000000L);
        } else if (dt >= 1000) {
            Log.info("[{}] {} {} - {} {} [us]", request.remoteIp(), request.getMethod(), request.getUri(), asyncOrResponseStatus, dt / 1000);
        } else {
            Log.info("[{}] {} {} - {} {} [ns]", request.remoteIp(), request.getMethod(), request.getUri(), asyncOrResponseStatus, dt);
        }
    }
}
