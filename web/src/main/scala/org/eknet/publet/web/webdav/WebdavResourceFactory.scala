package org.eknet.publet.web.webdav

import org.eknet.publet.web.{PubletWebContext, PubletWeb}
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import io.milton.http.ResourceFactory
import io.milton.http.exceptions.BadRequestException

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:01
 */
class WebdavResourceFactory extends ResourceFactory with Logging {

  def getResource(host: String, path: String) = {
    val resourcePath = Path(stripContextPath(path))

    PubletWeb.publet.rootContainer.lookup(resourcePath) match {
      case Some(r) => WebdavResource(r)
      case None => {
        debug("No webdav resource found for path: "+ resourcePath.asString)
        null
      }
      case r@_ => throw new BadRequestException("Cannot convert resource for webdav: "+ r)
    }
  }

  private def stripContextPath(path: String): String = {
    if (PubletWeb.servletContext.getContextPath.isEmpty) {
      path
    } else {
      val npath = if (!path.startsWith("/")) "/"+path else path
      npath.substring(PubletWeb.servletContext.getContextPath.length)
    }
  }
}
