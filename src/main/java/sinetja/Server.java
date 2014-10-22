package sinetja;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.Router;

public class Server {
  private static final Router router = new Router();

  private static final PipelineInitializer pipelineInitializer = new PipelineInitializer(router);

  public static Router router() {
    return router;
  }

  public static void start(int port) {
    NioEventLoopGroup bossGroup   = new NioEventLoopGroup(1);
    NioEventLoopGroup workerGroup = new NioEventLoopGroup();

    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
       .childOption(ChannelOption.TCP_NODELAY,  java.lang.Boolean.TRUE)
       .childOption(ChannelOption.SO_KEEPALIVE, java.lang.Boolean.TRUE)
       .channel(NioServerSocketChannel.class)
       .childHandler(pipelineInitializer);

      Channel ch = b.bind(port).sync().channel();
      Log.info("Server started: http://127.0.0.1:" + port + '/');

      ch.closeFuture().sync();
    } catch (InterruptedException e) {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
