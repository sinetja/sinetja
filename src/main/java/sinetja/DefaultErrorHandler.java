package sinetja;

import io.netty.handler.codec.http.HttpResponseStatus;

public class DefaultErrorHandler implements ErrorHandler {
    @Override
    public void run(Request request, Response response, Exception e) throws Exception {
        if (e instanceof MissingParam) {
            MissingParam mp = (MissingParam) e;
            response.setStatus(HttpResponseStatus.BAD_REQUEST);
            response.respondText("400 Bad Request, missing param: " + mp.param());
        } else {
            Log.error("Error when processing request: {}", request, e);
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            response.respondText("500 Internal Server Error");
        }
    }
}
