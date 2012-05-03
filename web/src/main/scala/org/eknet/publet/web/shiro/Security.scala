package org.eknet.publet.web.shiro

import scala.collection.JavaConversions._
import org.apache.shiro.SecurityUtils
import org.eknet.publet.vfs.Path
import org.eknet.publet.auth.User
import org.eknet.publet.web.{WebPublet, WebContext}
import org.eknet.publet.web.WebContext._
import org.eknet.publet.Publet
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.web.mgt.{DefaultWebSecurityManager, WebSecurityManager}
import org.apache.shiro.web.filter.mgt.{FilterChainResolver, PathMatchingFilterChainResolver}
import org.apache.shiro.web.filter.authc.{BasicHttpAuthenticationFilter, FormAuthenticationFilter, AnonymousFilter}
import org.apache.shiro.web.env.{EnvironmentLoader, DefaultWebEnvironment}
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.12 18:52
 */
object Security extends Logging {

  val gitRead = "git:read"
  val gitWrite = "git:write"

  val get = "get"
  val put = "put"

  def pathPermission(action: String, path: Path) = path.segments.mkString(action+":", ":", "")
  def pathPermission(engine: Symbol, path: Path) = path.segments.mkString(engine.name+":", ":", "")

  def subject = SecurityUtils.getSubject

  def isAuthenticated = subject.getPrincipals!=null

  /**
   * Returns the currently logged in user or [[scala.None]] if
   * not logged in.
   *
   * @return
   */
  def user = Option(subject.getPrincipal).map(_.asInstanceOf[User])

  /**
   * Returns the username of the currently logged in user
   * or `anonymous` if not logged in.
   *
   * @return
   */
  def username = user.map(_.login).getOrElse("anonymous")

  /**
   * Returns shiros session associated to the
   * current subject
   *
   * @return
   */
  def session = subject.getSession

  def isAnonymousRequest: Boolean = {
    val path = WebContext().requestPath
    val webenv = WebContext().shiroWebEnvironment

    val fcm = webenv.getFilterChainResolver.asInstanceOf[PathMatchingFilterChainResolver]
    val matcher = fcm.getPathMatcher

    val requestPath = path.toAbsolute.asString
    val chain = fcm.getFilterChainManager.getChainNames.find(cn => matcher.matches(cn, requestPath))
      .map(cn => fcm.getFilterChainManager.getChain(cn).get(0))

    chain.exists(_.getClass == classOf[AnonymousFilter])
  }

  def checkPerm(perm:String) {
    subject.checkPermission(perm)
  }

  def checkPerm(action: String, path: Path) {
    checkPerm(pathPermission(action, path))
  }

  def checkPerm(engine: Symbol, path: Path) {
    checkPerm(pathPermission(engine, path))
  }

  def hasPerm(perm: String): Boolean = {
    subject.isPermitted(perm)
  }

  def hasPerm(action: String, path: Path): Boolean = {
    hasPerm(pathPermission(action, path))
  }

  def hasPerm(engine: Symbol, path: Path): Boolean = {
    hasPerm(pathPermission(engine, path))
  }

  private[publet] def init(publ: WebPublet) {

    def createEnvironment(publ: WebPublet) = {
      val webenv = new DefaultWebEnvironment()
      webenv.setServletContext(publ.servletContext)

      val pam = new PubletAuthManager(publ.publet)
      publ.servletContext.setAttribute(publetAuthManagerKey.name, pam)

      val realm: AuthorizingRealm = (if (pam.isActive)
        new UsersRealm(pam)
      else
        new SuperRealm())

      webenv.setWebSecurityManager(createWebSecurityManager(publ.publet, realm))
      webenv.setFilterChainResolver(createFilterChainResolver(pam))
      webenv
    }

    def createWebSecurityManager(publ: Publet, realm: AuthorizingRealm): WebSecurityManager = {
      val wsm = new DefaultWebSecurityManager()
      wsm.setRealm(realm)
      wsm
    }

    def createFilterChainResolver(pam: PubletAuthManager): FilterChainResolver = {
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

    publ.servletContext.removeAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY);
    val environment = createEnvironment(publ);
    publ.servletContext.setAttribute(EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY, environment);

    debug("Published WebEnvironment as ServletContext attribute with name ["+
      EnvironmentLoader.ENVIRONMENT_ATTRIBUTE_KEY+"]");

    info("Shiro environment initialized.");
    environment
  }
}
