## Guice Modules

The different application components are wired using the [Guice](http://code.google.com/p/google-guice/)
dependency injection framework. Guice already offers an extension concept which is used here in conjunction
with [Java's ServiceLoader](http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html) pattern.
The guice "container" is an `Injector` that is created at  application start. This injector contains the default
bindings of the application and extensions can contribute other bindings easily by creating a module
that extends the `PubletModule` trait. This guice module implementation must then be published in a file inside
the `META-INF/services` directory of the extension jar.

Thus, to add a new module create a file

    META-INF/services/org.eknet.publet.web.guice.PubletModule

and add lines denoting the full classname of your modules. The module must have a no-arg constructor
in order to get instantiated. There is another trait `PubletBindings` that defines some helper
methods for creating bindings specific to publet. The injector can be used to inject or create new instances, but most
usually you would just bind your objects in the module implementation.

That means, that all such provided modules are automatically added to the injector. If you want to
prevent certain modules from being loaded, specify them in the configuration file `publet.properties`.
Please use the full class name of the published module class with an associated value of `false`. For
example, you could exclude the webeditor by specifying

    org.eknet.publet.webeditor.WebeditorModule=false

in the configuration file.

This is the only way to contribute to the application, but contributing new guice bindings allows to
hook into the application at various places. The [Hooks](ext_hooks.html) section describes some of the
possibilities.
