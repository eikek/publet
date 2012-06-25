package org.eknet.publet.web.webdav

import org.eknet.publet.vfs.Container
import com.bradmcevoy.http._
import exceptions.BadRequestException
import java.io.{OutputStream, InputStream}
import java.lang.Long
import com.bradmcevoy.http.Request.Method
import java.util

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 22:51
 */
class WebdavContainer(val container: Container) extends DavCollResource with GetableResource with PropFindableResource {
  def getUniqueId = null
  def getName = "/"
  def authenticate(user: String, password: String) = null
  def authorise(request: Request, method: Method, auth: Auth) = true
  def getRealm = ""
  def getModifiedDate = null
  def checkRedirect(request: Request) = null

  def sendContent(out: OutputStream, range: Range, params: util.Map[String, String], contentType: String) {
    throw new BadRequestException("unknown")
  }

  def getMaxAgeSeconds(auth: Auth) = null

  def getContentType(accepts: String) = "text/directory"

  def getContentLength = null

  def getCreateDate = null
}
