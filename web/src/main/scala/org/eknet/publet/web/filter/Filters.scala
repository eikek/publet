package org.eknet.publet.web.filter

import org.eknet.publet.web.shiro.AuthcFilter
import org.eknet.publet.web.webdav.WebdavFilter
import javax.servlet.http.{HttpServletRequestWrapper, HttpServletRequest}
import org.eknet.publet.web.asset.AssetFilter
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.PubletWebContext
import com.google.inject.servlet.GuiceFilter
import org.eknet.publet.web.guice.ExtensionManager
import com.google.common.eventbus.EventBus

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.09.12 07:31
 * 
 */
object Filters {

  val blacklist = new BlacklistFilter
  val exceptionHandler = new ExceptionFilter
  def extensionRequest(extman: ExtensionManager) = new ExtensionRequestFilter(extman)
  val publet = new PubletFilter
  val redirect = new RedirectFilter
  val source = new SourceFilter
  val webContext = new WebContextFilter
  val authc = new AuthcFilter
  val webdav = new WebdavFilter
  val assets = new AssetFilter
  val guice = new GuiceFilter

  private[web] class ForwardRequest(uri: String, req: HttpServletRequest, clearAttributes: Boolean = true) extends HttpServletRequestWrapper(req) {
    import collection.JavaConversions._

    if (clearAttributes) {
      req.getAttributeNames.toList.withFilter(!_.contains("eclipse.jetty")).foreach(key => req.removeAttribute(key))
    } else {
      req.removeAttribute("applicationUri")
    }

    override val getRequestURI = if (!uri.startsWith("/")) "/"+uri else uri
  }

  def forwardRequest(path: Path, clearAttributes: Boolean = true) =
    new ForwardRequest(PubletWebContext.req.getContextPath+path.asString, PubletWebContext.req, clearAttributes)
}
