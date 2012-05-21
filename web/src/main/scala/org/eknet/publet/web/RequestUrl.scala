package org.eknet.publet.web

import javax.servlet.http.HttpServletRequest
import java.net.URLDecoder
import org.eknet.publet.vfs.Path
import util.{Request, Key}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 21:03
 */
trait RequestUrl {

  protected def req: HttpServletRequest

  private val requestUriKey = Key("requestUriDecoded", {
    case Request => URLDecoder.decode(req.getRequestURI, "UTF-8")
  })

  /** The complete request uri, from the hostname up to the query string. Decoded but untouched otherwise */
  def requestUri = PubletWebContext.attr(requestUriKey).get

  private val urlBaseKey = Key("urlBase", {
    case Request => PubletWeb.publetSettings("publet.urlBase").getOrElse {
      val uri = req.getScheme +"://"+ req.getServerName
      val base = if (Set(80, 443) contains req.getServerPort)
        uri
      else
        uri +":" +req.getServerPort

      if (PubletWeb.servletContext.getContextPath.isEmpty) base
      else base + PubletWeb.servletContext.getContextPath
    }
  })

  /**Url prefix of this application. This is read from the config file or constructed
   * using the information provided by the request.
   *
   * This base should be used when constructing urls. The path does not end
   * with a `/` character
   * @return
   */
  def urlBase = PubletWebContext.attr(urlBaseKey).get

  /**Url prefix of this application. This is read from the config file or constructed
   * using the information provided by the request.
   *
   * This base should be used when constructing urls.
   * @return
   */
  def urlBasePath = Path(urlBase+"/")


  private val applicationUriKey: Key[String] = Key("applicationUri", {
    case Request => {
      val p = Path(req.getRequestURI.substring(req.getContextPath.length))
      if (p.directory) (p/"index.html").asString else p.asString
    }
  })

  /** The part of the uri after the context path. If it is a directory,
   * the standard `index.html` is appended. */
  def applicationUri = PubletWebContext.attr(applicationUriKey).get
  /** The part of the uri after the context path. */
  def applicationPath = Path(applicationUri)

  private val fullUrlKey = Key("fullUrl", {
    case Request => {
      val url = req.getRequestURI  //.substring((req.getContextPath+req.getServletPath).length)
      val params = Option(req.getQueryString).map("?"+_).getOrElse("")

      (if (url.startsWith("/")) url.substring(1) else url) + params
    }
  })

  /** The full uri to this request. With parameters, without contextPath */
  def fullUrl = PubletWebContext.attr(fullUrlKey).get

  def isGitRequest = applicationUri.startsWith("/"+Config.gitMount)

  /**
   * Returns the decoded context path
   * @return
   */
  def contextPath = URLDecoder.decode(req.getContextPath, "UTF-8")
}