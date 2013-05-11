# Publet 2

Publet2 is a complete new implementation of the ideas in publet1. It is now build using 
[akka](http://akka.io) and connected to http utilising the [spray](http://spray.io) toolkit.

Think of publet as a webserver together with a set of conventions. For example, if the resource
`index.html` is requested, it is looked up in the content tree. If it is not found, publet tries
to look for resources with the same base name -- by looking up `index.*`. If there exists some, it
is assumed that they provide the requested content but are presented in the wrong format. There 
may be the file `index.md`, which is a [markdown](http://daringfireball.net/projects/markdown/) file.
 Publet now tries to find a converter  that can produce the requested content from the source 
file(s). For markdown files, there is a converter registered, and thus after converting it the 
requested `index.html` can be delivered to the client. 

While being a webserver is the intended usage of publet, it is itself just an actor and can therefore
be integrated in other actor systems. It is [spray](http://spray.io) which is providing the web server
on top of the publet actor.

Publet aims to provide just enough to be a simple wiki-like web application:

* a converter that utilises [scalate](http://scalate.fusesource.org) to
  create html from different kind of source formats.
* a converter that compiles and loads `scala` files (which are expected to procude
  some data).
* besides attaching directories on the server or resources from the classpath, local
  [git](http://git-scm.org) repositories can also be hooked into the content tree. publet
  will update its working copy  on `git push`. This is provided by [jgit](http://www.jgit.org/).

Everything else can be easily added via *plugins*. Plugins are objects that are loaded with the
publet actor (they are declared in the configuration file) and receive the actor system and the
`ActorRef` of the publet actor. They can now create more actors, register new converter or add 
resources to the content tree. Plugins can specify to depend on other plugins. Publet will load
them in topological order.

## Modules

Publet consists at its base of two modules:

* **content** this is the library that defines the "vocabulary". There are the following main entities:
  _Engines_ (or converter), _Partitions_, _Resources_ and _Sources_. A _Source_ is just some data
  with an associated content type. The _Resource_ adds a name to it. A _Partition_ gives access to 
  its _Resources_. They can be mounted to certain paths, which finally make up the whole content tree
  (just like on unix/linux systems). At last, the engine is a function that can convert a given
  _Source_ to a given target type.
* **actor** this is the base module defining the publet actor and its set of messages.

Then there is a set of plugins that add useful things for webapps:

* **scalate** a converter engine that uses [scalate](http://scalate.fusesource.org) for 
  converting many source formats into html
* **gitr** by using [jgit](http://www.jgit.org/), it provides a _Partition_ based on git
  repositories
* **assets** provides a central place to register "asset" resources, like javascript or css files which
  can then be requested sorted and/or in compressed form.
* ...
* **webapp** this module finally combines all the above to an application.

## Building

Java 7 is required and Scala 2.10.x. [sbt](https://github.com/harrah/xsbt) is used
as build tool. To compile type

    sbt compile

## Older version

Publet1 is still alive, now in the branch `1.x`.


## More

The full story can be viewed [here](https://eknet.org/main/projects/publet/index.html)
