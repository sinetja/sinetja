package sinetja;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.router.Routed;

public class Request implements FullHttpRequest {
  private final Server  server;
  private final Channel channel;
  private final Routed  routed;

  private final String clientIp;
  private final String remoteIp;

  /**
   * Will be released after method <code>execute</code> is run. If you want to
   * keep it, call <code>request.retain()</code>.
   */
  private final FullHttpRequest request;

  /**
   * Set if the request content type is
   * <code>application/x-www-form-urlencoded</code>.
   */
  private final Map<String, List<String>> bodyParams;

  public Request(Server server, Channel channel, Routed routed) {
    this.server  = server;
    this.channel = channel;
    this.routed  = routed;

    request = (FullHttpRequest) routed.request();

    // Get client IP while the client is still connected; Netty may not allow
    // us to get this info later when the connection may be closed
    clientIp = getClientIpFromChannel();
    remoteIp = getRemoteIpFromClientIpOrReverseProxy();

    // Parse body params
    String contentTye = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
    if (HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED.equals(contentTye)) {
      String             content = request.content().toString(server.charset());
      QueryStringDecoder qsd     = new QueryStringDecoder("?" + content);
      bodyParams                 = qsd.parameters();
    } else {
      bodyParams = Collections.<String, List<String>>emptyMap();
    }
  }

  //----------------------------------------------------------------------------
  // Additional methods

  public Server server() {
    return server;
  }

  public Channel channel() {
    return channel;
  }

  /** @return IP of the direct HTTP client (may be the proxy) */
  public String clientIp() {
    return clientIp;
  }

  public String remoteIp() {
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

  public String path() {
    return routed.path();
  }

  //----------------------------------------------------------------------------
  // Param access methods

  public Map<String, String> pathParams() {
    return routed.pathParams();
  }

  public Map<String, List<String>> queryParams() {
    return routed.queryParams();
  }

  public Map<String, List<String>> bodyParams() {
    return bodyParams;
  }

  /**
   * Order: path, body, query
   *
   * When there's no param, this method will throw exception <code>MissingParam</code>.
   * If you don't handle this exception, response "400 Bad Request" will be automatically
   * responded by method respondMissingParam (you can override it if you want).
   * If you want "null" instead, please use method "paramo".
   */
  public String param(String name) throws MissingParam {
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
  public String paramo(String name) {
    String ret = routed.pathParams().get(name);
    if (ret != null) return ret;

    if (bodyParams != null && bodyParams.containsKey(name)) return bodyParams.get(name).get(0);

    ret = routed.queryParam(name);
    if (ret != null) return ret;

    return null;
  }

  public List<String> params(String name) {
    List<String> ret = routed.params(name);
    if (bodyParams.containsKey(name)) ret.addAll(bodyParams.get(name));
    return ret;
  }

  //----------------------------------------------------------------------------
  // Implement FullHttpRequest

  @Override
  public HttpMethod getMethod() {
    return request.getMethod();
  }

  @Override
  public String getUri() {
    return request.getUri();
  }

  @Override
  public HttpVersion getProtocolVersion() {
    return request.getProtocolVersion();
  }

  @Override
  public HttpHeaders headers() {
    return request.headers();
  }

  @Override
  public DecoderResult getDecoderResult() {
    return request.getDecoderResult();
  }

  @Override
  public void setDecoderResult(DecoderResult arg0) {
    request.setDecoderResult(arg0);
  }

  @Override
  public HttpHeaders trailingHeaders() {
    return request.trailingHeaders();
  }

  @Override
  public HttpContent duplicate() {
    return request.duplicate();
  }

  @Override
  public ByteBuf content() {
    return request.content();
  }

  @Override
  public int refCnt() {
    return refCnt();
  }

  @Override
  public boolean release() {
    return request.release();
  }

  @Override
  public boolean release(int arg0) {
    return request.release(arg0);
  }

  @Override
  public FullHttpRequest copy() {
    return request.copy();
  }

  @Override
  public FullHttpRequest retain() {
    return request.retain();
  }

  @Override
  public FullHttpRequest retain(int arg0) {
    return request.retain(arg0);
  }

  @Override
  public FullHttpRequest setMethod(HttpMethod arg0) {
    return request.setMethod(arg0);
  }

  @Override
  public FullHttpRequest setProtocolVersion(HttpVersion arg0) {
    return request.setProtocolVersion(arg0);
  }

  @Override
  public FullHttpRequest setUri(String arg0) {
    return request.setUri(arg0);
  }

  @Override
  public String toString() {
    return request.toString();
  }
}
