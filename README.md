# Publet (discontinued)

Publet is a web application written in [Scala](http://www.scala-lang.org/) where you can edit
pages like in a wiki. Besides three wiki languages, you can also write Scala scripts and choose
from a broad set of template languages to not only create static sites, but also dynamic sites
conveniently. Mainly, it glues together three great open-source components:

* [Scalate](http://scalate.fusesource.org/) which is a template engine
* [Git](http://git-scm.com/), using the java implementation [JGit](http://www.jgit.org/)
* [Bootstrap]() for a great ui

By using Scalate you can use markdown and textile wiki markup and create templates
using one of their [supported template languages](http://scalate.fusesource.org/documentation/index.html#Templates). 
Git can be used to hold all source files, which allows to edit the contents
offline and push changes back to the server. Also, it is possible to have multiple repositories. Finally 
Bootstrap is used for the default page layout creating a great look and feel. 

Publet aims to be an extensible application which makes setting up dynamic
web sites more convenient. Use wiki markup on text heavy pages, and use templates 
combined with Scala code for more complex views. It's also possible to create
Scala Scripts (the controller part) that can return any content, like for example 
JSON for interoperating with Javascript.

## Building

Java 7 is required and Scala 2.9.x. [sbt](https://github.com/harrah/xsbt) is used
as build tool. To compile type

    sbt compile

and to create a war file and standalon zip use

    sbt server-dist

The war is then available in `war/target/scala-2.9.1/sbt-0.11.2/`, the zip file
in `server/target/`.

## Install

The war file can be dropped in any servlet container. All it needs is
a directory with write privileges. By default it searches the system property 
`publet.dir`, then the environment variable `PUBLET_DIR` and if that also 
fails, it falls back to `$USER/publet`. 

When starting up, a new directory is created using the context path 
of the webapp (to have multiple wars on one container).

## More

The full story can be viewed [here](https://eknet.org/main/projects/publet/index.html)
