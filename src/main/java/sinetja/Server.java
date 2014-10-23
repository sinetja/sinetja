package sinetja;

import jauter.Router;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.CharsetUtil;

public class Server extends Router<HttpMethod, Action> {
  protected HttpMethod CONNECT() { return HttpMethod.CONNECT; }
  protected HttpMethod DELETE()  { return HttpMethod.DELETE ; }
  protected HttpMethod GET()     { return HttpMethod.GET    ; }
  protected HttpMethod HEAD()    { return HttpMethod.HEAD   ; }
  protected HttpMethod OPTIONS() { return HttpMethod.OPTIONS; }
  protected HttpMethod PATCH()   { return HttpMethod.PATCH  ; }
  protected HttpMethod POST()    { return HttpMethod.POST   ; }
  protected HttpMethod PUT()     { return HttpMethod.PUT    ; }
  protected HttpMethod TRACE()   { return HttpMethod.TRACE  ; }

  //----------------------------------------------------------------------------

  /** 1MB */
  public static final int MAX_CONTENT_LENGTH = 1024 * 1024;

  //----------------------------------------------------------------------------
  // Config

  /** Default: false */
  protected boolean openSsl = false;

  /** Default: MAX_CONTENT_LENGTH */
  protected int maxContentLength = MAX_CONTENT_LENGTH;

  /** Default: UTF-8. */
  protected Charset charset = CharsetUtil.UTF_8;

  //----------------------------------------------------------------------------

  public boolean openSsl() {
    return openSsl;
  }

  public Server openSsl(boolean openSsl) {
    this.openSsl = openSsl;
    return this;
  }

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

  //----------------------------------------------------------------------------

  public Server CONNECT(String path, ChannelInboundHandler handlerInstance) {
    return pattern(HttpMethod.CONNECT, path, handlerInstance);
  }
}
