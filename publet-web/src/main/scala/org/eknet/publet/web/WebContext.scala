package org.eknet.publet.web

import scala.collection.JavaConversions._
import scala.collection._
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import javax.servlet.http.HttpServletRequest
import org.eknet.publet.{Path, Publet}
import java.net.URLDecoder

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.04.12 12:09
 */
trait WebContext {

  def uploads: List[FileItem]

  def session: AttributeMap

  def request: AttributeMap

  def context: AttributeMap

  /** Returns all parameters of the
   * request.
   *
   * @return
   */
  def parameters: Map[String, List[String]]

  /**
   * Gets the parameter value to the given name. If
   * more than one value exists, the first is returned.
   * 
   * @param name
   * @return
   */
  def parameter(name: String): Option[String]

  /** Returns the value associated to the given key
   * in `context` scope.
   *
   * If no value exists, the `init` function of the
   * key is used to create a initial value. The `init`
   * function is mandatory.
   *
   * The value is added to the servlet context
   * attribute map to live across sessions and
   * requests.
   *
   * @param key
   * @tparam T
   * @return
   */
  def service[T : Manifest](key: Key[T]): T = {
    Predef.ensuring(key.init.isDefined, "no init defined")
    context.get(key).get
  }

  /** Returns the path to this request.
   *
   * Strips the context-path if present.
   *
   * @return
   */
  def decodePath: Path

  /** Returns the path to this request.
   *
   * If the path ends in a directory, the default file `index.html`
   * is appended.
   *
   * @return
   */
  def requestPath: Path

  def publet: Publet

  /** Returns the value of the `action`
   * request parameter.
   *
   * @return
   */
  def action: Option[String]
}

object WebContext {
  protected[web] val publetKey = new Key[Publet]("publet")
  
  private val params = new ThreadLocal[WebContext]()
  
  protected[web] def setup(req: HttpServletRequest) {
    params.set(new WebContextImpl(req))
  }

  def apply(): WebContext = params.get()

  def clear() {
    params.remove()
  }

  private class WebContextImpl(req: HttpServletRequest) extends WebContext {
    lazy val uploads: List[FileItem] = {
      val rct = req.getContentType
      if (rct != null && rct.startsWith("multipart")) {
        val items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
        items.collect({case p:FileItem => p}).filter(!_.isFormField).toList
      } else {
        List[FileItem]()
      }
    }

    lazy val session = new SessionMap(req.getSession)

    lazy val request = new RequestMap(req)

    lazy val context = new ContextMap(req.getSession.getServletContext)

    lazy val parameters = {
      val buf = mutable.Map[String, List[String]]()
      val pmap = req.getParameterMap
      for (s <- pmap.keySet()) {
        val v = pmap.get(s).asInstanceOf[Array[_]].toList.map(_.asInstanceOf[String])
        buf.put(s.asInstanceOf[String], v)
      }
      buf.toMap
    }

    def parameter(name: String) = Option(req.getParameter(name))

    lazy val publet = context.get(WebContext.publetKey) match {
      case None => throw new RuntimeException("Servlet not setup")
      case Some(x) => x
    }

    lazy val action = parameter("a")

    def decodePath: Path = {
      val p = Path(URLDecoder.decode(req.getRequestURI, "UTF-8"))
      if (Option(req.getContextPath).map(!_.isEmpty).getOrElse(false)) {
        p.strip(Path(req.getContextPath))
      } else {
        p
      }
    }

    def requestPath = {
      val p = decodePath
      if (p.directory) p / "index.html" else p
    }
  }
}



