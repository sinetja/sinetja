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
    respondText("404 Not Found: " + request.getUri)
  }
}

object Example {
  def main(args: Array[String]) {
    (new Server)
      .get("/",            classOf[IndexAction])
      .get("/hello/:name", classOf[HelloAction])
      .notFound(classOf[NotFoundAction])
      .start(8000)
  }
}
