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

import org.eknet.publet.auth.Algorithm
import org.eknet.publet.Glob

/**
 * Provides information used for authorizing and authenticating users.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.11.12 16:34
 */
trait UserStore {

  def findUser(login: String): Option[User]

  def allUser: Iterable[User]

  /**
   * Return all user that are in all specified groups.
   * @param groups
   * @return
   */
  def userOfGroups(groups: String*): Iterable[User]

  /**
   * Either update an existing user with all information from the
   * given one, or creates a new user.
   *
   * @param user
   * @return the old user or [[scala.None]] if a new user has been added
   */
  def updateUser(user: User): Option[User]

  def removeUser(login: String): Option[User]

  def addGroup(login: String, group: String)
  def dropGroup(login: String, group: String)
  def getGroups(login: String): Set[String]

}

class UserStoreAdapter extends UserStore {
  def findUser(login: String):Option[User] = None
  def allUser: Iterable[User] = Nil
  def userOfGroups(groups: String*): Iterable[User] = Nil
  def updateUser(user: User): Option[User] = None
  def removeUser(login: String): Option[User] = None
  def addGroup(login: String, group: String) {}
  def dropGroup(login: String, group: String) {}
  def getGroups(login: String) = Set[String]()
}