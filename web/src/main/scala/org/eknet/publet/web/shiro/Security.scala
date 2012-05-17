package org.eknet.publet.web.shiro

import org.apache.shiro.SecurityUtils
import org.eknet.publet.vfs.Path
import org.eknet.publet.Publet
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.web.mgt.{DefaultWebSecurityManager, WebSecurityManager}
import org.apache.shiro.web.filter.mgt.{FilterChainResolver, PathMatchingFilterChainResolver}
import org.apache.shiro.web.filter.authc.{BasicHttpAuthenticationFilter, FormAuthenticationFilter, AnonymousFilter}
import org.apache.shiro.web.env.{EnvironmentLoader, DefaultWebEnvironment}
import grizzled.slf4j.Logging
import org.apache.shiro.authz.UnauthenticatedException
import org.eknet.publet.web.{PubletWebContext, GitAction}
import org.eknet.publet.auth.{RepositoryTag, RepositoryModel, User}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.12 18:52
 */
object Security extends Logging {

  def pathPermission(action: String, path: Path) = path.segments.mkString(action+":", ":", "")

  def subject = SecurityUtils.getSubject

  def isAuthenticated = subject.getPrincipals!=null
  def checkAuthenticated() {
    if (!isAuthenticated) throw new UnauthenticatedException()
  }

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

  def checkPerm(perm:String) {
    subject.checkPermission(perm)
  }

  def checkPerm(action: String, path: Path) {
    checkPerm(pathPermission(action, path))
  }

  def checkGitAction(action: GitAction.Value, model: RepositoryModel) {
    if (model.tag == RepositoryTag.closed || action == GitAction.push) {
      val perm = action.toString +":"+ model.name
      checkPerm(perm)
    }
  }
  def checkGitAction(action: GitAction.Value) {
    val repoModel = PubletWebContext.getRepositoryModel
    if (repoModel.isDefined) {
      checkGitAction(action, repoModel.get)
    }
  }

  def hasPerm(perm: String): Boolean = {
    subject.isPermitted(perm)
  }

  def hasPerm(action: String, path: Path): Boolean = {
    hasPerm(pathPermission(action, path))
  }

  def hasGitAction(action: GitAction.Value, model: RepositoryModel) = {
    val perm = action.toString +":"+ model.name
    hasPerm(perm)
  }

  def hasGitAction(action: GitAction.Value) = {
    val repoModel = PubletWebContext.getRepositoryModel
    if (repoModel.isDefined) {
      if (repoModel.get.tag == RepositoryTag.closed || action == GitAction.push) {
        val perm = action.toString +":"+ PubletWebContext.getRepositoryModel.map(_.name).get
        hasPerm(perm)
      } else {
        true
      }
    } else {
      true
    }
  }
}
