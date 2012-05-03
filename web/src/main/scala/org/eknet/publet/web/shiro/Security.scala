package org.eknet.publet.web.shiro

import scala.collection.JavaConversions._
import org.apache.shiro.SecurityUtils
import org.eknet.publet.vfs.Path
import org.eknet.publet.web.WebContext
import org.apache.shiro.util.AntPathMatcher
import org.apache.shiro.web.env.WebEnvironment
import org.apache.shiro.web.util.WebUtils
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver
import org.apache.shiro.web.filter.authc.AnonymousFilter
import org.eknet.publet.auth.User

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.12 18:52
 */
object Security {

  val gitRead = "git:read"
  val gitWrite = "git:write"

  def pathPermission(path: Path) = path.segments.mkString("get:", ":", "")
  def pathPermission(engine:Symbol, path: Path) = path.segments.mkString(engine.name+":", ":", "")

  def subject = SecurityUtils.getSubject

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
    if (!isAnonymousRequest) subject.checkPermission(perm)
  }

  def checkPerm(path: Path) {
    checkPerm(pathPermission(path))
  }

  def checkPerm(engine:Symbol, path: Path) {
    checkPerm(pathPermission(engine, path))
  }

}
