package org.eknet.publet.webapp.assets

import org.eknet.publet.content.{ClasspathPartition, Partition, Content}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 13.05.13 18:45
 */
abstract class AssetCollection {

  private val groups = collection.mutable.ListBuffer[Group]()

  implicit def toAsset(c: Content) = Asset(c)
  implicit def toContent(a: Asset) = a.resource

  protected def make(group:  => Group) = {
    val g = group
    groups.append(g)
    g
  }

  def toList = groups.toList

  def partition: Partition = new ClasspathPartition(base = classPathBase)

  def classPathBase = ""

  def resource(name: String): Asset = partition.find(name).collect({ case c: Content => c })
    .getOrElse(sys.error(s"Unable to find resource $name"))

}

object AssetCollection {
  implicit def collectionToList(ac: AssetCollection) = ac.toList
}