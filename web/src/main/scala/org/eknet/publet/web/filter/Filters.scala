package org.eknet.publet.web.filter

import org.eknet.publet.web.shiro.AuthcFilter
import javax.servlet.http.{HttpServletRequestWrapper, HttpServletRequest}
import org.eknet.publet.web.asset.{AssetManager, AssetFilter}
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.{WebExtension, Settings, Config}
import org.eknet.publet.Publet
import com.google.common.base.Throwables

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.09.12 07:31
 * 
 */
object Filters {

  val blacklist = new BlacklistFilter
  val exceptionHandler = new ExceptionFilter
  def extensionRequest(webext: java.util.Set[WebExtension]) = new ExtensionRequestFilter(webext)
  def publet(p: Publet) = new PubletFilter(p)
  def redirect(config: Config, settings: Settings) = new RedirectFilter(config, settings)
  def source(p: Publet) = new SourceFilter(p)
  val webContext = new WebContextFilter
  val authc = new AuthcFilter
  def assets(assetMgr: AssetManager) = new AssetFilter(assetMgr)

  private[web] class ForwardRequest(uri: String, req: HttpServletRequest, clearAttributes: Boolean = true) extends HttpServletRequestWrapper(req) {
    import collection.JavaConversions._

    if (clearAttributes) {
      req.getAttributeNames.toList.withFilter(!_.contains("eclipse.jetty")).foreach(key => req.removeAttribute(key))
    } else {
      req.removeAttribute("applicationUri")
    }

    override val getRequestURI = if (!uri.startsWith("/")) "/"+uri else uri
  }

  def forwardRequest(req: HttpServletRequest, path: Path, clearAttributes: Boolean = true) =
    new ForwardRequest(req.getContextPath+path.asString, req, clearAttributes)

  /**
   * Tries to find out whether this exception is a "broken pipe" exception
   * where the client closed the connection (pressing reload multiple times
   * for example).
   *
   * @param e
   * @return
   */
  def findSocketClosed(e: Throwable): Option[Throwable] = {
    val root = Throwables.getRootCause(e)
    root.getMessage match {
      case x if (x.contains("broken pipe")) => Some(root)
      case _ => None
    }
  }
}
