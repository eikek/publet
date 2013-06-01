package org.eknet.publet.webapp.servlet

import javax.servlet._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 18:29
 */
class FilterWrapper(filter: Filter) extends GenericServlet {
  override def init(config: ServletConfig) {
    super.init(config)
    filter.init(new FilterConfig {
      def getFilterName = filter.getClass.getSimpleName
      def getInitParameterNames = config.getInitParameterNames
      def getInitParameter(name: String) = config.getInitParameter(name)
      def getServletContext = config.getServletContext
    })
  }

  def service(req: ServletRequest, res: ServletResponse) {
    filter.doFilter(req, res, new FilterChain {
      def doFilter(request: ServletRequest, response: ServletResponse) {}
    })
  }

  override def destroy() {
    filter.destroy()
    super.destroy()
  }
}
