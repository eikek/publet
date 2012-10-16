package org.eknet.publet.webdav.pvfs

import org.eknet.publet.vfs.Resource
import java.util

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.06.12 09:51
 */
trait ResourceProperties extends AbstractDavResource {
  this: DelegateResource[Resource] =>

  override def getName = {
    val fn = resource.name.fullName
    //fixme: right now the trailing slash indicates a directory. this is
    //stupid and leads to silly names for folders.
    if (fn.endsWith("/")) fn.substring(0, fn.length-1)
    else fn
  }

  override def getModifiedDate = resource.lastModification.map(new util.Date(_)).orNull
}
