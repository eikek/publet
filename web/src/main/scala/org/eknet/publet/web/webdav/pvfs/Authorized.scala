package org.eknet.publet.web.webdav.pvfs

import org.eknet.publet.web.shiro.{DigestAuthenticationToken, Security}
import org.eknet.publet.web.PubletWebContext
import org.eknet.publet.web.filter.AuthzFilter
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.UsernamePasswordToken
import grizzled.slf4j.Logging
import com.bradmcevoy.http.{Auth, Request, DigestResource, Resource}
import com.bradmcevoy.http.Request.Method
import com.bradmcevoy.http.http11.auth.DigestResponse

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.06.12 19:07
 */
trait Authorized extends Resource with Logging with DigestResource {

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
      case None => loginBasic(user, password)
    }
  }

  private def loginBasic(user: String, password: String) = {
    SecurityUtils.getSubject.login( new UsernamePasswordToken(user, password) )
    findPrincipal
  }

  private def loginDigest(req: DigestResponse) = {
    SecurityUtils.getSubject.login(new DigestAuthenticationToken(req))
    findPrincipal
  }

  private def findPrincipal = {
    val p = SecurityUtils.getSubject.getPrincipal
    if (p == null && AuthzFilter.hasAccessToResource(PubletWebContext.applicationUri)) {
      "anonymous" //must return something in order to allow anonymous access
    } else {
      p
    }
  }

  def authenticate(digestRequest: DigestResponse) = {
    Option(SecurityUtils.getSubject.getPrincipal) match {
      case Some(p) => p
      case None => loginDigest(digestRequest)
    }
  }


  def isDigestAllowed = authenticationRequired

  private def authenticationRequired = {
    val method = Request.Method.valueOf(PubletWebContext.getMethod.toString)
    if (method.isWrite) {
      trace("WebDAV: authorize write!")
      !Security.hasWritePermission(PubletWebContext.applicationPath)
    } else {
      trace("WebDAV: authorize read!")
      !AuthzFilter.hasAccessToResource(PubletWebContext.applicationUri)
    }
  }
}
