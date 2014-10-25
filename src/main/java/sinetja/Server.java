package sinetja;

import io.netty.handler.codec.http.router.MethodRouter;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

public class Server extends MethodRouter<Object, Server> {
  @Override protected Server getThis() { return this; }

  //----------------------------------------------------------------------------

  /** 1MB */
  public static final int MAX_CONTENT_LENGTH = 1024 * 1024;

  //----------------------------------------------------------------------------
  // Config

  /** Default: false */
  private boolean openSsl = false;

  /** Default: MAX_CONTENT_LENGTH */
  private int maxContentLength = MAX_CONTENT_LENGTH;

  /** Default: UTF-8. */
  private Charset charset = CharsetUtil.UTF_8;

  private Object errorHandler;

  //----------------------------------------------------------------------------

  public boolean openSsl() {
    return openSsl;
  }

  public Server openSsl(boolean openSsl) {
    this.openSsl = openSsl;
    return getThis();
  }

  public int maxContentLength() {
    return maxContentLength;
  }

  public Server maxContentLength(int maxContentLength) {
    this.maxContentLength = maxContentLength;
    return getThis();
  }

  public Charset charset() {
    return charset;
  }

  public Server charset(Charset charset) {
    this.charset = charset;
    return getThis();
  }

  public Object errorHandler() {
    return errorHandler;
  }

  public Server ERROR(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
    return getThis();
  }

  public Server ERROR(Class<? extends ErrorHandler> errorHandler) {
    this.errorHandler = errorHandler;
    return getThis();
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
