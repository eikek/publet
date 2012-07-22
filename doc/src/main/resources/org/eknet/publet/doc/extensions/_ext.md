# The _ext_ module

This module contains some nice-to-have extensions. It provides some templates
that could be of use as well as Scala singleton objects that aim to be of use
from within templates and custom scripts.

## Contact form

TODO

## Download Template

This is a predefined template that renders a download component. The component
is a table and each row represents a file. The click count and md5 checksum
are also rendered. (Note, the click-count is also available, if the
`CounterExtension` is active).

Include it in your `jade` page like this:

    - setAttribute("rootDir", Some("/some/other/path/"))
    =include("/publet/ext/includes/templates/_downloadTable.jade")

Here is an example screen:

![download table example](downloadTemplate.png)

You can control the resulting output by specifying following optional paramters:

* `rootDir`: (String) the path to a directory. all files within that directory
  are rendered for download. It defaults to the directory of the current page.
* `folderImagePath`: (String) an internal path to a image resource that is
  used to display next to folders
* `exclExtensions`: (Seq[String]) a set of file extensions. Those files are
  _excluded_ from rendering. The default list is `Seq("html", "jade", "md",
  "markdown", "page", "ssp")`.

By default, the template displays all files in the specified root directory
and all files located in all sub directories (not recursive, only one level).
At the top a simple link list is rendered that takes you to the named sub
folder.

The image above was rendered with a root directory with the following
structure

    .
    |-- 1.0.0
    |   |-- photozone-doc-1.0.0-manual.pdf
    |   `-- photozone-web-1.0.0.war
    |-- 1.0.1
    |   |-- photozone-doc-1.0.1-manual.pdf
    |   `-- photozone-web-1.0.1.war
    `-- 1.0.2
        |-- photozone-doc-1.0.2-manual.pdf
        `-- photozone-web-1.0.2.war


## Change Password and "my data" template

There exists a template that can be included that displays two forms: one for
changing the password and one that lets the current user change his name and
email.

This is only valid for authenticated users.

Include it in your `jade` template like this:

    =include("/publet/ext/includes/templates/_myData.jade")

By default, both forms are displayed horizontally using bootstraps `div`
classes (using class `span4` for both forms). By setting a parameter, you can
get the forms layout vertically:

    - setAttribute("layoutHorizontal", Some(false))
    =include("/publet/ext/includes/templates/_myData.jade")

Other parameters are

* `formAction`: (String) the url to the update script (optional)
* `renderResponseElement`: (Boolean) whether to render the div element that is used
  to render json responses. If this parameter is set to `false`, the div is not rendered.
  This way you can add a div element with id `myDataResponse` to the page manually (where
  you want to have it). (optional, defaults to `true`)
* `hashAlgorithm`: (String) the hash algorithm to be used when changing the password.
  (optional, defaults to _SHA-512_)


## Counter Extension

The counter extension is tracking some data for each request, namely the
number of visits of an URL and the last time it was accessed.

To avoid counting requests too often, the IP address is cached in memory (not
on disk!) for a specific amount of time (default is 2h). If the same IP hits
the same URL within this time frame, it is not counted. The data is saved
inside a graph database on the server.

The data is available via the `CounterService`. Retrieve it via
`CounterExtension.service`.


## Blueprints / OrientDB

The `ext` module is introducing a database to publet. It uses the
graph/document database [OrientDB](http://code.google.com/p/orient/) while the
code should be written against the
[Blueprints](http://blueprints.tinkerpop.com/) API that is part of the
[Tinkerpop stack](http://tinkerpop.com/).
[Blueprints](http://blueprints.tinkerpop.com/) provides a database-agnostic
API, that makes your code independent from the underlying graph database.


### API

The object `OrientDb` can be used to create new databases:

    /**
     * Creates a new [[com.tinkerpop.blueprints.impls.orient.OrientGraph]] instance.
     *
     * @param name
     * @return
     */
    def newGraph(name: String): OrientGraph

    /**
     * Creates a new [[org.eknet.publet.ext.orient.OrientDb]] with a new
     * instance of a [[com.tinkerpop.blueprints.impls.orient.OrientGraph]].
     *
     * @param name
     * @return
     */
    def newDatabase(name: String): OrientDb

Databases can only be created once on application startup. A listener is
registered that will shut down all Orient databases on application exit.

For the counter extension and for convenience, there is a database provided
you can use: `ExtDb`.