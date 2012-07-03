package org.eknet.publet.web.webdav.pvfs

import com.bradmcevoy.http.{PropFindableResource, Auth, Request, Resource}
import com.bradmcevoy.http.Request.Method
import java.util
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.{PubletWebContext, PubletWeb}
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.{CredentialsException, UsernamePasswordToken}
import org.eknet.publet.web.filter.AuthzFilter

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
