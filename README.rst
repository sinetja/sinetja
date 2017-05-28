Sinetja = Sinatra + Netty + Java

You can use Sinetja with Java 6+.
Below examples use Java 8 lambda syntax.

Basic usage
~~~~~~~~~~~

::

  import sinetja.Server;

  public class App {
    public static void main(String[] args) {
      Server server = new Server()

      server
      .GET("/", (req, res) ->
        res.respondText("Hello world")
      )
      .GET("/hello/:name", (req, res) -> {
        String name = req.param("name");
        res.respondText("Hello " + name);
      })

      server.start(8000);
    }
  }

``req`` and ``res`` are
`FullHttpRequest <http://netty.io/4.1/api/io/netty/handler/codec/http/FullHttpRequest.html>`_
and
`FullHttpResponse <http://netty.io/4.1/api/io/netty/handler/codec/http/FullHttpResponse.html>`_
with several helper methods to let you extract params and respond.

Javadoc:

* `Sinetja <http://sinetja.github.io/sinetja>`_
* `Netty <http://netty.io/4.1/api/io/netty/handler/codec/http/package-summary.html>`_

Reverse routing
~~~~~~~~~~~~~~~

To create reverse routing, you need to define routes using Action classes:

::

  import sinetja.Action;
  import sinetja.Request;
  import sinetja.Response;
  import sinetja.Server;

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
      Server server = new Server();

      server
        .GET("/",            IndexAction.class)
        .GET("/hello/:name", HelloAction.class);

      server.start(8000);
    }
  }

::

  server.uri(IndexAction.class)
  // => "/"

  server.uri(HelloAction.class, "name", "World")
  // => "/hello/World"

  // You can also use Map
  Map<String, String> params = new HashMap<String, String>();
  params.put("name", "World");
  server.uri(HelloAction.class, params)

Request methods
~~~~~~~~~~~~~~~

Common methods:
``CONNECT``, ``DELETE``, ``GET``, ``HEAD``, ``OPTIONS``,
``PATCH``, ``POST``, ``PUT``, and ``TRACE``.

You can also use ``ANY``, which means the route will match all request methods.

If you want to specify that a route should be matched first or last, use
``GET_FIRST``, ``GET_LAST`` etc.

::

  server
    .GET      ("/articles/:id", ShowAction.class)
    .GET_FIRST("/articles/new", NewAction.class)

Get request params
~~~~~~~~~~~~~~~~~~

Use these methods of `Request <http://sinetja.github.io/sinetja/sinetja/Request.html>`_:

::

  String       param(String name) throws MissingParam
  String       paramo(String name)
  List<String> params(String name)

``param`` and ``paramo`` return a single value.
``params`` returns a collection (can be empty) of values (params can be multiple values with same name).

If the request param is missing:

* ``paramo`` will just returns null.
* ``param`` will throw ``MissingParam``. By default, Sinetja will respond error
  400 bad request for you. If you want to change the default behavior, you can catch that
  exception in your action, or at the global error handler (see
  "500 Internal Server Error" section below).

Order of request param priority: path > body > query. For example, if the routing pattern is
``/hello/:foo``, when request ``/hello/abc?foo=xyz`` comes in, ``param("foo")`` will return ``abc``.

Respond
~~~~~~~

Use these methods of `Response <http://sinetja.github.io/sinetja/sinetja/Response.html>`_:

::

  respondText
  respondXml
  respondHtml
  respondJs
  respondJsonText
  respondJson
  respondJsonPText
  respondJsonP
  respondBinary
  respondFile
  respondEventSource

All the methods return `ChannelFuture <http://netty.io/4.1/api/io/netty/channel/ChannelFuture.html>`_.

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

Sinetja uses `SLF4J <www.slf4j.org>`_.
Please add an implementation like `Logback <http://logback.qos.ch/>`_ to your project.

::

  <dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.2</version>
  </dependency>

You can get a logger like this:

::

  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;

  public class MyClass {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyClass.class);
    ...
  }

Or if you don't care about the originality of the logger, simply use ``sinetja.Log``:

::

  import sinetja.Log;

  Log.info("Some info");


404 Not Found
~~~~~~~~~~~~~

If there's no matched action, Sinetja will automatically respond simple
"Not Found" text for you.

If you want to handle yourself:

Java 8 style:

::

  import io.netty.handler.codec.http.HttpResponseStatus;

  server.notFound((req, res) -> {
    String uri = req.getUri();
    Log.info("User tried to access nonexistent path: {}", uri);
    res.setStatus(HttpResponseStatus.NOT_FOUND);
    res.respondText("Not Found: " + uri);
  });

Class style:

::

  public class NotFound extends Action {
    public void run(Request req, Response res) {
      String uri = request.getUri();
      Log.info("User tried to access nonexistant path: {}", uri);
      res.setStatus(HttpResponseStatus.NOT_FOUND);
      res.respondText("Not Found: " + uri);
    }
  }

  server.notFound(NotFound.class);

500 Internal Server Error
~~~~~~~~~~~~~~~~~~~~~~~~~

By default, Sinetja will automatically respond simple
"Internal Server Error" text for you.

If you want to handle yourself:

Java 8 style:

::

  import io.netty.handler.codec.http.HttpResponseStatus;

  server.error((req, res, e) -> {
    Log.error("Error with request: {}", req, e);
    res.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    res.respondText("Internal Server Error");
  });

Class style:

::

  public class ErrorHandler extends ErrorHandler {
    public void run(Request req, Response res, Exception e) {
      Log.error("Error with request: {}", req, e);
      res.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
      res.respondText("Internal Server Error");
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

If you want to use your own certificate, use one of these methods:

::

  jdkSsl(String certChainFile, String keyFile)
  jdkSsl(String certChainFile, String keyFile, String keyPassword)

  openSsl(String certChainFile, String keyFile)
  openSsl(String certChainFile, String keyFile, String keyPassword)

The above are utility methods for setting `SslContext <http://netty.io/4.1/api/io/netty/handler/ssl/SslContext.html>`_.
If you want to set it directly:

::

   sslContext(SslContext sslContext)

CORS
~~~~

To tell the server to handle `CORS <http://en.wikipedia.org/wiki/Cross-origin_resource_sharing>`_,
set `CorsConfig <http://netty.io/4.1/api/io/netty/handler/codec/http/cors/CorsConfig.html>`_:

::

  import io.netty.handler.codec.http.cors.CorsConfig;

  CorsConfig config = CorsConfig.withAnyOrigin().build();
  server.cors(config);

New project skeleton
~~~~~~~~~~~~~~~~~~~~

* `Sinetja-Skeleton8 <https://github.com/sinetja/sinetja-skeleton8>`_:
  If you want to use Java 8 with its lambda syntax.
* `Sinetja-Skeleton <https://github.com/sinetja/sinetja-skeleton>`_:
  If you use older Java.
* `Sinetja-Scaleton <https://github.com/sinetja/sinetja-scaleton>`_:
  If you use Scala. Please also try `Xitrum <http://xitrum-framework.github.io/>`_.

`Discussion mailing list (Google group) <https://groups.google.com/group/sinetja>`_

Maven
~~~~~

::

  <dependency>
    <groupId>tv.cntt</groupId>
    <artifactId>sinetja</artifactId>
    <version>1.3.0</version>
  </dependency>

You should also add `Javassist <http://javassist.org/>`_ because it boosts Netty speed:

::

  <dependency>
    <groupId>org.javassist</groupId>
    <artifactId>javassist</artifactId>
    <version>3.21.0-GA</version>
  </dependency>
