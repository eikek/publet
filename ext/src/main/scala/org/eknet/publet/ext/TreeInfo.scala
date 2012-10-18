package org.eknet.publet.ext

import org.eknet.publet.vfs.{ContentResource, Resource, ContainerResource, Path}
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.util.{PubletWebContext, PubletWeb}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.06.12 15:53
 */
object TreeInfo {

  private def getChildren(path: Path) = {
    PubletWeb.publet.rootContainer.lookup(path)
      .collect({ case r: ContainerResource => r})
      .map(_.children)
      .getOrElse(List())
      .collect({ case r if (Security.hasReadPermission((path / r).toAbsolute.asString)) => r})
  }

  def children(path: Path): List[_ <: Resource] = {
    getChildren(path)
      .toList
      .sortWith(Resource.resourceComparator)
  }

  def children: List[_ <: Resource] = children(PubletWebContext.applicationPath.parent)

  def folders(path: Path): List[ContainerResource] = {
    getChildren(path)
      .collect({ case c: ContainerResource => c})
      .toList
      .sortWith(Resource.resourceComparator)
  }

  def folders: List[ContainerResource] = folders(PubletWebContext.applicationPath.parent)

  def files(path: Path): List[ContentResource] = {
    getChildren(path)
      .collect({case f:ContentResource => f})
      .toList
      .sortWith(Resource.resourceComparator)
  }

  def files: List[ContentResource] = files(PubletWebContext.applicationPath.parent)

  def filesWithExtensions(path: Path, ext: Seq[String]): List[ContentResource] = {
    val set = ext.map(_.toLowerCase).toSet
    files(path).filter(c => {
      set.contains(c.name.ext.toLowerCase)
    })
  }

  def filesWithExtensions(ext: Seq[String]): List[ContentResource] =
    filesWithExtensions(PubletWebContext.applicationPath.parent, ext)

  def filesWithoutExtensions(path: Path, ext: Seq[String]): List[ContentResource] = {
    val set = ext.map(_.toLowerCase).toSet
    files(path).filterNot(c => {
      set.contains(c.name.ext.toLowerCase)
    })
  }

  def filesWithoutExtensions(ext: Seq[String]): List[ContentResource] =
    filesWithoutExtensions(PubletWebContext.applicationPath.parent, ext)
}
