## Introduction

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

As said, [Scalate](http://scalate.fusesource.org/) is a template engine, thus
you can write templates: SSP (similiar to JSP), Jade, Mustache and more. All
those templates can contain Scala code and also wiki markup which allows for a
very flexible and comfortable web page design. If all that is too much, it's also
possible to write plain HTML files. Publet will serve html files without further
processing.

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
to the server. Publet will update its working copy after each push.