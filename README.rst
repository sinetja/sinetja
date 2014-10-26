Sinetja = Sintra + Netty + Java

You can use Sinetja with Java 5+.

Javadoc:

* `Sinetja <http://sinetja.github.io/sinetja>`_
* `Netty <http://netty.io/4.0/api/io/netty/handler/codec/http/package-summary.html>`_

Use with Java 8 - Lambda style
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Java 8 provides convenient lambda syntax:

::

  import sinetja.Server;

  public App {
    public static void main(String[] args) {
      new Server()

      .GET("/", (req, res) ->
        res.respondText("Hello world");
      )

      .GET("/hello/:name", (req, res) ->
        String name = req.param("name");
        res.respondText("Hello " + name);
      )

      .start(8000);
    }
  }

``req`` and ``res`` are
`FullHttpRequest <http://netty.io/4.0/api/io/netty/handler/codec/http/FullHttpRequest.html>`_
and
`FullHttpResponse <http://netty.io/4.0/api/io/netty/handler/codec/http/FullHttpResponse.html>`_
with several helper methods to let you extract params and respond.

Use with older Java - Style 1 of 2
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This style is just a verbose version of Java 8.

::

  import sinetja.Server;
  import sinetja.Action;
  import sinetja.Request;
  import sinetja.Response;

  public App {
    public static void main(String[] args) {
      new Server()

      .GET("/", new Action() {
        public void run(Request req, Response res) {
          res.respondText("Hello world");
        }
      )

      .GET("/hello/:name", new Action() {
        public void run(Request req, Response res) {
          String name = req.param("name");
          res.respondText("Hello " + name);
        }
      )

      .start(8000);
    }
  }

Use with older Java - Style 2 of 2
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In this style, you route by classes, instead of instances as above.
You can certainly use this style with Java 8.

This style allows you to create reverse routing (see below), suitable for creating
web pages with URL links among them.

::

  import sinetja.Server;
  import sinetja.Action;
  import sinetja.Request;
  import sinetja.Response;

  public class IndexAction extends Action {
    public void run(Request req, Response res) {
      req.respondText("Hello Sinetja");
    }
  }

  public class HelloAction extends Action {
    public void run(Request req, Response res) {
      String name = req.param("name");
      res.respondText("Hello " + name");
    }
  }

  public App {
    public static void main(String[] args) {
      new Server()
        .GET("/",            IndexAction.class)
        .GET("/hello/:name", HelloAction.class)
        .start(8000);
    }
  }

Routing
~~~~~~~

Methods: CONNECT, DELETE, GET, HEAD, OPTIONS, PATCH, POST, PUT, TRACE, ANY. ANY
means the route will match all methods.

Order: GET_FIRST, GET_LAST etc.

The route target can be an Action class or an Action instance.

Reverse routing
~~~~~~~~~~~~~~~

::

  server.path(IndexAction.class)
  server.path(HelloAction.class, "name", "World")

Access request params
~~~~~~~~~~~~~~~~~~~~~

Order of priority: path > body > query

::

  String       param(String name) throws MissingParam
  String       paramo(String name)
  List<String> params(String name)

TODO: Write doc in more detail

Respond
~~~~~~~

Respond text/plain:

::

  ChannelFuture respondText(Object  text)
  ChannelFuture respondText(ByteBuf buf)

Respond text/html:

::

  ChannelFuture respondHtml(Object  text)
  ChannelFuture respondHtml(ByteBuf buf)

Log
~~~

Sinetja uses `SLF4J <www.slf4j.org>`_.
Please add an implementation like `Logback <http://logback.qos.ch/>`_ to your project.

::

  import sinetja.Log;
  Log.info("Some info");

404 Not Found
~~~~~~~~~~~~~

If there's no matched action, Sinetjy will automatically respond simple
"Not Found" text for you.

If you want to handle yourself (response status has already been set to 404,
you don't have to set it yourself):

Java 8 style:

::

  server.NOT_FOUND((req, res) ->
    String uri = req.getUri();
    Log.info("User tried to access nonexistant path: {}", uri);
    res.respondText("Not Found: " + uri);
  );

Older Java style:

::

  server.NOT_FOUND(new Action() {
    public void run(Request req, Response res) {
      String uri = req.getUri();
      Log.info("User tried to access nonexistant path: {}", uri);
      res.respondText("Not Found: " + uri);
    }
  );

Class style:

::

  public class NotFoundAction extends Action {
    public void run() {
      String uri = request.getUri();
      Log.info("User tried to access nonexistant path: {}", uri);
      res.respondText("Not Found: " + uri);
    }
  }

  server.NOT_FOUND(NotFoundAction.class);

500 Internal Server Error
~~~~~~~~~~~~~~~~~~~~~~~~~

If there's no error handler, Sinetjy will automatically respond simple
"Internal Server Error" text for you.

If you want to handle yourself (response status has already been set to 500,
you don't have to set it yourself):

Java 8 style:

::

  server.ERROR((req, res, e) ->
    String uri = req.getUri();
    Log.error("Error when user tried to access path: {}", e);
    res.respondText("Internal Server Error: " + uri);
  );

Older Java style:

::

  server.ERROR(new Action() {
    public void run(Request req, Response res) {
      String uri = req.getUri();
      Log.error("Error when user tried to access path: " + uri, e);
      res.respondText("Internal Server Error: " + uri);
    }
  );

Class style:

::

  public class ErrorHandler extends ErrorHandler {
    public void run() {
      String uri = req.getUri();
      Log.error("Error when user tried to access path: " + uri, e);
      res.respondText("Internal Server Error: " + uri);
    }
  }

  server.ERROR(ErrorHandler.class);

HTTPS
~~~~~

TODO

Use with Maven
~~~~~~~~~~~~~~

::

  <dependency>
    <groupId>tv.cntt</groupId>
    <artifactId>sinetja</artifactId>
    <version>1.1</version>
  </dependency>

Together with Netty, Sinetja also adds `Javassist <http://javassist.org/>`_ as
a dependency, because it boosts Netty speed.

Sinetja uses `netty-router <https://github.com/sinetja/netty-router>`_.
