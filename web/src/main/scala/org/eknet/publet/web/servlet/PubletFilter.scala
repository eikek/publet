package org.eknet.publet.web.servlet

import org.eclipse.jgit.http.server.glue.MetaFilter
import javax.servlet.FilterConfig
import org.eknet.publet.web.filter._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 23:56
 */
class PubletFilter extends MetaFilter {

  override def init(filterConfig: FilterConfig) {
    serveRegex("^/.*")
      .through(new WebContextFilter)
      .through(new RedirectFilter)
      .`with`(new PublishServlet)
  }

}
