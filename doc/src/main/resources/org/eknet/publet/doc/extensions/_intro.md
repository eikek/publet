# Extensions

Extensions are packaged jar files that contain code, templates and other
resources that add extra functionality to publet. Publet is distributed with
some extensions already.

The _web-editor_, for example, is included by using the extension concept.

## The trait _WebExtension_

Extensions are created by extending the trait `WebExtension`. This is defined
as follows

    trait WebExtension {

      /**
       * Point on which extension code is executed
       * once per server start.
       *
       * It is ensured, that those are invoked
       * _after_ [[org.eknet.publet.web.PubletWeb]]
       * has been initialized.
       */
      def onStartup()

      /**
       * This method is invoked when the servlet container is
       * shutting down.
       *
       */
      def onShutdown()

      /**
       * This method is invoked on the begin of each request.
       *
       */
      def onBeginRequest()

      /**
       * This method is invoked on the end of each request.
       *
       */
      def onEndRequest()

    }

The `onStartup()` is often used to mount partitions (like `ClasspathContainer`
or `MapContainer`).

## Lookup mechanism

Publet will look for extensions on startup by using [Java's
ServiceLoader](http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html)
strategy.

You can prevent certain extensions from being loaded by specifying them in the
configuration file `publet.properties`. Please use the full class name of the
published web extension class with an associated value of `false`.

For example, if you don't want use the provided webeditor, specifiy the
following line in `publet.properties`:

    org.eknet.publet.webeditor.EditorWebExtension=false

That will prevent the webeditor from being loaded. By default all available
extensions are loaded.


## Installing Extensions

Extensions are installed by including them in the classpath. For a war file, that means using
the "exploded war" mode and dropping the jar file into `WEB-INF/lib`.

When using the standalone server, drop it in the directory `plugins`. You
might need to create it first (next to the `etc` directory).