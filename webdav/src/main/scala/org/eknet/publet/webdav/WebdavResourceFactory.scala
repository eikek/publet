package org.eknet.publet.webdav

import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import com.bradmcevoy.http.ResourceFactory
import com.bradmcevoy.http.exceptions.BadRequestException
import org.eknet.publet.Publet

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:01
 */
class WebdavResourceFactory(publet: Publet, contextPath: String) extends ResourceFactory with Logging {

  def getResource(host: String, path: String) = {
    val resourcePath = Path(stripContextPath(path))

    publet.rootContainer.lookup(resourcePath) match {
      case Some(r) => WebdavResource(r)
      case None => {
        debug("No webdav resource found for path: "+ resourcePath.asString)
        null
      }
      case r@_ => throw new BadRequestException("Cannot convert resource for webdav: "+ r)
    }
  }

  private def stripContextPath(path: String): String = {
    if (contextPath.isEmpty) {
      path
    } else {
      val npath = if (!path.startsWith("/")) "/"+path else path
      npath.substring(contextPath.length)
    }
  }
}
