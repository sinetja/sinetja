Sinetja = Sinatra + Netty + Java

You can use Sinetja with Java 5+.

Style 1: Use with Java 8 - Lambda style
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Java 8 provides convenient lambda syntax:

::

  import sinetja.Server;

  public class App {
    public static void main(String[] args) {
      new Server()

      .GET("/", (req, res) -> {
        res.respondText("Hello world");
      })

      .GET("/hello/:name", (req, res) -> {
        String name = req.param("name");
        res.respondText("Hello " + name);
      })

      .start(8000);
    }
  }

``req`` and ``res`` are
`FullHttpRequest <http://netty.io/4.0/api/io/netty/handler/codec/http/FullHttpRequest.html>`_
and
`FullHttpResponse <http://netty.io/4.0/api/io/netty/handler/codec/http/FullHttpResponse.html>`_
with several helper methods to let you extract params and respond.

Javadoc:

* `Sinetja <http://sinetja.github.io/sinetja>`_
* `Netty <http://netty.io/4.0/api/io/netty/handler/codec/http/package-summary.html>`_

New project skeleton:

* `Sinetja-Skeleton8 <https://github.com/sinetja/sinetja-skeleton8>`_:
  If you want to use Java 8 with its lambda syntax.
* `Sinetja-Skeleton <https://github.com/sinetja/sinetja-skeleton>`_:
  If you use older Java.
* `Sinetja-Scaleton <https://github.com/sinetja/sinetja-scaleton>`_:
  If you use Scala. Please also try `Xitrum <http://xitrum-framework.github.io/>`_.

`Discussion mailing list (Google group) <https://groups.google.com/group/sinetja>`_

Style 2: Use with older Java
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This style is just a verbose version of Java 8.

::

  import sinetja.Server;
  import sinetja.Action;
  import sinetja.Request;
  import sinetja.Response;

  public class App {
    public static void main(String[] args) {
      new Server()

      .GET("/", new Action() {
        public void run(Request req, Response res) throws Exception {
          res.respondText("Hello world");
        }
      })

      .GET("/hello/:name", new Action() {
        public void run(Request req, Response res) throws Exception {
          String name = req.param("name");
          res.respondText("Hello " + name);
        }
      })

      .start(8000);
    }
  }

Style 3: Use Action classes
~~~~~~~~~~~~~~~~~~~~~~~~~~~

In this style, you route by classes, instead of instances as above.
You can certainly use this style with any Java version.

This style allows you to create reverse routing (see below), suitable for
creating web pages with URL links among them.

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

  public class App {
    public static void main(String[] args) {
      new Server()
        .GET("/",            IndexAction.class)
        .GET("/hello/:name", HelloAction.class)
        .start(8000);
    }
  }

Request methods
~~~~~~~~~~~~~~~

``CONNECT``, ``DELETE``, ``GET``, ``HEAD``, ``OPTIONS``,
``PATCH``, ``POST``, ``PUT``, ``TRACE``, ``ANY``

``ANY`` means the route will match all request methods.

If you want to specify that a route should be matched first or last:
``GET_FIRST``, ``GET_LAST`` etc.

::

  server
    .GET      ("/articles/:id", ShowAction.class)
    .GET_FIRST("/articles/new", NewAction.class)

The route target can be an ``Action`` class or an ``Action`` instance.

Reverse routing
~~~~~~~~~~~~~~~

::

  server.path(IndexAction.class)
  // => "/"

  server.path(HelloAction.class, "name", "World")
  // => "/hello/World"

Access request params
~~~~~~~~~~~~~~~~~~~~~

Order of priority: path > body > query

::

  String       param(String name) throws MissingParam
  String       paramo(String name)
  List<String> params(String name)

``param`` and ``paramo`` return a single value.
``params`` returns a collection of values (params can have same name).

If the param is missing:

* ``paramo`` will just returns null.
* ``param`` will throw ``MissingParam``. By default, Sinetja will respond error
  400 bad request for you. You want to change that behavior, you can catch that
  exception in your action, or at the global error handler (see
  "500 Internal Server Error" section below).

Respond
~~~~~~~

::

  respondText
  respondHtml
  respondJson
  etc.

See `Javadoc <http://sinetja.github.io/sinetja/sinetja/Response.html>`_.

All the methods return `ChannelFuture <http://netty.io/4.0/api/io/netty/channel/ChannelFuture.html>`_.

Async
^^^^^

Thanks to `Netty <http://netty.io/>`_, unlike most Java web frameworks,
Sinetja is async. You don't have to respond right away as soon as possible.
You can respond later.

Before filter
~~~~~~~~~~~~~

Java 8 style:

::

  server.before((req, res) -> {
    ...
  });

