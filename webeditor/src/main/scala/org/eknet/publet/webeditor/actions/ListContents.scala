/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
object ListContents extends ScalaScript with ThumbnailUrlUtil {

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
    val ctx = PubletWebContext
    Map(
      "name" -> r.name,
      "container" -> isContainer(r),
      "type" -> contentType.typeName.name,
      "sourceRef" -> (path / r).asString,
      "thumbnail" -> thumbnailUrl(path, r),
      "delete_url" -> ctx.urlOf(EditorPaths.pushScript.asString+"?delete="+(path/r).asString),
      "href" -> ctx.urlOf(path/ r.name.withExtension("html")),
      "mimeBase" -> contentType.mime._1,
      "size" -> size
    )
  }
}
