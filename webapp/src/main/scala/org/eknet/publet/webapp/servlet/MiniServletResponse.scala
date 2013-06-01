package org.eknet.publet.webapp.servlet

import javax.servlet.http.{Cookie => JCookie, HttpServletResponse}
import java.util.Locale
import spray.http._
import spray.http.HttpResponse
import spray.http.HttpCookie
import scala.Some
import java.io.{PrintWriter, OutputStreamWriter, ByteArrayOutputStream}
import javax.servlet.ServletOutputStream
import com.typesafe.scalalogging.slf4j.Logging
import spray.http.HttpHeaders.RawHeader

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 00:54
 */
class MiniServletResponse extends HttpServletResponse with Logging {

  private val cookies = collection.mutable.ListBuffer.empty[HttpCookie]
  private val headers = collection.mutable.Map.empty[String, List[String]]
  private var status: StatusCode = StatusCodes.OK
  private var contentType: String = null
  private var characterEncoding = "UTF-8"
  private var locale: Locale = Locale.getDefault

  private var errorResponse: Option[HttpResponse] = None

  private val output = new ByteArrayOutputStream()

  def addCookie(cookie: JCookie) {
    cookies.append(HttpCookie(cookie.getName, cookie.getValue))
  }

  def containsHeader(name: String) = headers.keySet.exists(h => h == name.toLowerCase)

  def encodeURL(url: String) = ???

  def encodeRedirectURL(url: String) = ???

  def encodeUrl(url: String) = ???

  def encodeRedirectUrl(url: String) = ???

  def sendError(sc: Int, msg: String) {
    errorResponse = Some(HttpResponse(status = sc, entity = HttpEntity(msg)))
  }

  def sendError(sc: Int) {
    errorResponse = Some(HttpResponse(status = sc))
  }

  def sendRedirect(location: String) { ??? }

  def setDateHeader(name: String, date: Long) {
    setHeader(name, date.toString)
  }

  def addDateHeader(name: String, date: Long) {
    addHeader(name, date.toString)
  }

  def setHeader(name: String, value: String) {
    headers.put(name, List(value))
  }

  def addHeader(name: String, value: String) {
    val next = value :: headers.get(name).getOrElse(Nil)
    headers.put(name, next)
  }

  def setIntHeader(name: String, value: Int) {
    setHeader(name, value.toString)
  }

  def addIntHeader(name: String, value: Int) {
    addHeader(name, value.toString)
  }

  def setStatus(sc: Int) {
    this.status = StatusCodes.getForKey(sc).get
  }

  def setStatus(sc: Int, sm: String) {
    setStatus(sc)
  }

  def getCharacterEncoding = characterEncoding

  def getContentType = contentType

  def getOutputStream = new ServletOutputStream {
    def write(b: Int) {
      output.write(b)
    }
  }

  def getWriter = new PrintWriter(new OutputStreamWriter(output, getCharacterEncoding))

  def setCharacterEncoding(charset: String) {
    this.characterEncoding = charset
  }

  def setContentLength(len: Int) {
    setIntHeader("Content-Length", len)
  }

  def setContentType(`type`: String) {
    this.contentType = `type`
  }

  def setBufferSize(size: Int) {}

  def getBufferSize = ???

  def flushBuffer() {}

  def resetBuffer() {}

  def isCommitted = false

  def reset() {}

  def setLocale(loc: Locale) {
    this.locale = loc
  }

  def getLocale = locale


  def toHttpResponse: HttpResponse = errorResponse match {
    case Some(r) => {
      logger.error(s"Responding with error: $r")
      r
    }
    case None => {
      val ct = Option(getContentType) flatMap { c =>
        val split = c.split("/", 2)
        MediaTypes.getForKey((split(0), split(1)))
      } map { mt =>
        ContentType(mt, HttpCharsets.getForKey(getCharacterEncoding).getOrElse(HttpCharsets.`UTF-8`))
      }
      val httpheaders = headers.toMap.map({ case (name, values) => RawHeader(name, values.mkString(", ")) }).toList

      HttpResponse(status, HttpBody(ct.getOrElse(ContentType.`application/octet-stream`), output.toByteArray), httpheaders.toList)
    }
  }

}