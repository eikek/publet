package org.eknet.publet.web.webdav

import com.bradmcevoy.http.ResourceFactory
import org.eknet.publet.web.{PubletWebContext, PubletWeb}
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import com.bradmcevoy.http.exceptions.BadRequestException

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:01
 */
class WebdavResourceFactory extends ResourceFactory with Logging {

  def getResource(host: String, path: String) = {
    val resourcePath = {
      val p = PubletWebContext.applicationPath
      if (p.segments.last == "index.html" && !path.endsWith("index.html"))
        p.parent
      else
        p
    }
    PubletWeb.publet.rootContainer.lookup(resourcePath) match {
      case Some(r) => WebdavResource(r)
      case None => null
      case r@_ => throw new BadRequestException("Cannot convert resource for webdav: "+ r)
    }
  }
}
