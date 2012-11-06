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

package org.eknet.publet.auth

import com.google.inject.{Singleton, Inject}
import java.util
import org.eknet.publet.auth.user.{PermissionStore, UserProperty, User, UserStore}
import org.eknet.publet.Glob
import java.util.Locale

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.11.12 18:10
 */
@Singleton
class DefaultAuthStore @Inject() (userStore: util.Set[UserStore],
                                  permStore: util.Set[PermissionStore],
                                  passwordProvider: PasswordServiceProvider)
      extends UserStore with PermissionStore {

  import collection.JavaConversions._

  type RealmnameProvider = Unit => String

  var realmnameProvider: RealmnameProvider = Unit => "WebDav Area"

  // user store
  def findUser(login: String) =
    userStore.foldLeft(None:Option[User])((el, store) => if (el.isDefined) el else store.findUser(login))

  def allUser = userStore.flatMap(s => s.allUser)

  def userOfGroups(groups: String*) = userStore.flatMap(store => store.userOfGroups(groups: _*))

  private def findUserStore(login: String) =
    userStore.foldLeft(None:Option[UserStore])((el, store) => if (el.isDefined) el else {
      if (store.findUser(login).isDefined) Some(store)
      else None
    })

  def updateUser(user: User) = {
    findUserStore(user.login) flatMap { store => store.updateUser(user) }
  }

  def removeUser(login: String) = findUserStore(login) flatMap { store => store.removeUser(login) }


  /**
   * Sets a new password for the given user.
   *
   * The password is encrypted using the algorithm associated
   * to the user or a default algorithm is used. The updated
   * user entry is returned.
   *
   * @param login
   * @param newpasswordPlain
   * @return
   */
  def setPassword(login: String, newpasswordPlain: String, algorithm: Option[Algorithm.Value]) = {
    findUser(login) map { user =>
      val realmName = realmnameProvider()
      val newdigest = DigestGenerator.encodePasswordInA1Format(login, realmName, newpasswordPlain)
      val algo = algorithm
        .orElse(user.get(UserProperty.algorithm).map(as => Algorithm.withName(as.toUpperCase(Locale.ROOT))))
        .getOrElse(Algorithm.sha256)
      val newpass = passwordProvider.forAlgorithm(algo).encryptPassword(newpasswordPlain)

      val props = user.properties +
        (UserProperty.password -> newpass) +
        (UserProperty.digest -> newdigest) +
        (UserProperty.algorithm -> algo.toString)

      val newuser = User(login, props)
      updateUser(newuser)
      newuser
    }
  }

  def addPermission(login: String, perm: String) {
    permStore.headOption map { store => store.addPermission(login, perm) }
  }

  def dropPermission(login: String, perm: String) {
    permStore.foreach(store => store.dropPermission(login, perm))
  }

  def getPermissions(login: String) =
    permStore.flatMap(store => store.getPermissions(login)).toSet

  def addGroup(login: String, group: String) {
    findUserStore(login) map { store => store.addGroup(login, group) }
  }

  def dropGroup(login: String, group: String) {
    findUserStore(login) map { store => store.dropGroup(login, group) }
  }

  def getGroups(login: String) =
    findUserStore(login) map { store => store.getGroups(login) } getOrElse(Set())

  def anonPatterns = userStore.toList.flatMap(store => store.anonPatterns)

  def addAnonPattern(pattern: Glob) {
    userStore.headOption.map(store => store.addAnonPattern(pattern))
  }

  def removeAnonPattern(pattern: Glob) {
    userStore.find(store => store.containsAnonPattern(pattern)).map(_.removeAnonPattern(pattern))
  }

  def containsAnonPattern(pattern: Glob) = userStore.foldLeft(false)((b, store) => if (b) true else store.containsAnonPattern(pattern))
}