package org.eknet.publet.web.webdav

import com.bradmcevoy.http.{Request, Resource, AuthenticationHandler}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:10
 */
class WebdavAuthHandler extends AuthenticationHandler {
  def supports(r: Resource, request: Request) = false

  def authenticate(resource: Resource, request: Request) = null

  def getChallenge(resource: Resource, request: Request) = ""

  def isCompatible(resource: Resource) = false
}
