package org.eknet.publet.web.webdav.pvfs

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 23:04
 */
trait DelegateResource[+A] {

  def resource: A

}