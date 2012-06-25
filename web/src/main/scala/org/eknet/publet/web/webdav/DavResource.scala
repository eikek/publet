package org.eknet.publet.web.webdav

import com.bradmcevoy.http.{PropFindableResource, Auth, Request, Resource}
import com.bradmcevoy.http.Request.Method
import org.eknet.publet.vfs
import java.util

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 22:57
 */
trait DavResource extends Resource with PropFindableResource {

  def resource: vfs.Resource

  def getUniqueId = null
  def getName = resource.name.fullName
  def authenticate(user: String, password: String) = null
  def authorise(request: Request, method: Method, auth: Auth) = true
  def getRealm = ""
  def getModifiedDate = resource.lastModification.map(new java.util.Date(_)).orNull
  def checkRedirect(request: Request) = null
  def getCreateDate: util.Date = null
}
