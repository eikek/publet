package org.eknet.publet.resource

import org.eknet.publet.Path
import java.net.URL

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 23:01
 */
class ClasspathPartition(val id: Symbol, clazz: Class[_], root: Path) extends Partition {

  def lookup(path: Path) = Option(clazz.getResource((root / path).asString)) match {
    case None => None
    case Some(u) => Some(new UrlResource(u))
  }

  def children = List()

  def createContent(path: Path) = sys.error("Cannot create contents on class path partitions")

  def createContainer(path: Path) = sys.error("Cannot create contents on class path partitions")
}
