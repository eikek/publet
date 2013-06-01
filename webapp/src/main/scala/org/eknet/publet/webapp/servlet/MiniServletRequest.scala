package org.eknet.publet.webapp.servlet

import javax.servlet.http.{HttpServletRequest, Cookie => JCookie}
import org.eknet.publet.content.Path
import spray.http.{HttpCookie, HttpBody}
import java.io.{InputStreamReader, BufferedReader, ByteArrayInputStream}
import javax.servlet.{ServletContext, ServletInputStream}
import java.security.Principal
import java.util.Locale
import java.net.URL

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 00:42
 */
class MiniServletRequest(context: ServletContext, settings: RequestSettings) extends HttpServletRequest with BasicAttributes {
  private val request = settings.request.parseAll

  import collection.JavaConverters._

  private val localeRegex = """(.*?)-(.*)""".r

  private def splitHeaderValue(value: String) = value.split("\\s*,\\s*").map(_.trim)

  private def getFirstHeader(name: String) = request.headers.find(_.is(name.toLowerCase)).flatMap(h => splitHeaderValue(h.value).headOption)
  private def getAllHeaders(name: String) = request.headers.filter(_.is(name.toLowerCase)).flatMap(h => splitHeaderValue(h.value))

  private def convertCookie(c: HttpCookie): JCookie = {
    val jc = new JCookie(c.name, c.content)
    jc.setSecure(c.secure)
    c.domain.foreach(jc.setDomain)
    c.path.foreach(jc.setPath)
    c.maxAge.foreach(a => jc.setMaxAge(a.toInt))
    jc
  }

  private lazy val cookies = request.cookies
    .map(convertCookie)
    .toArray

  private def requestInput = request.entity match {
    case HttpBody(ct, buffer) => new ByteArrayInputStream(buffer)
    case _ => new ByteArrayInputStream(Array())
  }

  //TODO make other schemes available here
  def getAuthType = settings.remoteUser match {
    case Some(u) => "BASIC_AUTH"
    case _ => null
  }

  def getCookies = cookies

  def getDateHeader(name: String) = getFirstHeader(name).map(_.toLong).getOrElse(-1L)

  def getHeader(name: String) = getFirstHeader(name).orNull

  def getHeaders(name: String) = getAllHeaders(name).iterator

  def getHeaderNames = request.headers.map(_.name).iterator

  def getIntHeader(name: String) = getFirstHeader(name).map(_.toInt).getOrElse(-1)

  def getMethod = request.method.value.toUpperCase

  def getPathInfo = Path(request.path).drop(Path(settings.servletPath).segments.size).absoluteString

  def getPathTranslated = getRealPath(getPathInfo)

  def getContextPath = ""

  def getQueryString = request.query

  def getRemoteUser = settings.remoteUser.orNull

  def isUserInRole(role: String) = ???

  def getUserPrincipal = settings.remoteUser.map(n => new Principal {
    def getName = n
  }).orNull

  def getRequestedSessionId = null

  def getRequestURI = request.uri

  def getRequestURL = new StringBuffer(getScheme +"://"+ request.hostAndPort + request.uri + "?"+ request.rawQuery)

  def getServletPath = settings.servletPath

  def getSession(create: Boolean) = new RequestSession(context)

  def getSession = getSession(true)

  def isRequestedSessionIdValid = false

  def isRequestedSessionIdFromCookie = false

  def isRequestedSessionIdFromURL = false

  //deprecated
  def isRequestedSessionIdFromUrl = false

  def getCharacterEncoding = request.encoding.value

  def setCharacterEncoding(env: String) { }

  def getContentLength = request.headers.find(_.is("content-length")).map(_.value.toInt).getOrElse(-1)

  def getContentType = request.headers.find(_.is("content-type")).map(_.value).orNull

  def getInputStream = new ServletInputStream {
    private val in = requestInput
    def read() = in.read()
  }

  def getParameter(name: String) = request.queryParams.get(name).orNull

  def getParameterNames = request.queryParams.keysIterator

  def getParameterValues(name: String) = request.queryParams.get(name).map(v => Array(v)).orNull

  def getParameterMap = request.queryParams.asJava

  def getProtocol = request.protocol.value

  def getScheme = if (isSecure) "https" else "http"

  def getServerName = request.host

  def getServerPort = request.port.getOrElse(getLocalPort)

  def getReader = new BufferedReader(new InputStreamReader(requestInput))

  def getRemoteAddr = null

  def getRemoteHost = null

  def getRemotePort = -1

  def getLocale = getLocales.iterator.find(p => true).getOrElse(Locale.getDefault)

  def getLocales = getHeaders("Accept-Language").iterator.map(loc => {
    loc match {
      case localeRegex(lang, c) => new Locale(lang, c)
      case _ => new Locale(loc)
    }
  })

  def isSecure = false

  def getRequestDispatcher(path: String) = ???

  //deprecated
  def getRealPath(path: String) = throw new UnsupportedOperationException

  def getLocalName = settings.bindAddress

  def getLocalAddr = settings.bindAddress

  def getLocalPort = settings.bindPort
}
