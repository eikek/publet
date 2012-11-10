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

package org.eknet.publet.auth.store

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.11.12 00:09
 */
trait PermissionStore {

  /**
   * Associates a permission string to the named group.
   * @param group
   * @param perm
   */
  def addPermission(group: String, perm: String)

  /**
   * Removes the given associated permission string from
   * the group. If it is not associated, this just returns.
   *
   * @param group
   * @param perm
   */
  def dropPermission(group: String, perm: String)

  /**
   * Returns the union set of all permissions of all groups.
   *
   * @param group
   * @return
   */
  def getPermissions(group: String*): Set[String]

  /**
   * Returns any permissions specific to a certain user.
   *
   * @param login
   * @return
   */
  def getUserPermissions(login: String): Set[String]

}
