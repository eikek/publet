# Wiki

Publet is just another wiki. If a resource is requested, that does not
exist, a edit or upload form is presented in order to create that page.
It is possible to use [markdown](http://daringfireball.net/projects/markdown/),
[textile](http://en.wikipedia.org/wiki/Textile_%28markup_language%29) and
[confluence](https://confluence.atlassian.com/display/DOC/Confluence+Wiki+Markup) wiki
markup, because all that is supported by [Scalate](http://scalate.fusesource.org/).

The path of the resources is reflected at the file system on the server. If
you create a new page `/articles/today/news.md`, this will be saved at the
server on the same path (directories `articles` and `today` are created
if necessary).

Scalate detects on the file extension how to process the file. An extension
`md` or `markdown`, for example, triggers the markdown engine, while the
extension `textile` would trigger the textile engine. You have to name the
files according to their content.

If you just care about that, you might want to read the first section
about security, and that's it.

## Using Git

By using git, you can edit contents offline and push changes back to the
server. The content repository is located at `/git/contentroot.git`. If
publet is installed at `http://localhost:8080/publet` you can check out
the repository using

    git clone http://localhost:8080/publet/git/contentroot.git

To have the changes reflected immediately you need to publish them to
the `master` branch.


# Advanced Content

Since the whole transformation is backed by Scalate,
[it's documentation](http://scalate.fusesource.org/documentation/index.html) is
well worth a look! Scalate is a template engine written in Scala, supporting multiple
template languages. Templates can be mocked up with Scala code.
That gives a great amount of flexibility when creating pages.

If you want to go beyond simple wiki pages, take a look at the scalate
documentation on how to write templates. The *developer* section of this
manual gives some hints on existing objects and classes as well as examples
to get started.

## Hidden files

With the hidden files convention you can create files that will not
be served. Any request to them will always result in a http 404 error. The idea
is to include them or call them from Scala scripts only.

A hidden file is any file that contains a segment starting with an underscore `_`
character. For example, the file `_userdoc.md` from the example above is
hidden. A directory starting with `_` would hide any files below it.


## Special Directories

For implementing some useful default behaviour, there exist two special
directories:

* `.allIncludes/` only at the root of the repository
* `.includes/` at any folder

These are places where publet looks for resources to use in favor for
any defaults. The `.allIncludes` directory exists only once at the
root of the repository. It contains things that are global to the whole
application. The `.includes/` directory, on the other hand, may appear
in any directory in the content tree and applies only to resources in
its parent directory and to all resources below that do not have their
own `.includes`.

When looking up exactly one file from those directories, files in the `.includes/`
are always preferred. At last the `.allIncludes/` directory is tried.

When looking up more than one file, all matching files from `.allIncludes` and
`.includes/` are combined.

The following sections explain their use.


## Layouts

[Layouts](http://scalate.fusesource.org/documentation/user-guide.html#layouts) are
used to define a html page skeletons. They may provide a `<head/>` section with
common css and js files and meta information, header and footer parts etc. Each
template page can specify which layout to be wrapped in. If nothing is specified
the default layout is used.

## The default layout

Publet provides a default layout that is based on [Bootstrap](http://twitter.github.com/bootstrap/index.html).
The layout includes all bootstrap css and javascript files and the code highlighter [highlightJs](http://softwaremaniacs.org/soft/highlight/en/).
It also includes [JQuery](http://jquery.com/) and the [JQuery forms plugin](http://jquery.malsup.com/form/).

A template file can define the following parameters to the default layout:

* `title: String` the page title (default is "Welcome")
* `head: String` additional html head elements (default is `null`)
* `nav: String` the navigation bar (default is `null`)
* `footer: String` a page footer (default is `null`)
* `searchForSidebar: Boolean` whether to search for a sidebar file in the _special directories_ (default is `true`)
* `searchForHeadIncludes: Boolean` whether to search for additional head includes in _special directories_ (default is `true`)
* `useHighlightJs: Boolean` whether to use HighlightJS (default is `true`)
* `highlightTheme: String` the theme for highlightJs (the css file without extension; default is "googlecode")
* `layout: String` the uri to the layout to use. Default is `null` which results in using the default layout

For example, this page is does the following

    ---
    title: Publet Documentation

    --- name:content pipeline:jade
    .row
      .offset1.span10
        =include("_userdoc.md")

It sets the page title to "Publet Documentation". Then the body consists of two nested
`div` elements where the second includes the content of the markdown file `_userdoc.md`.
This has to be saved to a file with extension `page`.

You can checkout the complete source of the default layout [here](../../../publet/bootstrap/bootstrap.single.jade).


### Html head includes

The default layout includes any `js`, `css` and `meta` files from the _special directories_.
The `js` and `css` files are obvoius. the `meta` files are XML files that define other
elements that should go into the html head, like `<meta/>` tags, for example.

If you like to override some css, just place your css file into `/.allIncludes/mytheme.css`,
for example, and it is included in every page.

### Navigation bar

The default layout will include a the contents from a template `nav` that is looked up
from the special directories. It renders the contents of the black bar on the top.


### Sidebar

The default layout will search the special directories for a file with name `sidebar`. If
one exists, the page is divided into two columns, where the right column renders the
`sidebar` and the main (left) column renders the page body.

## Custom layouts

You can easily create a custom layout and place it anywhere in the content tree. Then use
the parameter `layout` in your templates and define the uri to your new layout.

You can also define a new default layout by saving your layout template to `/.allIncludes/pageLayout.*`.
Publet will always look for a file  `/.allIncludes/pageLayout.*` first before falling back
to the bootstrap layout.

To explicitely use no layout at all, just define the `layout` parameter with an
empty string `""`. Besides that, you can add a request parameter `noLayout` that will
do the same (if the request goes to a template, of course). You can try it with this
page right away: [this page without layout](?noLayout)

## Javascript includes

Using the `include()` function in templates, you can include other templates on the server
side. It's also possible to include content via ajax at the client. A tiny javascript
function is executed on each page that looks for tags with a `p:ref` attribute. If found,
it triggers an ajax request to the URL obtained from the `p:ref` attribute and adds
the returned html inside that element. The parameter `noLayout` is added automatically
to the request.

# Security

Publet implements a simple security strategy using [Apache Shiro](http://shiro.apache.org).

Resources are always inside a git repository. A repository can be _open_ or _closed_. An
open repository can be cloned by anybody, while closed repositories need explicit _pull_
permission for cloning and any other read access. Write access will always be protected by
checking an explicit _push_ permission.

Furthermore, restriction on resources can be defined.

These rules are specified in one xml file on a specific location

    /.allIncludes/config/permissions.xml

If this file is not present, a realm with one user called _superadmin_ is created. The
default password of this user is also _superadmin_, but can be specified in the
configuration file with property "superadminPassword". The _superadmin_ always has
all privileges.

Here is an example `permissions.xml` file explained:

    <publetAuth>
      <users>
        <user login="jdoe" password="098f6bcd4621d373cade4e832627b4f6" algorithm="md5">
          <fullName>John Doe</fullName>
          <email>email@host.com</email>
          <group>admin</group>
          <group>moderator</group>
        </user>
      </users>

      <repositories>
        <repository name="contentroot" tag="closed"/>
      </repositories>

      <resourceConstraints>
        <pattern name="/main/.allIncludes/config/*" perm="configure"/>
        <pattern name="/main/projects/*" perm="moderate"/>
        <pattern name="/main/slides/*" perm="moderate"/>
        <pattern name="/main/*" perm="anon"/>
        <pattern name="/publet/doc/*" perm="moderate"/>
      </resourceConstraints>

      <permissions>
        <grant name="PUSH">
          <on>contentroot</on>
          <to>admin</to>
        </grant>
        <grant name="moderate">
          <to>moderator</to>
        </grant>
        <grant name="configure">
          <to>admin</to>
        </grant>
      </permissions>
    </publetAuth>


Note, the file is inside the repository, so you can edit it like any
other resource. But modifications are not reflected until a manual
`reload()` is invoked on the authentication manager.

## User Database

The `<users/>` tag represents the user database. Each user is specified with
login and password. The password can be plain text or encrypted using one of
JDK's supported algorithms. In the example file, I created a md5 encrypted
password using the command

    echo -n "test" | md5sum

The `<group/>` tag specified the roles or groups the user belongs to. Permissions
can only be associated with groups.

## Repositories

The only repository seen so far is `contentroot`. But there may exists more
repositories. The tag `<repositories/>` defines the state for each. Any
repository not listed is always an _open_ repository. In the example the
`contentroot` is defined to be a closed repository. Access to a closed repository
will always be checked. That means that no anonymous user could now browse
a page anymore.

## Resource constraints

By specifying `<resourceConstraints/>` definitions, access to single resources can
be defined. An uri pattern is associated to a permission string. Each request
uri is matched against the list of uri patterns. If one matches (the first wins,
so order matters), the associated permission is checked against the current
subject.

The line

    <pattern name="/main/.allIncludes/config/*" perm="configure"/>

restricts access to all resources below `/main/.allIncludes/config/` to users
with the permission `configure`.

If you want to open resources, use the special permission string `anon`. If
this permission is found, the request handling resumes without access checks.

The line

    <pattern name="/main/*" perm="anon"/>

would allow access to any resource below `/main` to anybody.

## Permissions

Permissions are simple strings and associated to a group. You sould read
the [shiro documentation about wildcard permissions](http://shiro.apache.org/permissions.html).

There exist two predefined permissions: `push` and `pull`. They are usually
restricted to a specific repository, which is specified using the `<on/>`
tag. Internally the wildcard permission `pull:<repositoryname>` is created.

You can define any permission you like using the `<grant/>` tag.


# Settings

An optional `settings.properties` can be created in the contentroot repository
at `/.allIncludes/settings.properties`. This should hold web application specific
information, like the global application name. The settings file is also honored
by the default layout to retrieve default values.

The settings file can be accessed in scripts and templates using `PubletWeb.publetSettings`
variable which refers to an `o.e.p.web.util.PropertiesMap` class. String values can be retrieved by
applying a string key, for example:

    val stringValue: Option[String] = PubletWeb.publetSettings("applicationName")


Note, the settings must be explcitely reloaded after making changes.

The following properties are known to publet:

* `applicationName` used sometimes to refer to the application.
* `publet.useHighlightJs` whether to activate [HighlightJS](http://softwaremaniacs.org/soft/highlight/en/)
* `publet.highlightTheme` the global [HighlightJS](http://softwaremaniacs.org/soft/highlight/en/) theme to use
* `publet.searchForSidebar` whether to search the include directories for a `sidebar` file
* `publet.searchForHeadIncludes` whether to search for addtional html head includes (like javascript and css files)



# Controllers

It is also possible to write Scala scripts. They are executed on each
request and are supposed to act as a controller. The code is embedded
in the trait `org.eknet.publet.engine.scala.ScalaScript` which is defined as

    trait ScalaScript extends Logging {

      def serve(): Option[Content]

    }

There exists some handy methods to create content. The `renderTemplate()` method
can be used to create html by rendering a template. The method expects an uri
that names the template and an optional map of attributes.

Another quite common scenario is to return JSON, usually when interacting with
javascript. The method `makeJson()` can be used for that. It will convert a
scala object into JSON using [this JSON serializer](https://github.com/twitter/scala-json/blob/master/src/main/scala/com/twitter/json/Json.scala)

You need to give the file the extension `scala`.


## Example: Admin page

This example creates a simple page that allows to reload the configuration,
settings and permissions file.

At first, we create a template for the new page at `/.allIncludes/config/admin.page`:

    ---
    title: Admin

    --- name:content pipeline:jade

    - import org.eknet.publet.web.Config
    h1 Admin
    br/
    #response
    table.table.table-condensed
      tr
        td Server config dir
        td =Config.repositories.getParentFile.getAbsolutePath
      tr
        td Reload Configuration:
        td
          button.btn.btn-primary(href="#" id="config") Reload Configuration
      tr
        td Reload Settings
        td
          button.btn.btn-primary(href="#" id="settings") Reload Settings
      tr
        td Reload Permissions
        td
          button.btn.btn-primary(href="#" id="permissions") Reload Permissions
    :javascript
      $(function() {
        $('button').each(function(i, el) {
          $(el).on('click', function() {
            $.get("reload.json", { what: $(el).attr("id") }, function(result) {
              var closeIcon = '<a class="close" data-dismiss="alert" href="#">×</a>';
              if (result.success) {
                $('#response').html('<div class="alert alert-success">'+result.message + closeIcon + '</div>');
              } else {
                $('#response').html('<div class="alert alert-error">' +result.message + closeIcon + '</div>');
              }
            });
          });
        });
      });

This displays a simple table and registers javascript click handlers that will execute an
ajax request to the url `reload.json` with one request parameter called `what`. The value
will be the id of the button that has been clicked.

The reload functionality is implemented in a Scala script `/.allIncludes/config/reload.scala`:

    Security.checkPerm("configure")
    PubletWebContext.param("what") match {
      case Some("config") => {
        Config.reload();
        makeJson(Map("success"->true, "message"->"Configuration reloaded!"))
      }
      case Some("settings") => {
        PubletWeb.publetSettings.reload();
        makeJson(Map("success"->true, "message"->"Settings reloaded!"))
      }
      case Some("permissions") => {
        PubletWeb.authManager.reload()
        makeJson(Map("success"->true, "message"->"Permissions reloaded!"))
      }
      case _ => {
        makeJson(Map("success"->false, "message"->"Don't know what to reload!"))
      }
    }

At first a special permission `configure` is checked. The `Security` object is
imported by default and defines some helper methods for checking permissions. Then
the parameter is evaluated and the corresponding action is executed. The output will
be a short JSON response that the client will handle.

Go to `/.allIncludes/config/admin.html` to reload something...


## Mini projects

Scala scripts can access all classes in the classpath of the web application. Often,
one wants to define addtional objects and classes to share code between scripts. You
can extend the source and classpath of Scala scripts to achieve it.

Suppose there exists several scripts in `/apps/myapp/` that want to share code.
You can create a _mini project_ by creating the following directory outline:

    /apps/myapp/.includes/project/src/main/scala/
    /apps/myapp/.includes/project/lib/

The `project` folder contains the mini project files. The `lib` directory can contain jar
files that are added to the classpath when compiling and the sourcce directory is also
added to the compiler classpath. Note, since the mini project applies only to scripts in
`/apps/myapp` and below, because it is defined in the `.includes` folder at that level.
