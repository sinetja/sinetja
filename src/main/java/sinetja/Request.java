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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.router.RouteResult;

public class Request implements FullHttpRequest {
    private final Server server;
    private final Channel channel;

    /**
     * Will be released after method {@code execute} is run. If you want to
     * keep it, call {@code execute}.
     */
    private final FullHttpRequest request;

    private final RouteResult<Action> routeResult;

    private final String clientIp;
    private final String remoteIp;

    /**
     * Set if the request content type is {@code application/x-www-form-urlencoded}.
     */
    private final Map<String, List<String>> bodyParams;

    public Request(Server server, Channel channel, FullHttpRequest request, RouteResult<Action> routeResult) {
        this.server = server;
        this.channel = channel;
        this.request = request;
        this.routeResult = routeResult;

        // Get client IP while the client is still connected; Netty may not allow
        // us to get this info later when the connection may be closed
        clientIp = getClientIpFromChannel();
        remoteIp = getRemoteIpFromClientIpOrReverseProxy();

        // Parse body params
        String contentTye = request.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.contentEqualsIgnoreCase(contentTye)) {
            String content = request.content().toString(server.charset());
            QueryStringDecoder qsd = new QueryStringDecoder("?" + content);
            bodyParams = qsd.parameters();
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

    /**
     * @return IP of the direct HTTP client (may be the proxy)
     */
    public String clientIp() {
        return clientIp;
    }

    public String remoteIp() {
        return remoteIp;
    }

    public String decodedPath() {
        return routeResult.decodedPath();
    }

    private String getClientIpFromChannel() {
        SocketAddress remoteAddress = channel.remoteAddress();

        // TODO: inetSocketAddress can be Inet4Address or Inet6Address
        // See java.net.preferIPv6Addresses
        InetSocketAddress inetSocketAddress = (InetSocketAddress) remoteAddress;
        InetAddress addr = inetSocketAddress.getAddress();
        return addr.getHostAddress();
    }

    private String getRemoteIpFromClientIpOrReverseProxy() {
        // FIXME
        return clientIp;
    }

    //----------------------------------------------------------------------------
    // Param access methods

    public Map<String, String> pathParams() {
        return routeResult.pathParams();
    }

    public Map<String, List<String>> queryParams() {
        return routeResult.queryParams();
    }

    public Map<String, List<String>> bodyParams() {
        return bodyParams;
    }

    /**
     * Order: path, body, query
     *
     * <p>When there's no param, this method will throw exception <code>MissingParam</code>.
     * If you don't handle this exception, response "400 Bad Request" will be automatically
     * responded by method respondMissingParam (you can override it if you want).
     * If you want "null" instead, please use method "paramo".
     */
    public String param(String name) throws MissingParam {
        String ret = routeResult.pathParams().get(name);
        if (ret != null) {
            return ret;
        }

        if (bodyParams != null && bodyParams.containsKey(name)) {
            return bodyParams.get(name).get(0);
        }

        ret = routeResult.queryParam(name);
        if (ret != null) {
            return ret;
        }

        throw new MissingParam(name);
    }

    /**
     * Order: path, body, query
     * <p>
     * When there's no param, this method will return null. See also method <code>param</code>.
     */
    public String paramo(String name) {
        String ret = routeResult.pathParams().get(name);
        if (ret != null) {
            return ret;
        }

        if (bodyParams != null && bodyParams.containsKey(name)) {
            return bodyParams.get(name).get(0);
        }

        ret = routeResult.queryParam(name);
        if (ret != null) {
            return ret;
        }

        return null;
    }

    public List<String> params(String name) {
        List<String> ret = routeResult.params(name);
        if (bodyParams.containsKey(name)) {
            ret.addAll(bodyParams.get(name));
        }
        return ret;
    }

    //----------------------------------------------------------------------------
    // Implement FullHttpRequest

    @Override
    public HttpMethod getMethod() {
        return request.getMethod();
    }

    @Override
    public HttpMethod method() {
        return request.method();
    }

    @Override
    public String getUri() {
        return request.getUri();
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public HttpVersion getProtocolVersion() {
        return request.getProtocolVersion();
    }

    @Override
    public HttpVersion protocolVersion() {
        return request.protocolVersion();
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
    public DecoderResult decoderResult() {
        return request.decoderResult();
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
    public FullHttpRequest retainedDuplicate() {
        return request.retainedDuplicate();
    }

    @Override
    public FullHttpRequest replace(ByteBuf content) {
        return request.replace(content);
    }

    @Override
    public ByteBuf content() {
        return request.content();
    }

    @Override
    public int refCnt() {
        return request.refCnt();
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
    public FullHttpRequest duplicate() {
        return request.duplicate();
    }

    @Override
    public FullHttpRequest retain() {
        return request.retain();
    }

    @Override
    public FullHttpRequest touch() {
        return request.touch();
    }

    @Override
    public FullHttpRequest touch(Object hint) {
        return request.touch(hint);
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
