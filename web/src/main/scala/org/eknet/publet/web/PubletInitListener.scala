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
        val publ = new DefaultWebPublet(sc, List[WebExtension]().asJava)
        sc.setAttribute(webPubletKey.name, publ)

        shiroInit(sc, publ.publet)
      }
      catch {
        case e: Throwable => log.error("Error initializing publet!", e); throw e
      }
    }
  }

  def publetDestroy(sc: ServletContext) {
    try {
      Option(sc.getAttribute(webPubletKey.name)).map(_.asInstanceOf[WebPublet].gitPartition.close())
    } catch {
      case e: Throwable => log.error("Error on destroy.", e)
    }
  }

  def shiroInit(sc: ServletContext, publ: Publet) {
    try {
      val environment = createEnvironment(sc, publ);
      sc.setAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY, environment);

      log.debug("Published WebEnvironment as ServletContext attribute with name [{}]",
        EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY);

      if (log.isInfoEnabled()) {
        log.info("Shiro environment initialized.");
      }
    } catch {
      case e: Throwable => log.error("Error initializing shiro", e)
    }
  }

  def createEnvironment(sc: ServletContext, publ: Publet) = {
    val webenv = new DefaultWebEnvironment()
    webenv.setServletContext(sc)

    val pam = new PubletAuthManager(publ)
    sc.setAttribute(publetAuthManagerKey.name, pam)

    val realm = new UsersRealm(pam)
    webenv.setWebSecurityManager(createWebSecurityManager(publ, realm))
    createFilterChainResolver match {
      case Some(fr) => webenv.setFilterChainResolver(fr)
      case None =>
    }
    webenv
  }

  def createWebSecurityManager(publ: Publet, realm: AuthorizingRealm): WebSecurityManager = {
    val wsm = new DefaultWebSecurityManager()
    wsm.setRealm(realm)
    wsm
  }

  def createFilterChainResolver: Option[FilterChainResolver] = {
    val resolver = new PathMatchingFilterChainResolver()
    resolver.getFilterChainManager.addFilter("authc", new FormAuthenticationFilter)
    resolver.getFilterChainManager.addFilter("authcBasic", new BasicHttpAuthenticationFilter)
    resolver.getFilterChainManager.addFilter("anon", new AnonymousFilter)

    resolver.getFilterChainManager.createChain("/**", "authcBasic")
    Some(resolver)
  }
}
