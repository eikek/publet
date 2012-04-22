package org.eknet.publet.web.shiro

import javax.servlet.ServletContext
import org.apache.shiro.web.env.{DefaultWebEnvironment, EnvironmentLoaderListener}
import org.apache.shiro.web.mgt.{DefaultWebSecurityManager, WebSecurityManager}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 08:40
 */
class ShiroInitListener extends EnvironmentLoaderListener {

  override def createEnvironment(sc: ServletContext) = {
    val webenv = new DefaultWebEnvironment()
    webenv.setServletContext(sc)

    webenv.setWebSecurityManager(createWebSecurityManager)

//    val resolver: FilterChainResolver = createFilterChainResolver
//    if (resolver != null) {
//      webenv.setFilterChainResolver(resolver)
//    }
    webenv
  }

  def createWebSecurityManager(): WebSecurityManager = {
    val wsm = new DefaultWebSecurityManager()
    wsm.setRealm()
  }

//  def createFilterChainResolver(): FilterChainResolver = null
}
