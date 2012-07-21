# Scala Scripts

It is also possible to write Scala scripts. They are executed on each request
and are supposed to act as a controller. The code is embedded in the trait
`org.eknet.publet.engine.scala.ScalaScript` which is defined as:

    trait ScalaScript extends Logging {

      def serve(): Option[Content]

    }

There exists some handy methods to create content objects. The
`renderTemplate()` method can be used to create html by rendering a template.
The method expects an uri that names the template and an optional map of
attributes.

Another quite common scenario is to return JSON, usually when interacting with
javascript. The method `makeJson()` can be used for that. It will convert a
scala object into JSON using [this JSON
serializer](https://github.com/twitter/scala-json/blob/master/src/main/scala/com/twitter/json/Json.scala)

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
imported by default and defines some helper methods for checking permissions.
Then the parameter is evaluated and the corresponding action is executed. The
output will be a short JSON response that the client will handle.

Go to `/.allIncludes/config/admin.html` to reload something...


## Mini projects

Scala scripts can access all classes in the classpath of the web application.
Often, one wants to define addtional objects and classes to share code between
scripts. You can extend the source and classpath of Scala scripts to achieve
it.

Suppose there exists several scripts in `/apps/myapp/` that want to share
code. You can create a _mini project_ by creating the following directory
outline:

    /apps/myapp/.includes/project/src/main/scala/
    /apps/myapp/.includes/project/lib/

The `project` folder contains the mini project files. The `lib` directory can
contain jar files that are added to the classpath when compiling and the
sourcce directory is also added to the compiler classpath. Note, since the
mini project applies only to scripts in `/apps/myapp` and below, because it is
defined in the `.includes` folder at that level.
