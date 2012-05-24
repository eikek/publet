package org.eknet.publet.webeditor.actions

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.vfs.Resource._
import org.eknet.publet.Publet
import collection.mutable
import org.eknet.publet.vfs._
import ScalaScript._
import org.eknet.publet.web.{PubletWeb, PubletWebContext}
import org.eknet.publet.webeditor.EditorPaths

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
    jsonMap
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
    val (contentType, size) = r match {
      case cr: ContentResource => (cr.contentType, cr.length.getOrElse(0L))
      case cr: ContainerResource => (ContentType.unknown, 0L)
    }
    Map(
      "name" -> r.name,
      "container" -> isContainer(r),
      "type" -> contentType.typeName.name,
      "sourceRef" -> (path / r).asString,
      "thumbnail" -> (EditorPaths.thumbNailer.asString+"?resource="+(path/r).asString),
      "delete_url" -> (EditorPaths.pushScript.asString+"?delete="+(path/r).asString),
      "href" -> ((path/ r.name.withExtension("html")).asString),
      "mimeBase" -> contentType.mime._1,
      "size" -> size
    )
  }
}
