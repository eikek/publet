package org.eknet.publet.web.filter

import org.eknet.publet.web.shiro.AuthcFilter
import org.eknet.publet.web.webdav.WebdavFilter
import javax.servlet.http.{HttpServletRequestWrapper, HttpServletRequest}
import org.eknet.publet.web.asset.AssetFilter

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.09.12 07:31
 * 
 */
object Filters {

  val blacklist = new BlacklistFilter
  val exceptionHandler = new ExceptionFilter
  val extensionRequest = new ExtensionRequestFilter
  val publet = new PubletFilter
  val redirect = new RedirectFilter
  val source = new SourceFilter
  val webContext = new WebContextFilter
  val authc = new AuthcFilter
  val webdav = new WebdavFilter
  val assets = new AssetFilter

  private[web] class ForwardRequest(uri: String, req: HttpServletRequest) extends HttpServletRequestWrapper(req) {
    import collection.JavaConversions._

    req.getAttributeNames.toList.withFilter(!_.contains("eclipse.jetty")).foreach(key => req.removeAttribute(key))

    override val getRequestURI = if (!uri.startsWith("/")) "/"+uri else uri
  }
}
