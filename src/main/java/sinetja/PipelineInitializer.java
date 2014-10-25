package sinetja;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.router.BadClientSilencer;

class PipelineInitializer extends ChannelInitializer<SocketChannel> {
  private final Server            server;
  private final RouterHandler     routerHandler;
  private final BadClientSilencer badClientSilencer = new BadClientSilencer();

  public PipelineInitializer(Server server) {
    this.server        = server;
    this.routerHandler = new RouterHandler(server);
  }

  public void initChannel(SocketChannel ch) {
    ChannelPipeline p = ch.pipeline();
    p.addLast(new HttpRequestDecoder());
    p.addLast(new HttpObjectAggregator(server.maxContentLength()));
    p.addLast(new HttpResponseEncoder());
    p.addLast(routerHandler);
    p.addLast(badClientSilencer);
  }
}
