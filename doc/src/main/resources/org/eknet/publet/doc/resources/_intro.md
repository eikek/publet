# Introduction

Publet is a web application where you can edit pages like in a wiki. Besides three
wiki languages, you can also write Scala scripts and choose from a broad set of
template languages to not only create static sites, but also dynamic sites conveniently
and *"on the fly"*.

Publet offers three wiki markup languages:

* [markdown](<http://daringfireball.net/projects/markdown/>)
* [textile](<http://textile.thresholdstate.com/>) and
* [confluence](<https://confluence.atlassian.com/display/DOC/Confluence+Wiki+Markup>)

This is achieved by utilising [Scalate](http://scalate.fusesource.org/), a
template engine that supports those three major wiki markup languages. Publet
is handing wiki pages to [Scalate](http://scalate.fusesource.org/) which
produces the HTML output that is finally displayed.

Many wiki applications allow to extend the markup by using macros. That is
quite handy for adding simple dynamic content. Still, when a more complex page
is desired, the wiki syntax with many macros feels cumbersome. The macros are
often hard to read and are most often specific to the current application. In
those situations I often found it better to have the other way around: don't
use wiki markup with macros, but use a template language and insert wiki
markup in the appropriate places. Now, that's exactly what
[Scalate](http://scalate.fusesource.org/) offers!

As said, [Scalate](http://scalate.fusesource.org/) is a template engine, thus
you can write templates: SSP (similiar to JSP), Jade (very cool for creating
html!), Mustache and more. All those templates can contain Scala code and also
wiki markup which allows for a very flexible and comfortable web page design.
If all that is too much, it's also possible to write plain HTML files. Publet
will serve html files without further processing.

Publet adds the ability to create Scala scripts which are compiled and
executed by publet. The scripts can return content of any type, like HTML,
JSON or images. With this server-side scripts and the templating mechanism
provided by [Scalate](http://scalate.fusesource.org/), publet is actually
providing an environment for creating a custom web application. For easier
startup publet comes with [Bootstrap](http://twitter.github.com/bootstrap/)
that offers a lot of attractive client side functionality. Since
[Bootstrap](http://twitter.github.com/bootstrap/) inclusion is based on a
[Scalate](http://scalate.fusesource.org/) template, this can be replaced
easily.

By default, resources are maintained in a git repository. This gives you
versioning "for free". While you could edit all text resources online, you
can also clone the git repository edit files offline and push changes back
to the server. Publet will update its working copy after each push. A possible
workflow could be to install publet locally, add/edit files with your favorite
editor and verify the result locally. The collected changes can then be
pushed to the server via git.

Publet can be seen as a wiki for developers. The more interesting features are
targeted at users that like to create (dynamic) web sites. In this respect,
the git repository is a major part. If publet is used to create templates,
scala and javascript code, a version control system is mandatory.