package org.eknet.publet.web.template

import org.eknet.publet.Publet
import org.eknet.publet.vfs.util.{UrlResource, MapContainer, ClasspathContainer}
import org.eknet.publet.web.RequestUrl
import org.eknet.publet.vfs.{ResourceName, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 19:04
 */
object Templates {

  def mountJQuery(publet: Publet) {
    publet.mountManager.mount(Path("/publet/jquery/"),
      new ClasspathContainer(base ="/org/eknet/publet/web/includes/jquery"))
  }

  def mountHighlightJs(publet: Publet) {
    publet.mountManager.mount(Path("/publet/highlightjs/"),
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/highlight"))
  }

  def mountSticky(publet: Publet) {
    publet.mountManager.mount(Path("/publet/sticky/"),
      new ClasspathContainer(base = "/org/eknet/publet/web/includes/sticky"))
  }

  def mountPubletResources(publet: Publet) {
    val publetJs = new MapContainer()
    publetJs.addResource(new UrlResource(classOf[RequestUrl].getResource("includes/publet/js/publet.js"), ResourceName("publet.js")))
    publet.mountManager.mount(Path("/publet/js/"), publetJs)

    val publetCss = new MapContainer()
    publetCss.addResource(new UrlResource(classOf[RequestUrl].getResource("includes/publet/css/pygmentize.css"), ResourceName("pygmentize.css")))
    publet.mountManager.mount(Path("/publet/css/"), publetCss)

    val publTempl = new MapContainer()
    publTempl.addResource(new UrlResource(classOf[RequestUrl].getResource("includes/publet/templ/empty.ssp"), ResourceName("empty.ssp")))
    publTempl.addResource(new UrlResource(classOf[RequestUrl].getResource("includes/publet/templ/login.jade"), ResourceName("login.jade")))
    publTempl.addResource(new UrlResource(classOf[RequestUrl].getResource("includes/publet/templ/_messagepage.page"), ResourceName("_messagepage.page")))
    publet.mountManager.mount(Path("/publet/templates/"), publTempl)
  }
}
