## Scala Scripts

Besides writing templates and wiki pages, it is also possible to write Scala
scripts. They are executed on each request. The code is embedded in the trait
`ScalaScript` which is defined as:

    trait ScalaScript extends Logging {

      def serve(): Option[Content]

    }

Thus the body of a script must return a value of type `Option[Content]`. There exists
some handy methods to create content objects in `org.eknet.publet.web.util.RenderUtils`,
which is imported by default. For example, the `renderTemplate()` method can be used to
create html by rendering a template. The method expects an uri that names the template
and an optional map of attributes.

Another quite common scenario is to return JSON, usually when interacting with
javascript. The method `makeJson()` can be used for that. It will convert a
scala object into JSON using [this JSON
serializer](https://github.com/twitter/scala-json/blob/master/src/main/scala/com/twitter/json/Json.scala)

You need to give the file the extension `scala`.

### Mini projects

Scala scripts can access all classes in the classpath of the web application.
Often, one wants to define addtional objects and classes to share code between
scripts. You can extend the sourcepath and classpath of Scala scripts to
achieve it.

Suppose there exists several scripts in `/apps/myapp/` that want to share
code. You would create a _mini project_ by creating the following directory
outline:

    /apps/myapp/.includes/project/src/main/scala/
    /apps/myapp/.includes/project/lib/

The `project` folder contains the mini project files. The `lib` directory can
contain jar files that are added to the classpath when compiling and the
sourcce directory is also added to the compiler classpath. Note, since the
mini project applies only to scripts in `/apps/myapp` and below, because it is
defined in the `.includes` folder at that level.


### Startup Scripts

On startup, the special directory `.allIncludes/startup/` is scanned for scala
source files. The source files are then compiled and expected to comply to the
following conditions:

* each file contains one class
* the class name is the same as the file name

After compiling the classes are instantiated via guice' injector (and are therefore
registered at publet's event bus).

This can be useful to do some initial setup tasks. For example, the `AssetManager`
can be injected to register other resources, or scripts can listen for any events
and do some further work (send mails etc).

### Example: Admin page

This example creates a simple page that allows to reload the configuration.

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
    hr/

    :javascript
     $(function() {
       $('button').each(function(i, el) {
         var button = $(el);
         button.on('click', function() {
            button.mask().attr("disabled", "disabled");
            $.get("reload.json", { what: button.attr("id") }, function(result) {
              button.unmask().removeAttr("disabled");
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

This displays a simple table and registers javascript click handlers that will
execute an ajax request to the url `reload.json` with one request parameter
called `what`. The value will be the id of the button that has been clicked.

The reload functionality is implemented in a Scala script
`/.allIncludes/config/reload.scala`:

    Security.checkPerm("configure")
    PubletWebContext.param("what") match {
      case Some("config") => {
        Config.reload();
        makeJson(Map("success"->true, "message"->"Configuration reloaded!"))
      }
      case _ => {
        makeJson(Map("success"->false, "message"->"Don't know what to reload!"))
      }
    }

At first a special permission `configure` is checked. The `Security` object is
imported by default and defines some helper methods for checking permissions.
Then the parameter is evaluated and the corresponding action is executed. The
output will be a short JSON response that the client will handle.
