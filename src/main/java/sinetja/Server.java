package sinetja;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.ChainRouter;

public class Server extends ChainRouter<Server> {
  /** Default: 1MB */
  public static final int MAX_CONTENT_LENGTH = 1024 * 1024;

  //----------------------------------------------------------------------------
  // Config

  protected int     maxContentLength = MAX_CONTENT_LENGTH;
  protected boolean openSSL          = false;

  //----------------------------------------------------------------------------

  public int maxContentLength() {
    return maxContentLength;
  }

  public Server maxContentLength(int maxContentLength) {
    this.maxContentLength = maxContentLength;
    return this;
  }

  //----------------------------------------------------------------------------

  protected final PipelineInitializer pipelineInitializer = new PipelineInitializer(this);

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

  public void start(int port, String certChainFile, String keyFile) {

  }
}
