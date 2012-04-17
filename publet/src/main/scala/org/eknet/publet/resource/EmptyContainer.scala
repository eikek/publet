package org.eknet.publet.resource

import org.eknet.publet.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.04.12 22:37
 */
class EmptyContainer(val path: Path) extends ContainerResource {

  def parent = None

  def lastModification = None

  def name = if (path.isRoot) path.asString else path.segments.last

  def isRoot = path.isRoot

  def isWriteable = false

  def exists = false

  def delete() { throw new RuntimeException("Cannot delete non-existent resource.") }

  def create() { throw new RuntimeException("Cannot create resource.") }

  def children = List()

  def content(name: String) = null

  def container(name: String) = null

  def child(name: String) = null

  def hasEntry(name: String) = false
}

class EmptyContent(val path: Path) extends ContentResource {
  def parent = Some(new EmptyContainer(path.parent))

  def name = path.segments.last

  def isWriteable = false

  def exists = false

  def delete() { throw new RuntimeException("Cannot delete non-existent resource.") }

  def create() { throw new RuntimeException("Cannot create resource.") }

  def outputStream = None

  def length = None

  def contentType = ContentType.unknown

  def inputStream = throw new RuntimeException("no input available")

  def lastModification = None
}
