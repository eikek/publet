package org.eknet.publet.web.webdav.pvfs

import java.util
import org.eknet.publet.web.webdav.WebdavResource
import com.bradmcevoy.http.{Request, PropFindableResource, Resource}

/**
 * Implements the milton `Resource` interface based on nothing.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 22:46
 */
abstract class AbstractDavResource extends Resource with PropFindableResource with Authorized {

  def getUniqueId: String = null

  def getName: String = ""

  def getRealm: String = WebdavResource.getRealmName

  def getModifiedDate: util.Date = null

  def checkRedirect(request: Request): String = null

  def getCreateDate: util.Date = null
}
