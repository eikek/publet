package org.eknet.publet.web

import scala.collection.JavaConverters._
import org.eknet.publet.partition.git.GitPartition
import org.eknet.publet.web.WebContext._
import javax.servlet.{ServletContext, ServletContextEvent, ServletContextListener}
import org.slf4j.LoggerFactory
import org.apache.shiro.web.mgt.{DefaultWebSecurityManager, WebSecurityManager}
import org.apache.shiro.web.env.{EnvironmentLoader, DefaultWebEnvironment}
import org.eknet.publet.Publet
import org.eknet.publet.web.shiro.{PubletAuthManager, UsersRealm}
import org.apache.shiro.web.filter.mgt.{PathMatchingFilterChainResolver, FilterChainResolver}
import org.apache.shiro.web.filter.authc.{AnonymousFilter, BasicHttpAuthenticationFilter, FormAuthenticationFilter}
import org.apache.shiro.realm.AuthorizingRealm


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 20:53
 */
class PubletInitListener extends ServletContextListener {

  private val log = LoggerFactory.getLogger(getClass)

  def contextInitialized(sce: ServletContextEvent) {
    publetInit(sce.getServletContext)
  }

  def contextDestroyed(sce: ServletContextEvent) {
    publetDestroy(sce.getServletContext)
  }

  def publetInit(sc: ServletContext) {
    synchronized {
      try {
        WebPublet.setup(sc, List[WebExtension]())
      } catch {
        case e: Throwable => log.error("Error initializing publet!", e); throw e
      }
    }
  }

  def publetDestroy(sc: ServletContext) {
    try {
      WebPublet.close(sc)
    } catch {
      case e: Throwable => log.error("Error on destroy.", e)
    }
  }


}
