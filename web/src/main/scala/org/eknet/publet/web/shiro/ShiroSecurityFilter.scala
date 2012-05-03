package org.eknet.publet.web.shiro

import org.apache.shiro.web.servlet.ShiroFilter
import org.eknet.publet.web.WebContext._
import javax.servlet.http.{HttpServletRequest, HttpServletRequestWrapper}
import org.apache.shiro.SecurityUtils
import javax.servlet._
import org.apache.shiro.authc.UsernamePasswordToken
import java.util.concurrent.Callable
import org.eknet.publet.auth.User

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 23:40
 */
class ShiroSecurityFilter extends ShiroFilter {

  private def getPubletAuthManager = {
    getServletContext.getAttribute(publetAuthManagerKey.name)
          .asInstanceOf[PubletAuthManager]
  }

//  override def isEnabled = {
////    only enabled if both user account and permission files are there
//    getServletContext.getAttribute(publetAuthManagerKey.name)
//      .asInstanceOf[PubletAuthManager].isActive
//  }

  //for some reason the servlet context was not available, probably
  // a misunderstanding in the use of MetaFilter or a bug there
  override def doFilterInternal(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
    val sreq = ShiroSecurityFilter.toShiroRequest(servletRequest)
    super.doFilterInternal(sreq, servletResponse, chain)
  }

  override def createSubject(request: ServletRequest, response: ServletResponse) = {
    val subj = super.createSubject(request, response)
    if (!getPubletAuthManager.isActive) {
      subj.execute(new Callable[Unit] {
        def call() {
          val token = new UsernamePasswordToken("superadmin", "superadmin".toCharArray)
          token.setRememberMe(true)
          subj.login(token)
        }
      })
    }
    subj
  }
}

object ShiroSecurityFilter {

  def toShiroRequest(req: ServletRequest): ServletRequest = req match {
    case hreq: HttpServletRequest => new ShiroRequest(hreq)
    case _ => req
  }

  private class ShiroRequest(req: HttpServletRequest) extends HttpServletRequestWrapper(req) {
    override def getRemoteUser = {
      val p = Option(SecurityUtils.getSubject.getPrincipal)
      p.map(_.asInstanceOf[User].login).getOrElse(null)
    }
  }

}
