# Installation

Publet is distributed in two flavors:

1. as a war file. Just drop it in the `webapp` directory of some servlet container.
2. as a "standalone" zip file that has jetty bundled inside.

You can find packaged war/zip files on the [download
page](https://eknet.org/main/projects/publet/download.html). Alternatively you can
build those artifacts from sources as described [here](#Building_sources).

## Running

### WAR File

The war file only needs to be dropped in a servlet container's webapp
directory.

### Standalone

After downloading the zip file unpack it somewhere. Then start the `start.sh`
script or on Windows double-click the `start.bat` file.

    unzip publet-server-<version>.zip
    sh bin/start.dist.sh

Then point your browser to <http://localhost:8080/>. You can stop the server
by pressing `CTRL+C`.

Files in `etc` and `bin` always have an additional `dist` in their name. The
idea behind that is that you can copy the files to their correct name (without `dist`)
and make any modifications there. When updating the installation to a new version, your
changes are not overwritten.

By default the server port is set to `8080`. This is done via a system property
`-Dpublet.server.port=8080` within the `start.[bat|sh]` scripts. You can safely
remove this system property and adopt the `server.properties` file in `etc`
directory to your needs.

## Configuration

### WAR File

Publet expects a directory with read/write privieleges. This directory can be
specified by a system property `publet.dir` or by an environment variable
`PUBLET_DIR`. If that fails, the fallback is the directory `.publet` in the
current user's home directory. For the following, this directory is called
`$PUBLET_DIR`.

On start, publet creates a sub folder with the name of the context path. This
is just for the case that multiple publet war files are deployed to one
container. If the context path is not defined, the directory `root` is used.

The main configuration file `publet.properties` is expected in the root of
such a directory.

As an example, when using [Jetty](http://www.eclipse.org/jetty/) on Linux
create a file `/etc/default/jetty` and define the environment variable:

<pre>
export PUBLET_DIR=/var/lib/publet
</pre>

The file `/etc/default/jetty` is picked up by the jetty init script.

### Standalone

All configuration files are located in `etc` folder. There are pre-defined
files that have the word `dist` in their name. Please copy them to their
correct name and make modifications as desired.

Additionally there is a `server.properties` file that configures the jetty
integrated server.

Publet will save all data to the folder `var/root`, so this is the important
folder when doing backups.

The application can be started and stopped using the scripts in the `bin`
folder.

## Behind a proxy

A common scenario is to use a servlet container behind a proxy. By default,
publet generates links based on the current http request. But when used behind
a proxy this is not valid anymore.

You can specify a custom url base in the configuration file:

    publet.urlBase=https://my.server.com

Make sure, that it does not end with a slash. All links are then prefixed with
`https://my.server.com`.

## Building sources

Java 7 is required and Scala 2.9.2. [sbt](http://www.scala-sbt.org/) is used
as build tool. It will download the correct scala version automatically. To
compile type

    sbt compile

and to create a war file type

    sbt package

The war is then available in `war/target/scala-2.9.1/sbt-0.11.2/`. To create
the standalone zip file, type

    sbt server-dist

The zip file is available afterwards in `server/target`.
