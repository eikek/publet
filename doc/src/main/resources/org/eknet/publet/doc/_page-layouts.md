# Layouts

[Layout templates](http://scalate.fusesource.org/documentation/user-guide.html#layouts)
are used to define html page skeletons. They may provide a `<head/>` section
with common css and js files and meta information, header and footer parts
etc.

Each template/wiki page can specify which layout to be wrapped in. If nothing
is specified the default layout is used.


## The default layout

Publet is shipped with [Bootstrap](http://twitter.github.com/bootstrap/) and
provides a default layout template that is wrapped around each page (if not
specified otherwise).

The default layout defines a html head section with the following includes

* All [Bootstrap](http://twitter.github.com/bootstrap/) `css` and `js` files
* code highlighter [highlightJs](http://softwaremaniacs.org/soft/highlight/en/) that
  automatically highlights text enclosed in `<pre><code>` tags
* [JQuery](http://jquery.com/) with two plugins
    * [JQuery forms plugin](http://jquery.malsup.com/form/)
    * [JQuery File Upload Plugin](http://blueimp.github.com/jQuery-File-Upload/)

The file upload plugin is only included when showing the page to upload files.

Pages can control the layout's behaviour by defining parameters:

* `title: String` the page title (default is "Welcome")
* `head: String` additional html head elements (default is `null`)
* `nav: String` the navigation bar (default is `null`)
* `footer: String` a page footer (default is `null`)
* `searchForSidebar: Boolean` whether to search for a sidebar file in the
  _special directories_ (default is `true`)
* `searchForHeadIncludes: Boolean` whether to search for additional head
  includes in _special directories_ (default is `true`)
* `useHighlightJs: Boolean` whether to use HighlightJS (default is `true`)
* `highlightTheme: String` the theme for highlightJs (the css file without
  extension; default is "googlecode")
* `layout: String` the uri to the layout to use. Default is `null` which
  results in using the default layout

See the full source of the default layout [here](../../../publet/bootstrap/bootstrap.single.jade).

### Html head includes

The default layout includes any `js`, `css` and `meta` files from the _special directories_.
The `js` and `css` files are obvoius. the `meta` files are XML files that define other
elements that should go into the html head, like `<meta/>` tags, for example.

If you like to override some css, just place your css file into `/.allIncludes/mytheme.css`,
for example, and it is included in every page.

### Navigation bar

The parameter `nav` is containing HTML code to render contents of the
navigation bar at the top of the page. If no value is specified for this
parameter, publet will look up a template file with name `nav` in the
_special directories_.

The initial content provides a basic outline of the navigation bar that is
stored at `.allIncludes/nav.jade`.

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
page right away: [see this page without layout](?noLayout)
