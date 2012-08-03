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

    /.allIncludes/config/settings.properties

This should hold web application specific information. The settings file is
also honored by the default layout to retrieve default values.

The settings file can be accessed in scripts and templates using
`PubletWeb.publetSettings` variable which refers to an
`o.e.p.web.util.PropertiesMap` class. String values can be retrieved by
applying a string key, for example:

    val stringValue: Option[String] = PubletWeb.publetSettings("applicationName")


Note, the settings must be explcitely reloaded after making changes.


### Reference settings file

A (hopefully) complete `settings.properties` file is shown below.

<div p:ref="settings.html"></div>


## Configuration File

The location of the configuration file `publet.properties` depends on whether
you use the war file or the standalone server.

The configuration can be accessed in code using the object
`org.eknet.publet.web.Config`. This object is also of type `o.e.p.web.util.PropertiesMap`
(like the settings above) and string values can be accessed by applying a string key

    import org.eknet.publet.web.Config
    val stringValue: Option[String] = Config("webdav.enabled")

### War

Using the war file, the configuration file is expected at the root of the
`$PUBLET_DIR/<context-path>` of the war file. Please see the [install instructions]() for more information.


### Standalone Server

The file is expected in the `etc` directory.


### Reference configuration file

A (hopefully) complete `publet.properties` file is shown below.

<div p:ref="publet-cfg.html"></div>

## Logging

[Logback](http://logback.qos.ch/) is used as logging backend. It is configured via a
`logback.xml` configuration file that is expected next to the `publet.properties` file.

When using standalone server, this file should be in the `etc` directory. When using the
war file, this file is picked up in the `$PUBLET_DIR/<context-path>`. If no such file is
found, a default configuration similiar to this is used:

    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>

      <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type
             ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder>
          <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
      </appender>

      <logger name="org" level="INFO"/>
      <logger name="com" level="WARN"/>
      <logger name="com.bradmcevoy" level="INFO"/>
      <logger name="com.ettrema" level="INFO"/>

      <root>
        <appender-ref ref="STDOUT" />
      </root>

    </configuration>
