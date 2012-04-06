package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.slf4j.LoggerFactory
import org.eknet.publet.Path
import com.twitter.json.Json
import org.eknet.publet.resource.{ContentType, ContainerResource, ContentResource, Resource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.04.12 00:43
 */
object ListContentsFilter extends Filter with FilterContext {

  private val log = LoggerFactory.getLogger(getClass)

  def handle(req: HttpServletRequest, resp: HttpServletResponse) = {
    action(req) match {
      case Some("list") => {
        val out = resp.getOutputStream
        val json = container(req).toList.sortWith((r1, r2) => {
          if (r1.isContainer && !r2.isContainer) true 
          else if (r2.isContainer && !r1.isContainer) false
          else r1.name.compareTo(r2.name) < 0
        }).map(resourceEntry).mkString("[", ", ", "]")

        resp.setContentType("text/json")
        resp.setContentLength(json.length())
        resp.setCharacterEncoding("UTF-8")
        out.println(json)
        out.flush()
        true
      }
      case _ => false
    }
  }

  private def container(req: HttpServletRequest): Iterable[_ <: Resource] = {
    val p = if (path(req).hasExtension) path(req).parent
    else path(req)
    log.info("Children for path: "+ p)
    publet(req).children(p)
  }
  
  private def resourceEntry(r: Resource) = {
    val templ = "{ name: %s, container: %s, type: %s }"
    val contentType = r match {
      case cr: ContentResource => cr.contentType.typeName.name
      case cr: ContainerResource => "unknown"
    }
    String.format(templ, Json.quote(r.name),
      Json.quote(String.valueOf(r.isContainer)),
      Json.quote(contentType))
  }
}
