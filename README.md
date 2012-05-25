# Publet

Publet is a wiki web application written in [Scala](http://www.scala-lang.org/)
that glues together three major players

* [Scalate](http://scalate.fusesource.org/) which is a template engine
* [Git](http://git-scm.com/), using the java implementation [JGit](http://www.jgit.org/)
* [Bootstrap]() for a great ui

By using Scalate you can use markdown and textile wiki markup and create templates
using one of their [supported template languages](http://scalate.fusesource.org/documentation/index.html#Templates). 
Git is used to hold all source files, which allows to edit the contents 
offline and push changes back to the server. Also, it is possible to have multiple repositories. Finally 
Bootstrap is used for the default page layout creating a great look and feel. 

Publet aims to be an extensible application which makes setting up dynamic
web sites more convenient. Use wiki markup on text heavy pages, and use templates 
combined Scala code for more complex view creation. It's also possible to create
Scala Scripts (the controller part) that can return any content, like for example 
JSON. 

## Building

Publet uses [sbt](https://github.com/harrah/xsbt) as build tool. To compile used

    sbt compile

and to create a war file used

   sbt package

The war is then available in `war/target/scala-2.9.1/sbt-0.11.2/`.

## Install

The war file can be dropped in any servlet container. All it needs is
a directory with write privileges. By default it searches the system property 
`publet.dir`, then the environment variable `PUBLET_DIR` and if that also 
fails, it falls back to `$USER/publet`. 

When starting up, a new directory is created using the context path 
of the webapp (to have multiple wars on one container).

## More

The full story can be viewed [here](http://eknet.org/main/projects/publet/index.html)