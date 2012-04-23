package org.eknet.publet.web.servlet

import org.eclipse.jgit.http.server.glue.MetaServlet
import org.slf4j.LoggerFactory
import javax.servlet._

/** Building with the infrastructure provided by jgit.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 23:52
 */
class PubletServlet extends MetaServlet(new PubletFilter) {
  private val log = LoggerFactory.getLogger(getClass)
  val filter = getDelegateFilter

  //this is necessary because MetaServlet is not calling super.init, thus the servletconfig is not available
  var servletConfig: ServletConfig = null

  override def init(config: ServletConfig) {
    this.servletConfig = config
    filter.init(new FilterConfig {

      def getInitParameterNames = config.getInitParameterNames

      def getFilterName = filter.getClass.getName

      def getServletContext = config.getServletContext

      def getInitParameter(name: String) = config.getInitParameter(name)
    })
  }
}
