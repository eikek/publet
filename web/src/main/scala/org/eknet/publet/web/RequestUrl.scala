/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.web

import javax.servlet.http.HttpServletRequest
import java.net.URLDecoder
import org.eknet.publet.vfs.Path
import util.{Request, Key}
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 21:03
 */
trait RequestUrl extends Logging {
  this: RequestAttr =>

  protected def req: HttpServletRequest

  private val requestUriKey = Key("requestUriDecoded", {
    case Request => URLDecoder.decode(req.getRequestURI, "UTF-8")
  })

  /** The complete request uri, from the hostname up to the query string. Decoded but untouched otherwise */
  def requestUri = PubletWebContext.attr(requestUriKey).get

  private val urlBaseKey = Key("urlBase", {
    case Request => Config("publet.urlBase").getOrElse {
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
  def urlBase = attr(urlBaseKey).get

  private val applicationUriKey: Key[String] = Key("applicationUri", {
    case Request => {
      val cp = Config("publet.contextPath").getOrElse(req.getContextPath)
      val p = Path(req.getRequestURI.substring(cp.length))
      if (p.directory) (p/"index.html").asString else p.asString
    }
  })

  /** The part of the uri after the context path. If it is a directory,
   * the standard `index.html` is appended. */
  def applicationUri = attr(applicationUriKey).get
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
  def fullUrl = attr(fullUrlKey).get

  def isGitRequest = applicationUri.startsWith("/"+Config.gitMount+"/")

  private val resourceUri = Key("applicationSourceUri", {
    case Request => {
      PubletWeb.publet.findSources(applicationPath).toList match {
        case c::cs => Some(applicationPath.sibling(c.name.fullName))
        case _ => None
      }
    }
  })

  /**
   * Creates an absolute url by prefixing the host and
   * context path to the given path
   *
   * @param path
   * @return
   */
  def urlOf(path: String): String = urlOf(Path(path))

  /**
   * Creates an absolute url by prefixing the host and
   * context path to the given path
   *
   * @param path
   * @return
   */
  def urlOf(path: Path): String =  urlBase + path.toAbsolute.asString


  /**
   * Returns the path to the source file that this request
   * is pointing to.
   * @return
   */
  def resourcePath = attr(resourceUri).get

  def getResourceUri = resourcePath.map(_.asString).getOrElse("")

  /**
   * Returns the query string that is contained in the request URL after the path. This method
   * returns [[scala.None]] if the URL does not have a query string. Same as the value of the CGI
   * variable QUERY_STRING.
   * @return
   */
  def getQueryString = Option(req.getQueryString)

}
