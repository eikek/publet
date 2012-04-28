package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.vfs.Resource._
import org.eknet.publet.web.{Config, WebContext}
import org.eknet.publet.Publet
import collection.mutable
import org.eknet.publet.com.twitter.json.Json
import org.eknet.publet.vfs._
import ScalaScript._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:47
 */
object ListContents extends ScalaScript {

  private val resourceComparator = (r1: Resource, r2: Resource) => {
    if (isContainer(r1) && !isContainer(r2)) true
    else if (isContainer(r2) && !isContainer(r1)) false
    else r1.name.compareTo(r2.name) < 0
  }

  def serve() = {
    val ctx = WebContext()

    val path = ctx.parameter("path").map(Path(_)).getOrElse(ctx.decodePath)

    val prefixPath = WebContext().getContextPath match {
      case Some(cp) => cp + "/" + Config.mainMount
      case None => "/" + Config.mainMount
    }
    val p = WebContext.stripPath(if (path.directory) path else path.parent)
    val json = createJsonMap(p, WebContext().webPublet.publet, prefixPath)

    makeJson(json)
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

  private def container(path: Path, publet: Publet): List[_ <: Resource] = {
    def children(path: Path) = {
      publet.rootContainer.lookup(path) match {
        case None => List()
        case Some(r) => r match {
          case cr: ContainerResource => cr.children
          case _ => List()
        }
      }
    }
    children(path).toList.sortWith(resourceComparator)
  }

  private def resourceEntry(r: Resource, contextPath: String) = {
    val contentType = r match {
      case cr: ContentResource => cr.contentType.typeName.name
      case cr: ContainerResource => "unknown"
    }
    Map(
      "name" -> r.name,
      "container" -> isContainer(r),
      "type" -> contentType,
      "href" -> Path(contextPath + r.path.asString).asString
    )
  }
}
