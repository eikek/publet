package org.eknet.publet.web.filter

import org.apache.shiro.web.servlet.ShiroFilter
import org.eknet.publet.web.WebContext._
import org.eknet.publet.web.shiro.PubletAuthManager
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper}
import org.apache.shiro.SecurityUtils
import javax.servlet._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 23:40
 */
class ShiroSecurityFilter extends ShiroFilter {

  override def isEnabled = {
    //only enabled if both user account and permission files are there
    getServletContext.getAttribute(publetAuthManagerKey.name)
      .asInstanceOf[PubletAuthManager].active
  }

  //for some reason the servlet context was not available, probably
  // a misunderstanding in the use of MetaFilter or a bug there
  override def doFilterInternal(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
    val sreq = ShiroSecurityFilter.toShiroRequest(servletRequest)
    super.doFilterInternal(sreq, servletResponse, chain)
  }
}

object ShiroSecurityFilter {

  def toShiroRequest(req: ServletRequest): ServletRequest = req match {
    case hreq: HttpServletRequest => new ShiroRequest(hreq)
    case _ => req
  }

  private class ShiroRequest(req: HttpServletRequest) extends HttpServletRequestWrapper(req) {
    override def getRemoteUser =  SecurityUtils.getSubject.getPrincipal.toString
  }
}
