package sinetja;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.BadClientSilencer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.ssl.SslContext;

class PipelineInitializer extends ChannelInitializer<SocketChannel> {
    private final Server server;
    private final RouterHandler routerHandler;
    private final BadClientSilencer badClientSilencer;

    public PipelineInitializer(Server server) {
        this.server = server;
        this.routerHandler = new RouterHandler(server);
        this.badClientSilencer = new BadClientSilencer() {
            @Override
            protected void onUnknownMessage(Object msg) {
                if (msg != io.netty.handler.codec.http.LastHttpContent.EMPTY_LAST_CONTENT) {
                    Log.trace("Unknown msg: " + msg);
                }
            }

            @Override
            protected void onBadClient(Throwable e) {
                Log.trace("Caught exception (maybe client is bad)", e);
            }

            @Override
            protected void onBadServer(Throwable e) {
                Log.error("Caught exception (maybe server is bad)", e);
            }
        };
    }

    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        SslContext sslContext = server.sslContext();
        if (sslContext != null) p.addLast(sslContext.newHandler(ch.alloc()));

        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpObjectAggregator(server.maxContentLength()));
        p.addLast(new HttpResponseEncoder());

        if (server.cors() != null) p.addLast(new CorsHandler(server.cors()));

        p.addLast(routerHandler);
        p.addLast(badClientSilencer);
    }
}
