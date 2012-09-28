package org.eknet.publet.web.filter

import org.eknet.publet.web.shiro.AuthcFilter
import org.eknet.publet.web.webdav.WebdavFilter

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
}
