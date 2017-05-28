package sinetja;

import io.netty.handler.codec.http.HttpResponseStatus;

public class DefaultNotFoundHandler implements Action {
    @Override
    public void run(Request request, Response response) throws Exception {
        response.setStatus(HttpResponseStatus.NOT_FOUND);
        response.respondText("404 Not Found");
    }
}
