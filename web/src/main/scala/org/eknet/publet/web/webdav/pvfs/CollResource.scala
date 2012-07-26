package org.eknet.publet.web.webdav.pvfs

import org.eknet.publet.vfs.Container
import collection.JavaConversions._
import org.eknet.publet.web.webdav.WebdavResource
import io.milton.resource.CollectionResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:07
 */
trait CollResource extends CollectionResource {
  this: DelegateResource[Container] =>

  def child(childName: String) = resource.child(childName).map(WebdavResource(_)).orNull

  def getChildren = resource.children.map(WebdavResource(_)).toList
}
