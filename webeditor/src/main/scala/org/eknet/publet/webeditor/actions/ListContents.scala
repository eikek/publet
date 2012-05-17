package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.vfs.Resource._
import org.eknet.publet.Publet
import collection.mutable
import org.eknet.publet.com.twitter.json.Json
import org.eknet.publet.vfs._
import ScalaScript._
import org.eknet.publet.web.{PubletWeb, PubletWebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 19:47
 */
object ListContents extends ScalaScript {

  def serve() = {
    val ctx = PubletWebContext

    val path = ctx.param("path").map(Path(_)).getOrElse(ctx.applicationPath)
    val p = if (path.directory) path else path.parent
    val json = createJsonMap(p, PubletWeb.publet)

    makeJson(json)
  }

  private def createJsonMap(path: Path, publet: Publet) = {
    val jsonMap = mutable.Map[String, Any]()
    jsonMap.put("containerPath", path.asString)
    jsonMap.put("files", container(path, publet).map(resourceEntry(path, _)))
    if (!path.isRoot) {
      jsonMap.put("parent", path.parent.asString)
    }
    Json.build(jsonMap)
  }

  private def container(path: Path, publet: Publet): List[_ <: Resource] = {
    def children(path: Path) = {
      publet.rootContainer.lookup(path) match {
        case Some(r: ContainerResource) => r.children
        case _ => List()
      }
    }
    children(path).toList.sortWith(resourceComparator)
  }

  private def resourceEntry(path: Path, r: Resource) = {
    val contentType = r match {
      case cr: ContentResource => cr.contentType.typeName.name
      case cr: ContainerResource => "unknown"
    }
    Map(
      "name" -> r.name,
      "container" -> isContainer(r),
      "type" -> contentType,
      "href" -> (path / r).asString
    )
  }
}
