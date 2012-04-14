package org.eknet.publet.resource

import org.eknet.publet.Path
import org.eknet.publet.impl.Conversions._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 23:01
 */
class ClasspathPartition(val id: Symbol, clazz: Class[_], root: Path) extends Partition {

  def lookup(path: Path) = Option(clazz.getResource((root / path).asString)) match {
    case None => None
    case Some(u) => Some(new UrlResource(Some(u)))
  }

  def children = List()

  def content(name: String) = new UrlResource(Option(clazz.getResource((root/name).asString)))

  def container(name: String) = null

  def child(name: String) = lookup(root/name).get

  def hasEntry(name: String) = clazz.getResource((root/name).asString) != null

  def newContainer(path: Path) = throwException("Classpath partitions does not support containers.")

  def newContent(path: Path) = new UrlResource(Option(clazz.getResource((root/path).asString)))

  override def toString = clazz +"/"+ root
}
