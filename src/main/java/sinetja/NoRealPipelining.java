package sinetja;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;

/**
 * http://en.wikipedia.org/wiki/HTTP_pipelining
 *
 * <p>Sinetja does not support real pipelining. A client may send multiple requests,
 * but Sinetja will not process them at concurrently. Sinetja will process one
 * by one.
 *
 * <p>From Mongrel2 doc: http://mongrel2.org/manual/book-finalch6.html
 *
 * <p>"Where problems come in is with pipe-lined requests, meaning a browser sends
 * a bunch of requests in a big blast, then hangs out for all the responses.
 * This was such a horrible stupid idea that pretty much everone gets it wrong
 * and doesn't support it fully, if at all. The reason is it's much too easy to
 * blast a server with a ton of request, wait a bit so they hit proxied backends,
 * and then close the socket. The web server and the backends are now screwed
 * having to handle these requests which will go nowhere."
 */
public class NoRealPipelining {
    public static void pauseReading(Channel channel) {
        channel.config().setAutoRead(false);
    }

    public static void resumeReading(Channel channel) {
        // We don't have to call channel.read() because setAutoRead also calls
        // channel.read() if not reading
        channel.config().setAutoRead(true);
    }

    /**
     * Handle keep alive as long as there's the request contains
     * 'connection:Keep-Alive' header, no matter what the client is 1.0 or 1.1:
     * http://sockjs.github.com/sockjs-protocol/sockjs-protocol-0.3.3.html#section-157
     */
    public static void if_keepAliveRequest_then_resumeReading_else_closeOnComplete(
            final HttpRequest request, final Channel channel, final ChannelFuture channelFuture
    ) {
        // TODO:
        // Add Connection: Close, or Keep-Alive?
        // res.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        if (HttpUtil.isKeepAlive(request)) {
            channelFuture.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    resumeReading(channel);
                }
            });
        } else {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }
}
