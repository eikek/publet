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
import org.apache.shiro.SecurityUtils
import org.eknet.publet.web.util.{PubletWeb, PubletWebContext}
import org.eknet.publet.auth.store.UserProperty
import org.eknet.publet.vfs.ChangeInfo
import org.eknet.publet.auth.{ResourcePermissionService, ResourceAction, PermissionBuilder}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.12 18:52
 */
object Security extends Logging with PermissionBuilder {

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

  def checkResourcePerm(action: String, path: Path) {
    checkPerm(resource grant(action) on path.asString)
  }

  def checkResourcePerm(action: ResourceAction.Action, path: Path) {
    checkPerm(resource action(action) on path.asString)
  }

  def hasPerm(perm: String): Boolean = {
    isAuthenticated && subject.isPermitted(perm)
  }

  def hasResourcePerm(action: String, path: Path): Boolean = {
    hasPerm(resource grant(action) on path.asString)
  }

  def hasWritePermission(resourcePath: Path): Boolean = {
    PubletWeb.instance[ResourcePermissionService].get.isWritePermitted(resourcePath)
  }

  /**
   * Same as [[.hasWritePermission]] but throws an exception instead
   * of returning a boolean.
   *
   * @param resourcePath
   */
  def checkWritePermission(resourcePath: Path) {
    PubletWeb.instance[ResourcePermissionService].get.checkWrite(resourcePath)
  }


  def hasReadPermission(resourcePath: Path): Boolean = {
    PubletWeb.instance[ResourcePermissionService].get.isReadPermitted(resourcePath)
  }

  def hasGroup(names: String*) = {
    import collection.JavaConversions._
    if (!isAuthenticated) false
    else subject.hasAllRoles(names.toList)
  }
}
