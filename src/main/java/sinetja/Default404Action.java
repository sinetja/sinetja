package sinetja;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;

public class Default404Action extends Action {
  private static final ByteBuf CONTENT_404 = Unpooled.copiedBuffer("404 Not Found".getBytes());

  protected void execute() throws Exception {
    response.setStatus(HttpResponseStatus.NOT_FOUND);
    respondText(CONTENT_404);
  }
}
