package sinetja

class IndexAction extends Action {
  def run(req: Request, res: Response) {
    res.respondText("Hello Sinetja")
  }
}

class HelloAction extends Action {
  def run(req: Request, res: Response) {
    val name = req.param("name")
    res.respondText(s"Hello $name")
  }
}

class NotFoundAction extends Action {
  def run(req: Request, res: Response) {
    // Demo about log
    val uri = req.getUri()
    Log.info("User tried to access nonexistant path: {}", uri)

    // Response status has already been set to 404 Not Found by Sinetja
    res.respondText("Not Found: " + uri)
  }
}

object Example {
  def main(args: Array[String]) {
    (new Server)
      .GET("/",            classOf[IndexAction])
      .GET("/hello/:name", classOf[HelloAction])
      .NOT_FOUND(classOf[NotFoundAction])
      .start(8000)
  }
}
