# Configuration

There are two configuration files:

* `publet.properties` - this file is on the server and is supposed to hold
  configuration values that are specific to the server instance
* `settings.properties` - this file is inside the content root (thus
  accessible via git) and specifies configuration (or settings) that are
  specific to the running application


## Settings

An optional `settings.properties` can be created in the contentroot repository
at

    /.allIncludes/settings.properties

This should hold web application specific information. The settings file is
also honored by the default layout to retrieve default values.

The settings file can be accessed in scripts and templates using
`PubletWeb.publetSettings` variable which refers to an
`o.e.p.web.util.PropertiesMap` class. String values can be retrieved by
applying a string key, for example:

    val stringValue: Option[String] = PubletWeb.publetSettings("applicationName")


Note, the settings must be explcitely reloaded after making changes.

The following properties are known to publet:

* `applicationName` used sometimes to refer to the application.
* `publet.useHighlightJs` whether to activate [HighlightJS](http://softwaremaniacs.org/soft/highlight/en/)
* `publet.highlightTheme` the global [HighlightJS](http://softwaremaniacs.org/soft/highlight/en/) theme to use
* `publet.searchForSidebar` whether to search the include directories for a `sidebar` file
* `publet.searchForHeadIncludes` whether to search for addtional html head includes (like javascript and css files)