If the filter responds something, the main action will not be called.

Older Java style:

::

  server.before(new Action() {
    public void run(Request req, Response res) {
      ...
    }
  );

Class style:

::

  public class BeforeFilter extends Action {
    public void run(Request req, Response res) {
      ...
    }
  }

  server.before(BeforeFilter.class);

After filter
~~~~~~~~~~~~

Similar to before filter.
It's run after the main action, but before the response is returned to the client.
For example, if you want to add a header to all responses, you can do it here.

Log
~~~

::

  import sinetja.Log;
  Log.info("Some info");

Sinetja uses `SLF4J <www.slf4j.org>`_.
Please add an implementation like `Logback <http://logback.qos.ch/>`_ to your project.

::

  <dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.1.2</version>
  </dependency>

404 Not Found
~~~~~~~~~~~~~

If there's no matched action, Sinetja will automatically respond simple
"Not Found" text for you.

If you want to handle yourself (response status has already been set to 404,
you don't have to set it yourself):

Java 8 style:

::

  server.notFound((req, res) -> {
    String uri = req.getUri();
    Log.info("User tried to access nonexistant path: {}", uri);
    res.respondText("Not Found: " + uri);
  });

Older Java style:

::

  server.notFound(new Action() {
    public void run(Request req, Response res) {
      String uri = req.getUri();
      Log.info("User tried to access nonexistant path: {}", uri);
      res.respondText("Not Found: " + uri);
    }
  );

Class style:

::

  public class NotFound extends Action {
    public void run(Request req, Response res) {
      String uri = request.getUri();
      Log.info("User tried to access nonexistant path: {}", uri);
      res.respondText("Not Found: " + uri);
    }
  }

  server.notFound(NotFound.class);

500 Internal Server Error
~~~~~~~~~~~~~~~~~~~~~~~~~

If there's no error handler, Sinetja will automatically respond simple
"Internal Server Error" text for you.

If you want to handle yourself (response status has already been set to 500,
you don't have to set it yourself):

Java 8 style:

::

  server.error((req, res, e) -> {
    String uri = req.getUri();
    Log.error("Error when user tried to access path: {}", e);
    res.respondText("Internal Server Error: " + uri);
  });

Older Java style:

::

  server.error(new ErrorHandler() {
    public void run(Request req, Response res, Exception e) {
      String uri = req.getUri();
      Log.error("Error when user tried to access path: " + uri, e);
      res.respondText("Internal Server Error: " + uri);
    }
  );

Class style:

::

  public class ErrorHandler extends ErrorHandler {
    public void run(Request req, Response res, Exception e) {
      String uri = req.getUri();
      Log.error("Error when user tried to access path: " + uri, e);
      res.respondText("Internal Server Error: " + uri);
    }
  }

  server.error(ErrorHandler.class);

HTTPS
~~~~~

Use autogenerated selfsigned certificate:

::

  server.jdkSsl()

or (Apache Portable Runtime and OpenSSL libs must be in load path such as system
library directories, $LD_LIBRARY_PATH on *nix or %PATH% on Windows):

::

  server.openSsl()

If you want to use your own certificate:

::

  jdkSsl(String certChainFile, String keyFile)
  jdkSsl(String certChainFile, String keyFile, String keyPassword)
  jdkSsl(
    String certChainFile, String keyFile, String keyPassword,
    Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout
  )

  openSsl(String certChainFile, String keyFile)
  openSsl(String certChainFile, String keyFile, String keyPassword)
  openSsl(
    String certChainFile, String keyFile, String keyPassword,
    Iterable<String> ciphers, Iterable<String> nextProtocols, long sessionCacheSize, long sessionTimeout
  )

The above are utility methods for setting `SslContext <http://netty.io/4.0/api/io/netty/handler/ssl/SslContext.html>`_.
If you want to set it directly:

::

   sslContext(SslContext sslContext)

CORS
~~~~

To tell the server to handle `CORS <http://en.wikipedia.org/wiki/Cross-origin_resource_sharing>`_,
set `CorsConfig <http://netty.io/4.0/api/io/netty/handler/codec/http/cors/CorsConfig.html>`_:

::

  import io.netty.handler.codec.http.cors.CorsConfig;

  CorsConfig config = CorsConfig.withAnyOrigin().build();
  server.cors(config);

Use with Maven
~~~~~~~~~~~~~~

::

  <dependency>
    <groupId>tv.cntt</groupId>
    <artifactId>sinetja</artifactId>
    <version>1.2</version>
  </dependency>

Together with Netty, Sinetja also adds `Javassist <http://javassist.org/>`_ as
a dependency, because it boosts Netty speed.

Sinetja uses `netty-router <https://github.com/sinetja/netty-router>`_.
