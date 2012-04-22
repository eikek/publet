package org.eknet.publet.web.filter

import org.apache.shiro.web.servlet.ShiroFilter
import org.eknet.publet.web.WebContext._
import org.eknet.publet.web.shiro.PubletAuthManager
import javax.servlet.ServletContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 23:40
 */
class ShiroSecurityFilter(sc: ServletContext) extends ShiroFilter {


  override def isEnabled = {
    //only enabled if both user account and permission files are there
    getServletContext.getAttribute(publetAuthManagerKey.name)
      .asInstanceOf[PubletAuthManager].active
  }

  //for some reason the servlet context was not available, probably
  // a misunderstanding in the use of MetaFilter or a bug there
  override def getServletContext = sc
}
