package org.eknet.publet.web

import filter.SuperFilter
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import java.net.URLDecoder
import org.slf4j.LoggerFactory
import org.eknet.publet._
import resource.{ContentType, Content, FilesystemPartition}
import scala.collection.JavaConversions._
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import java.io.File

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 27.03.12 22:42
 */
class PubletServlet extends HttpServlet {

  private val log = LoggerFactory.getLogger(getClass)
  private val filter = SuperFilter()

  override def init() { createPublet }
  def createPublet = {
    val config = getServletConfig
    val publetRoot = Option(config.getInitParameter("publetRoot"))
      .getOrElse(sys.error("No publet root specified!"))

    val app = getServletContext
    val publet = PubletFactory.createPublet()
    if (publetRoot.startsWith(File.separator)) {
      log.info("Initialize publet root to: "+ publetRoot)
      publet.mount(Path.root, new FilesystemPartition(publetRoot, 'publetroot))
    } else {
      val np = new java.io.File(".").getAbsolutePath+ File.separator+ publetRoot
      log.info("Initialize publet root to: "+ np)
      publet.mount(Path.root, new FilesystemPartition(np, 'publetroot))
    }
    app.setAttribute("publet", publet)
    publet
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    filter.handle(req, resp)
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    filter.handle(req, resp)
  }
}
