package org.eknet.publet.web

import filter.SuperFilter
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import org.slf4j.LoggerFactory
import org.eknet.publet._
import resource.FilesystemPartition
import java.io.File

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 27.03.12 22:42
 */
class PubletServlet extends HttpServlet {

  private val log = LoggerFactory.getLogger(getClass)
  private val filter = SuperFilter()

  override def init() {
    publetRootParam match {
      case Some(dir) => System.setProperty("publet.dir", dir)
      case None =>
    }
  }

  protected def publetRootParam = {
    val config = getServletConfig
    Option(config.getInitParameter("publetRoot")) match {
      case None => None
      case Some(publetRoot) => {
        if (publetRoot.startsWith(File.separator)) {
          log.info("Initialize publet root to: "+ publetRoot)
          Some(publetRoot)
        } else {
          val np = new java.io.File(".").getAbsolutePath+ File.separator+ publetRoot
          log.info("Initialize publet root to: "+ np)
          Some(np)
        }
      }
    }
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    WebContext.setup(req)
    try {
      filter.handle(req, resp)
    } catch {
      case t:Throwable => log.error("Error for "+ req.getRequestURI, t)
    }
    WebContext.clear()
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    WebContext.setup(req)
    try {
      filter.handle(req, resp)
    } catch {
      case t:Throwable => log.error("Error for "+ req.getRequestURI, t)
    }
    WebContext.clear()
  }
}
