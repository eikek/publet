package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import com.twitter.json.Json
import org.eknet.publet.resource.{ContentType, ContainerResource, ContentResource, Resource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.04.12 00:43
 */
object ListContentsFilter extends Filter with FilterContext {

  private val log = LoggerFactory.getLogger(getClass)

  private val resourceComparator = (r1: Resource, r2: Resource) => {
    if (r1.isContainer && !r2.isContainer) true
    else if (r2.isContainer && !r1.isContainer) false
    else r1.name.compareTo(r2.name) < 0
  }

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    action(req) match {
      case Some("list") => {
        val out = resp.getOutputStream
        val json = container(req).map(resourceEntry).mkString("[", ", ", "]")

//        resp.setContentType("text/json")
//        resp.setCharacterEncoding("UTF-8")
        out.println("{ \"files\": "+ json +"}")
        out.flush()
        true
      }
      case _ => false
    }
  }

  private def container(req: HttpServletRequest): List[_ <: Resource] = {
    val p = if (path(req).hasExtension) path(req).parent
    else path(req)
    log.info("Children for path: "+ p)
    if (p.isRoot) publet(req).children(p).toList.sortWith(resourceComparator)
    else publet(req).lookup(p.parent).get :: publet(req).children(p).toList.sortWith(resourceComparator)
  }
  
  private def resourceEntry(r: Resource) = {
    val templ = "{ \"name\": %s, \"container\": %s, \"type\": %s }"
    val contentType = r match {
      case cr: ContentResource => cr.contentType.typeName.name
      case cr: ContainerResource => "unknown"
    }
    String.format(templ, Json.quote(r.name),
      Json.quote(String.valueOf(r.isContainer)),
      Json.quote(contentType))
  }
}
