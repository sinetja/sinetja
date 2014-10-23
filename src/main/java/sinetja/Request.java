package sinetja;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.Routed;

public class Request {
  protected Routed  routed;

  /**
   * Will be released after method <code>execute</code> is run. If you want to
   * keep it, call <code>request.retain()</code>.
   */
  protected FullHttpRequest request;

  /**
   * Set if the request content type is
   * <code>application/x-www-form-urlencoded</code>.
   */
  protected Map<String, List<String>> bodyParams;

  protected Channel channel;
  protected String  clientIp;
  protected String  remoteIp;

  public Request() {
    this.routed = routed;

    // Get client IP while the client is still connected; Netty may not allow
    // us to get this info later when the connection may be closed
    clientIp = getClientIpFromChannel();
    remoteIp = getRemoteIpFromClientIpOrReverseProxy();

    // Parse body params
    String contentTye = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
    if (HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED.equals(contentTye)) {
      String             content = request.content().toString(charset);
      QueryStringDecoder qsd     = new QueryStringDecoder("?" + content);
      bodyParams                 = qsd.parameters();
    }

  }

  /** @return IP of the direct HTTP client (may be the proxy) */
  protected String clientIp() {
    return clientIp;
  }

  protected String remoteIp() {
    return remoteIp;
  }

  private String getClientIpFromChannel() {
    SocketAddress remoteAddress = channel.remoteAddress();

    // TODO: inetSocketAddress can be Inet4Address or Inet6Address
    // See java.net.preferIPv6Addresses
    InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
    InetAddress       addr              = inetSocketAddress.getAddress();
    return addr.getHostAddress();
  }

  private String getRemoteIpFromClientIpOrReverseProxy() {
    return clientIp;  // FIXME
  }

  //----------------------------------------------------------------------------

  /**
   * Order: path, body, query
   *
   * When there's no param, this method will throw exception <code>MissingParam</code>.
   * If you don't handle this exception, response "400 Bad Request" will be automatically
   * responded by method respondMissingParam (you can override it if you want).
   * If you want "null" instead, please use method "paramo".
   */
  protected String param(String name) throws MissingParam {
    String ret = routed.pathParams().get(name);
    if (ret != null) return ret;

    if (bodyParams != null && bodyParams.containsKey(name)) return bodyParams.get(name).get(0);

    ret = routed.queryParam(name);
    if (ret != null) return ret;

    throw new MissingParam(name);
  }

  /**
   * Order: path, body, query
   *
   * When there's no param, this method will return null. See also method <code>param</code>.
   */
  protected String paramo(String name) {
    String ret = routed.pathParams().get(name);
    if (ret != null) return ret;

    if (bodyParams != null && bodyParams.containsKey(name)) return bodyParams.get(name).get(0);

    ret = routed.queryParam(name);
    if (ret != null) return ret;

    return null;
  }

  protected List<String> params(String name) {
    List<String> ret = routed.params(name);
    if (bodyParams.containsKey(name)) ret.addAll(bodyParams.get(name));
    return ret;
  }
}
