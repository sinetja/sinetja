package sinetja

import scala.xml.Xhtml

class IndexAction extends Action {
  def run(req: Request, res: Response) {
    val href = req.server().path(classOf[HelloAction], "name", "World")
    res.respondHtml(Xhtml.toXhtml(
      <body>
        <p>Hello Sinetja</p>
        <p><a href={href}>Link to another action</a></p>
      </body>
    ))
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
      .notFound(classOf[NotFoundAction])
      .jdkSsl()
      .start(8000)
  }
}
