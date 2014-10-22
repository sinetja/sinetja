package sinetja;

import io.netty.handler.codec.http.HttpMethod

class IndexAction extends Action {
  def execute() {
    respondText("Hello Sinetja")
  }
}

object Example {
  def main(args: Array[String]) {
    Server.router().pattern(HttpMethod.GET, "/", classOf[IndexAction])
    Server.start(8000)
  }
}
