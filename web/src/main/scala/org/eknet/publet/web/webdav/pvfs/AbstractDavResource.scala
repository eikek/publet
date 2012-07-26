package org.eknet.publet.web.webdav.pvfs

import io.milton.http.{Auth, Request}
import java.util
import org.eknet.publet.web.PubletWeb
import io.milton.resource.{PropFindableResource, Resource}

/**
 * Implements the milton `Resource` interface based on nothing.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.06.12 22:46
 */
abstract class AbstractDavResource extends Resource with PropFindableResource with Authorized {

  def getUniqueId: String = null

  def getName: String = ""

  def getRealm: String = PubletWeb.publetSettings("webdav.realmName").getOrElse("WebDav Area")

  def getModifiedDate: util.Date = null

  def checkRedirect(request: Request): String = null

  def getCreateDate: util.Date = null
}
