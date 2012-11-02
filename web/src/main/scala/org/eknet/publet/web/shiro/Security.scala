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
import org.eknet.publet.auth._
import org.apache.shiro.SecurityUtils
import org.eknet.publet.vfs.ChangeInfo
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.web.RepositoryNameResolver
import org.eknet.publet.auth.repository.{RepositoryModel, GitAction, RepositoryTag}
import org.eknet.publet.auth.user.UserProperty

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
  def securityFilterEnabled = AuthcFilter.authenticationEnabled(PubletWebContext.req)

  def subject = SecurityUtils.getSubject

  def isAuthenticated = {
    if (!securityFilterEnabled) false
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
  def user = if (!isAuthenticated) None
    else Option(subject.getPrincipal)
      .collect({case login:String=>login})
      .flatMap(PubletWeb.authManager.findUser)

  /**
   * Returns a [[org.eknet.publet.vfs.ChangeInfo]] object populated with data
   * from the current user.
   *
   * @param message
   * @return
   */
  def changeInfo(message: String) = user.map( u=> {
    new ChangeInfo(u.get(UserProperty.fullName), u.get(UserProperty.email), message)
  })

  /**
   * Returns the username of the currently logged in user
   * or `anonymous` if not logged in.
   *
   * @return
   */
  def username = user.map(_.login).getOrElse {
    if (isAuthenticated) subject.getPrincipal.toString
    else "anonymous"
  }

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
    if (model.tag != RepositoryTag.open || action != GitAction.pull) {
      //not checking pull, if push is granted: TODO use implies relation on Permission!
      if (action != GitAction.pull || !hasGitAction(GitAction.push, model)) {
        val perm = action.toString +":"+ model.name
        checkPerm(perm)
      }
    }
  }

  def hasGitAction(action: GitAction.Value, model: RepositoryModel): Boolean = {
    if (model.tag == RepositoryTag.open && action == GitAction.pull)
      true
    else {
      //not checking pull, if push is granted: TODO use implies relation on Permission!
      if (action != GitAction.pull || !hasGitAction(GitAction.push, model)) {
        val perm = action.toString +":"+ model.name
        hasPerm(perm)
      } else {
        true
      }
    }
  }

  def hasGitAction(action: GitAction.Value): Boolean = {
    val repoModel = PubletWebContext.getRepositoryModel
    if (repoModel.isDefined) {
      hasGitAction(action, repoModel.get)
    } else {
      true
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

  /**
   * Checks whether the current user has write permission to the
   * resource at the specified path.
   *
   * If the path points to a resource in a git repository, this
   * will check whether `push` permission to the repository is granted
   * to the current user. If the resource is not inside a git repository
   * a generated permission `write:[ppath]` is checked, where `ppath` is
   * the `resourcePath` where each path delimiter `/` is replaced by a
   * colon.
   *
   * @param resourcePath
   */
  def hasWritePermission(resourcePath: Path): Boolean = {
    writePermissionCheck(resourcePath)(hasGitAction, hasPerm)
  }

  /**
   * Same as [[.hasWritePermission]] but throws an exception instead
   * of returning a boolean.
   *
   * @param resourcePath
   */
  def checkWritePermission(resourcePath: Path) {
    writePermissionCheck(resourcePath)(checkGitAction, checkPerm)
  }

  private def writePermissionCheck[A](resource:Path)(gf:(GitAction.Value, RepositoryModel)=>A, rf:String=>A): A = {
    val gp = PubletWeb.getRepositoryModel(resource) map { model =>
      gf(GitAction.push, model)
    }
    gp getOrElse {
      val perm = "write:"+ resource.segments.mkString(":")
      rf(perm)
    }
  }

  /**
   * Returns whether the current request can access the resource at
   * the specified uri.
   *
   * The request is allowed, if the resource is explicitely marked
   * with an `anon` permission. If it is marked with another permission,
   * it checks the permission against the principal of the current request.
   *
   * If no permission is specified, it is checked whether the resource
   * belongs to a git repository. If it is an open git repository, access
   * is granted. Otherwise `pull` permission is checked.
   *
   * If the resource it not marked with an explicit permission and neither
   * belongs to a git repository, it is considered an open resource and
   * any request may access it.
   *
   * @param applicationUri
   * @return
   */
  def hasReadPermission(applicationUri: String): Boolean = {
    lazy val repoModel = RepositoryNameResolver
      .getRepositoryName(Path(applicationUri), isGitRequest = false)
      .map(name => PubletWeb.authManager.getRepository(name.name))

    lazy val hasPull = repoModel map { repoModel =>
      Security.hasGitAction(GitAction.pull, repoModel)
    }
true
    //TODO
//    PubletWeb.authManager.getResourceConstraints(applicationUri).map(rc => {
//      if (rc.perm.isAnon) true
//      else Security.hasPerm(rc.perm.permString)
//    }) getOrElse {
//      hasPull getOrElse (true)
//    }
  }

}
