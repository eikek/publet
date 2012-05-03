package org.eknet.publet.web

import org.eknet.publet.Publet
import org.eknet.publet.partition.git.GitPartition
import shiro.{UsersRealm, PubletAuthManager}
import template.StandardEngine
import javax.servlet.ServletContext
import org.apache.shiro.web.env.{DefaultWebEnvironment, EnvironmentLoader, WebEnvironment}
import org.eknet.publet.web.WebContext._
import grizzled.slf4j.Logging
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.web.mgt.{DefaultWebSecurityManager, WebSecurityManager}
import org.apache.shiro.web.filter.mgt.{PathMatchingFilterChainResolver, FilterChainResolver}
import org.apache.shiro.web.filter.authc.{AnonymousFilter, BasicHttpAuthenticationFilter, FormAuthenticationFilter}
import util.Key

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:33
 */
trait WebPublet {

  def publet: Publet

  def gitPartition: GitPartition

  def standardEngine: StandardEngine

  def servletContext: ServletContext
}

object WebPublet extends Logging {
  private val webPubletKey = Key[WebPublet]("org.eknet.web.publet")

  def setup(sc: ServletContext, ext:List[WebExtension]): WebPublet = {
    val publ = new DefaultWebPublet(sc, ext)
    initializeSecurity(publ)
    sc.setAttribute(webPubletKey.name, publ)
    publ
  }

  def apply(sc: ServletContext): WebPublet = sc.getAttribute(webPubletKey.name).asInstanceOf[WebPublet]
  def apply(): WebPublet = WebContext().service(webPubletKey)

  def close(sc:ServletContext) {
    val wp = WebPublet(sc)
    wp.gitPartition.close()
    sc.removeAttribute(publetAuthManagerKey.name)
    wp.servletContext.removeAttribute(webPubletKey.name)
  }

  def initializeSecurity(publ: WebPublet): WebEnvironment = {
    publ.servletContext.removeAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY);
    val environment = createEnvironment(publ);
    publ.servletContext.setAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY, environment);

    debug("Published WebEnvironment as ServletContext attribute with name ["+
      EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY+"]");

    info("Shiro environment initialized.");
    environment
  }

  private def createEnvironment(publ: WebPublet) = {
    val webenv = new DefaultWebEnvironment()
    webenv.setServletContext(publ.servletContext)

    val pam = new PubletAuthManager(publ.publet)
    publ.servletContext.setAttribute(publetAuthManagerKey.name, pam)

    val realm = new UsersRealm(pam)
    webenv.setWebSecurityManager(createWebSecurityManager(publ.publet, realm))
    webenv.setFilterChainResolver(createFilterChainResolver(pam))
    webenv
  }

  private def createWebSecurityManager(publ: Publet, realm: AuthorizingRealm): WebSecurityManager = {
    val wsm = new DefaultWebSecurityManager()
    wsm.setRealm(realm)
    wsm
  }

  private def createFilterChainResolver(pam: PubletAuthManager): FilterChainResolver = {
    val resolver = new PathMatchingFilterChainResolver()
    resolver.getFilterChainManager.addFilter("authc", new FormAuthenticationFilter)
    resolver.getFilterChainManager.addFilter("authcBasic", new BasicHttpAuthenticationFilter)
    resolver.getFilterChainManager.addFilter("anon", new AnonymousFilter)

    def filter(str: String) = if (str == "anon") str else "authcBasic"

    val mappings = pam.urlMappings
    if (mappings.isEmpty) resolver.getFilterChainManager.createChain("/**", "anon")
    else mappings.foreach(t => resolver.getFilterChainManager.createChain(t._1, filter(t._2)))
    resolver
  }

  private class DefaultWebPublet(val servletContext: ServletContext, exts: List[WebExtension]) extends WebPublet {

    private val triple = PubletFactory.createPublet()
    exts.foreach(_.onStartup(this, servletContext))

    def publet = triple._1
    def gitPartition = triple._2
    def standardEngine = triple._3
  }
}