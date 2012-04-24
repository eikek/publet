package org.eknet.publet.web

import scala.collection.JavaConversions._
import scala.collection._
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import javax.servlet.http.HttpServletRequest
import org.eknet.publet.{Path, Publet}
import java.net.URLDecoder
import org.eknet.publet.partition.git.GitPartition
import shiro.PubletAuthManager
import util._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.04.12 12:09
 */
trait WebContext {

  def uploads: List[FileItem]

  def session: AttributeMap

  def request: AttributeMap

  def context: AttributeMap

  def apply[T:Manifest](key:Key[T]): Option[T]

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
   * key is used to create a initial value.
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
  private val params = new ThreadLocal[WebContext]()

  private def request: HttpServletRequest = WebContext().asInstanceOf[WebContextImpl].req

  val gitpartitionKey = Key[GitPartition]("gitPartition")
  val publetAuthManagerKey = Key[PubletAuthManager]("publetAuthManager")
  val publetKey = Key[Publet]("publet")

  /**The url up to the path that starts the resource path.
   *
   * This is the host, port and the mountpoint.
   */
  val contextUrl = Key("contextUrl", {
    case Session => synchronized {
      // Config has preference
      Config("publet.contextUrl").getOrElse {
        Settings("publet.contextUrl").getOrElse {
          val uriRegex = "https?://[^:]+(:\\d+)?" + request.getContextPath
          uriRegex.r.findFirstIn(request.getRequestURL.toString).get
        }
      }
    }
  })

  /**The url to the current request without parameters.
   *
   */
  val requestUrl = Key("requestUrl", {
    case Request => {
      val url = Option(request.getPathInfo)
      WebContext()(contextUrl).get + url
    }
  })

  /**The mount path for the wiki.
   *
   */
  val mainMount = Key("publet.mainMount", {
    case Request => {
      Config("publet.mainMount").getOrElse("main")
    }
  })


  protected[web] def setup(req: HttpServletRequest) {
    params.set(new WebContextImpl(req))
  }

  def apply(): WebContext = Option(params.get).getOrElse(sys.error("No webcontext installed."))

  def apply[T:Manifest](key:Key[T]): Option[T] = WebContext()(key)

  def clear() {
    params.remove()
  }

  private class WebContextImpl(val req: HttpServletRequest) extends WebContext {
    lazy val uploads: List[FileItem] = {
      val rct = req.getContentType
      if (rct != null && rct.startsWith("multipart")) {
        val items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
        items.collect({case p:FileItem => p}).filter(!_.isFormField).toList
      } else {
        List[FileItem]()
      }
    }

    lazy val session = AttributeMap(req.getSession)

    lazy val request = AttributeMap(req)

    lazy val context = AttributeMap(req.getSession.getServletContext)

    def apply[T: Manifest](key: Key[T]) = {
      request.get(key).orElse {
        session.get(key).orElse {
          context.get(key)
        }
      }
    }

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

    lazy val publet = service(publetKey)

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



