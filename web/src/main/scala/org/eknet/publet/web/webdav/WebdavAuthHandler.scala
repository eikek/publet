package org.eknet.publet.web.webdav

import com.bradmcevoy.http.{Request, Resource, AuthenticationHandler}
import org.apache.shiro.SecurityUtils
import org.eknet.publet.web.PubletWeb
import org.apache.shiro.authc.{CredentialsException, UsernamePasswordToken}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:10
 */
class WebdavAuthHandler extends AuthenticationHandler {

  def supports(r: Resource, request: Request): Boolean = {
    true
  }

  def authenticate(resource: Resource, request: Request) = {
    Option(SecurityUtils.getSubject.getPrincipal) match {
      case Some(p) => p
      case None => {
        val auth = Option(request.getAuthorization)
        val user = auth.flatMap(auth => Option(auth.getUser))
        val passw = auth.flatMap(auth => Option(auth.getPassword))
        user flatMap { u => passw map { p =>
          SecurityUtils.getSubject.login( new UsernamePasswordToken(u, p) ) }
        } getOrElse(new CredentialsException("No username/password pair given"))

        SecurityUtils.getSubject.getPrincipal
      }
    }
  }

  def getChallenge(resource: Resource, request: Request) = {
    val realmName = PubletWeb.publetSettings("webdav.realmName").getOrElse("WebDav Area")
    "Basic realm=\"" + realmName +"\""
  }

  def isCompatible(resource: Resource) = true
}
