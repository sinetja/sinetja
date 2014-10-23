package sinetja

class IndexAction extends Action {
  def execute() {
    respondText("Hello Sinetja")
  }
}

class HelloAction extends Action {
  def execute() {
    val name = param("name")
    respondText(s"Hello $name")
  }
}

class NotFoundAction extends Action {
  def execute() {
    // Demo about log
    val uri = request.getUri()
    log.info("User tried to access nonexistant path: {}", uri)

    // Response status has already been set to 404 Not Found by Sinetja
    respondText("404 Not Found: " + request.getUri)
  }
}

object Example {
  def main(args: Array[String]) {
    (new Server)
      .GET("/",            classOf[IndexAction])
      .GET("/hello/:name", classOf[HelloAction])
      .handler404(classOf[NotFoundAction])
      .start(8000)
  }
}
