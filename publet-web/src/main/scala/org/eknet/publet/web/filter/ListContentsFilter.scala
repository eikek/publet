package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import org.eknet.publet.engine.scalascript.com.twitter.json.Json
import org.eknet.publet.{Publet, Path}
import org.eknet.publet.resource._
import collection.mutable
import javax.servlet.{FilterChain, Filter}
import org.eknet.publet.web.{Config, WebContext}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 10.04.12 12:37
 */

class ListContentsFilter extends Filter with SimpleFilter {

  private val log = LoggerFactory.getLogger(getClass)

  private val resourceComparator = (r1: Resource, r2: Resource) => {
    if (r1.isContainer && !r2.isContainer) true
    else if (r2.isContainer && !r1.isContainer) false
    else r1.name.compareTo(r2.name) < 0
  }


  def doFilter(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
    WebContext().action match {
      case Some("list") => {
        val path = WebContext().decodePath
        val p = if (path.directory) path else path.parent
        val prefixPath = "/" + Config.mainMount // URLDecoder.decode(req.getContextPath, "UTF-8")
        val json = createJsonMap(p, WebContext().publet, prefixPath)
        Content(json.toString, ContentType.json).copyTo(resp.getOutputStream)
      }
      case _ => chain.doFilter(req, resp)
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
