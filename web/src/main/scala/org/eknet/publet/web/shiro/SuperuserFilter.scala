package org.eknet.publet.web.shiro

import org.eknet.publet.web.filter.SimpleFilter
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import javax.servlet.FilterChain
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.UsernamePasswordToken

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 17:39
 */
class SuperuserFilter extends SimpleFilter {
  def doFilter(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
    SecurityUtils.getSubject.login(new UsernamePasswordToken("superadmin", "superadmin".toCharArray))
  }
}
