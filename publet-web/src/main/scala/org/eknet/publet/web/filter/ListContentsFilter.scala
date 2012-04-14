package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import org.eknet.publet.engine.scalascript.com.twitter.json.Json
import org.eknet.publet.{Publet, Path}
import org.eknet.publet.resource._
import java.net.URLDecoder
import collection.mutable
import org.eknet.publet.web.WebContext

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 10.04.12 12:37
 */

object ListContentsFilter extends Filter {
  private val log = LoggerFactory.getLogger(getClass)
  private val resourceComparator = (r1: Resource, r2: Resource) => {
    if (r1.isContainer && !r2.isContainer) true
    else if (r2.isContainer && !r1.isContainer) false
    else r1.name.compareTo(r2.name) < 0
  }

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    WebContext().action match {
      case Some("list") => {
        val path = WebContext().decodePath
        val p = if (path.directory) path else path.parent
        val contextPath = URLDecoder.decode(req.getContextPath, "UTF-8")
        val json = createJsonMap(p, WebContext().publet, contextPath)
        Content(json.toString, ContentType.json).copyTo(resp.getOutputStream)
        true
      }
      case _ => false
    }
  }

  private def createJsonMap(path: Path, publet: Publet, contextPath: String) = {
    val jsonMap = mutable.Map[String, Any]()
    jsonMap.put("containerPath", path.asString)
    jsonMap.put("files", container(path, publet).map(resourceEntry(_, contextPath)))
    if (!path.isRoot) {
      jsonMap.put("parent", path.parent.asString)
    }
    Json.build(jsonMap)
  }

  private def container(path: Path, publet:Publet): List[_ <: Resource] = {
    publet.children(path).toList.sortWith(resourceComparator)
  }

  private def resourceEntry(r: Resource, contextPath: String) = {
    val contentType = r match {
      case cr: ContentResource => cr.contentType.typeName.name
      case cr: ContainerResource => "unknown"
    }
    Map(
      "name" -> r.name,
      "container" -> r.isContainer,
      "type" -> contentType,
      "href"-> Path(contextPath + r.path.asString).asString
    )
  }
}
