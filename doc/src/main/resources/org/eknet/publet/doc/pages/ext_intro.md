## Extensions

Extensions are packaged jar files that contain code, templates and other
resources that add extra functionality to publet. Publet is distributed with
some extensions already.

The _web-editor_, for example, is included by using the extension concept.

In order to create new extensions, you need to know a little about how components
interact. Please read the [Guice](ext_guice.html) and then the [Hooks](ext_hooks.html)
sections for more.

### Installing Extensions

Extensions are installed by including them in the classpath. For a war file, that means using
the "exploded war" mode and dropping the jar file into `WEB-INF/lib`.

When using the standalone server, drop it in the directory `plugins`. You
might need to create it first (next to the `etc` directory).

If the extenions classes are on the classpath, they're loaded on startup by default. If you
want to exclude certain modules, add an entry to publet's configuration file like

    org.eknet.publet.ext.jmx.JmxConnectorModule=false

The key is the full classname of the module (this is shown at the top of each module
documentation page) with an associated value of `false`.

The publet documentation itself is an extension using the module class `org.eknet.publet.doc.PubletDocModule`.
If you exclude this module, no documentation is not loaded.
