package org.eknet.publet.vfs.util

import scala.collection.mutable
import org.eknet.publet.vfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.04.12 21:26
 */
class MapContainer extends Container {

  private val map = mutable.Map[String, Resource]()
  private var lastMod: Option[Long] = None

  def children = map.values

  import ResourceName._

  def content(name: String) = map.get(name) match {
    case None => Resource.emptyContent(name.rn)
    case cc: ContentResource => cc
    case _ => sys.error("Not a content resource")
  }

  def container(name: String) = map.get(name) match {
    case None => Resource.emptyContainer(name.rn)
    case Some(cr: ContainerResource) => cr
    case _ => sys.error("Not a container resource")
  }

  def child(name: String) = map.get(name)

  def lastModification = lastMod

  def addResource(r: Resource) {
    map.put(r.name.fullName, r)
    lastMod = Some(System.currentTimeMillis())
  }

  def exists = true
}
