Sinetja = Sintra + Netty + Java

Example
~~~~~~~

::

  package sinetja;

  public class IndexAction extends Action {
    public void execute() {
      respondText("Hello Sinetja");
    }
  }

  public class HelloAction extends Action {
    public void execute() {
      String name = param("name");
      respondText("Hello " + name");
    }
  }

  public class NotFoundAction extends Action {
    public void execute() {
      // Demo about log
      String uri = request.getUri();
      log.info("User tried to access nonexistant path: {}", uri);

      // Response status has already been set to 404 Not Found by Sinetja
      respondText("404 Not Found: " + uri);
    }
  }

  public App {
    public static void main(String[] args) {
      new Server()
        .GET("/",            IndexAction.class)
        .GET("/hello/:name", HelloAction.class)
        .handler404(NotFoundAction.class)
        .start(8000);
    }
  }

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

::

  ChannelFuture respondText(Object text)
  ChannelFuture respondText(ByteBuf buf)

Reverse routing
~~~~~~~~~~~~~~~

TODO

Log
~~~

Sinetja uses `SLF4J <www.slf4j.org>`_.
Please add an implementation like `Logback <http://logback.qos.ch/>`_ to your project.

In your actions, you can call:

::

  log.info("Some info");

From other places, you can use ``sinetja.Log``:

::

  import sinetja.Log;
  Log.info("Some info");

Use with Maven
~~~~~~~~~~~~~~

::

  <dependency>
    <groupId>tv.cntt</groupId>
    <artifactId>sinetja</artifactId>
    <version>1.0</version>
  </dependency>

Together with Netty, Sinetja also adds `Javassist <http://javassist.org/>`_ as
a dependency, because it boosts Netty speed.

Sinetja uses `netty-router <https://github.com/xitrum-framework/netty-router>`_.
