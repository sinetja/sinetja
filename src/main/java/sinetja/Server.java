package sinetja;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.Router;

public class Server extends Router {
  /** Default: 1MB */
  public static final int MAX_CONTENT_LENGTH = 1024 * 1024;

  //----------------------------------------------------------------------------
  // Config

  public int maxContentLength = MAX_CONTENT_LENGTH;

  //----------------------------------------------------------------------------
  // Route helpers

  public Server connect(String path, Class<? extends Action> actionClass) {
    pattern(HttpMethod.CONNECT, path, actionClass);
    return this;
  }

  public Server delete(String path, Class<? extends Action> actionClass) {
    pattern(HttpMethod.DELETE, path, actionClass);
    return this;
  }

  public Server get(String path, Class<? extends Action> actionClass) {
    pattern(HttpMethod.GET, path, actionClass);
    return this;
  }

  public Server head(String path, Class<? extends Action> actionClass) {
    pattern(HttpMethod.HEAD, path, actionClass);
    return this;
  }

  public Server options(String path, Class<? extends Action> actionClass) {
    pattern(HttpMethod.OPTIONS, path, actionClass);
    return this;
  }

  public Server patch(String path, Class<? extends Action> actionClass) {
    pattern(HttpMethod.PATCH, path, actionClass);
    return this;
  }

  public Server post(String path, Class<? extends Action> actionClass) {
    pattern(HttpMethod.POST, path, actionClass);
    return this;
  }

  public Server put(String path, Class<? extends Action> actionClass) {
    pattern(HttpMethod.PUT, path, actionClass);
    return this;
  }

  public Server trace(String path, Class<? extends Action> actionClass) {
    pattern(HttpMethod.TRACE, path, actionClass);
    return this;
  }

  public Server notFound(Class<? extends Action> actionClass) {
    pattern(null, ":*", actionClass);
    return this;
  }

  //----------------------------------------------------------------------------

  private final PipelineInitializer pipelineInitializer = new PipelineInitializer(this);

  public void start(int port) {
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
      Log.info("HTTP server started: http://127.0.0.1:" + port + '/');

      ch.closeFuture().sync();
    } catch (InterruptedException e) {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
