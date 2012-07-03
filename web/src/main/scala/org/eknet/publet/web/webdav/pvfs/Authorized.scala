package org.eknet.publet.web.webdav.pvfs

import com.bradmcevoy.http.{Auth, Request, Resource}
import com.bradmcevoy.http.Request.Method
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.PubletWebContext
import org.eknet.publet.web.filter.AuthzFilter
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.UsernamePasswordToken
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.06.12 19:07
 */
trait Authorized extends Resource with Logging {

  /**
   * Checks permission for the current request uri.
   *
   * @param request
   * @param method
   * @param auth
   * @return
   */
  override def authorise(request: Request, method: Method, auth: Auth): Boolean = {
    if (method.isWrite) {
      trace("WebDAV: authorize write!")
      Security.hasWritePermission(PubletWebContext.applicationPath)
    } else {
      trace("WebDAV: authorize read!")
      AuthzFilter.hasAccessToResource(PubletWebContext.applicationUri)
    }
  }

  override def authenticate(user: String, password: String) = {
    Option(SecurityUtils.getSubject.getPrincipal) match {
      case Some(p) => p
      case None => {
        SecurityUtils.getSubject.login( new UsernamePasswordToken(user, password) )
        val p = SecurityUtils.getSubject.getPrincipal
        if (p == null && AuthzFilter.hasAccessToResource(PubletWebContext.applicationUri)) {
          "anonymous" //must return something in order to allow anonymous access
        } else {
          p
        }
      }
    }
  }
}
