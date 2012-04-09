package org.eknet.publet.web

import org.eknet.publet.engine.PubletEngine
import org.slf4j.LoggerFactory
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import org.eknet.publet.resource._
import com.twitter.json.Json
import org.eknet.publet.{Publet, Path}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 08.04.12 14:22
 */

class ListEngine(publet: Publet) extends PubletEngine {
  
  def name = 'list

  def process(path: Path, data: Seq[Content], target: ContentType) = {
    val json = container(path).map(resourceEntry).mkString("[", ", ", "]")
    Right(Content("{ \"files\": "+ json+ "}", ContentType.json))
  }

  private val log = LoggerFactory.getLogger(getClass)

  private val resourceComparator = (r1: Resource, r2: Resource) => {
    if (r1.isContainer && !r2.isContainer) true
    else if (r2.isContainer && !r1.isContainer) false
    else r1.name.compareTo(r2.name) < 0
  }

  private def container(path: Path): List[_ <: Resource] = {
    val p = if (path.hasExtension) path.parent else path
    log.info("Children for path: "+ p)
    if (p.isRoot) publet.children(p).toList.sortWith(resourceComparator)
    else publet.lookup(p.parent).get :: publet.children(p).toList.sortWith(resourceComparator)
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
