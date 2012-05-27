/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.web.shiro

import org.eknet.publet.vfs.Path
import grizzled.slf4j.Logging
import org.apache.shiro.authz.UnauthenticatedException
import org.eknet.publet.web.{PubletWebContext, GitAction}
import org.eknet.publet.auth.{RepositoryTag, RepositoryModel, User}
import org.apache.shiro.SecurityUtils
import org.eknet.publet.web.filter.PubletShiroFilter

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.12 18:52
 */
object Security extends Logging {

  def pathPermission(action: String, path: Path) = path.segments.mkString(action+":", ":", "")

  /**Returns whether the shiro request filter is enabled for this request.
   * Note, that any access to the shiro subsystem is forbidden if this returns
   * false.
   *
   * @return
   */
  def securityFilterEnabled = PubletShiroFilter.shiroFilterEnabled

  def subject = SecurityUtils.getSubject

  def isAuthenticated = {
    if (!PubletShiroFilter.shiroFilterEnabled) false
    else subject.getPrincipals != null
  }

  def checkAuthenticated() {
    if (!isAuthenticated) throw new UnauthenticatedException()
  }

  /**
   * Returns the currently logged in user or [[scala.None]] if
   * not logged in.
   *
   * @return
   */
  def user = if (!isAuthenticated) None else Option(subject.getPrincipal).map(_.asInstanceOf[User])

  /**
   * Returns the username of the currently logged in user
   * or `anonymous` if not logged in.
   *
   * @return
   */
  def username = user.map(_.login).getOrElse("anonymous")

  /**
   * Returns shiros session associated to the
   * current subject.
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
      val push= GitAction.push.toString +":"+ model.name
      if (!hasPerm(push)) {
        val perm = action.toString +":"+ model.name
        checkPerm(perm)
      }
    }
  }
  def checkGitAction(action: GitAction.Value) {
    val repoModel = PubletWebContext.getRepositoryModel
    if (repoModel.isDefined) {
      checkGitAction(action, repoModel.get)
    }
  }

  def hasPerm(perm: String): Boolean = {
    isAuthenticated && subject.isPermitted(perm)
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
