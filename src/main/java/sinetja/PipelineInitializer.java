package sinetja;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.Router;

class PipelineInitializer extends ChannelInitializer<SocketChannel> {
  private static final int MAX_CONTENT_LENGTH = 100 * 1024;

  private final Router router;

  public PipelineInitializer(Router router) {
    this.router = router;
  }

  public void initChannel(SocketChannel ch) {
    ChannelPipeline p = ch.pipeline();
    p.addLast(new HttpRequestDecoder());
    p.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
    p.addLast(new HttpResponseEncoder());
    p.addLast(router.name(), router);
  }
}
