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

import com.google.inject.{Singleton, Inject}
import java.util
import java.util.Locale
import org.eknet.publet.Glob
import org.eknet.publet.auth.{AuthDataChanged, DigestGenerator, Algorithm, PasswordServiceProvider}
import com.google.common.eventbus.EventBus

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.11.12 18:10
 */
@Singleton
class DefaultAuthStore @Inject() (passwordProvider: PasswordServiceProvider, bus: EventBus)
      extends UserStore with PermissionStore with ResourceSetStore {

  import collection.JavaConversions._

  type RealmnameProvider = Unit => String

  var realmnameProvider: RealmnameProvider = Unit => "WebDav Area"

  private var userStore: util.Set[UserStore] = util.Collections.emptySet()
  private var permStore: util.Set[PermissionStore] = util.Collections.emptySet()
  private var resourceStore: util.Set[ResourceSetStore] = util.Collections.emptySet()

  @Inject(optional = true)
  def setUserStore(store: util.Set[UserStore]) {
    this.userStore = store
  }

  @Inject(optional = true)
  def setPermStore(store: util.Set[PermissionStore]) {
    this.permStore = store
  }

  @Inject(optional = true)
  def setResourceStore(store: util.Set[ResourceSetStore]) {
    this.resourceStore = store
  }

  // user store
  def findUser(login: String) =
    userStore.foldLeft(None:Option[User])((el, store) => if (el.isDefined) el else store.findUser(login))

  def allUser = userStore.flatMap(s => s.allUser)

  def allGroups = userStore.flatMap(s => s.allGroups).toSet

  def userOfGroups(groups: String*) = userStore.flatMap(store => store.userOfGroups(groups: _*))

  private def findUserStores(login: String) =
    userStore.filter(store => store.findUser(login).isDefined)

  def updateUser(user: User): Option[User] = {
    val results = userStore flatMap { store => store.updateUser(user) }
    val ret = if (results.isEmpty) {
      userStore.map(store => store.updateUser(user))
      None
    } else {
      results.headOption
    }
    bus.post(new AuthDataChanged)
    ret
  }

  def removeUser(login: String) = {
    val results = findUserStores(login) flatMap { store => store.removeUser(login) }
    bus.post(new AuthDataChanged)
    results.headOption
  }


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
    val realmName = realmnameProvider()
    val newdigest = DigestGenerator.encodePasswordInA1Format(login, realmName, newpasswordPlain)
    findUserStores(login).map(store => {
      val user = store.findUser(login).get
      val algo = algorithm
        .orElse(user.get(UserProperty.algorithm).map(as => Algorithm.withName(as.toUpperCase(Locale.ROOT))))
        .getOrElse(Algorithm.sha256)
      val newpass = passwordProvider.forAlgorithm(algo).encryptPassword(newpasswordPlain)

      val props = user.properties +
        (UserProperty.password -> newpass) +
        (UserProperty.digest -> newdigest) +
        (UserProperty.algorithm -> algo.toString)

      val newuser = User(login, props)
      store.updateUser(newuser)
      bus.post(new AuthDataChanged)
      newuser
    }).headOption
  }

  def addPermission(group: String, perm: String) {
    permStore map { store => store.addPermission(group, perm) }
    bus.post(new AuthDataChanged)
  }

  def dropPermission(group: String, perm: String) {
    permStore.foreach(store => store.dropPermission(group, perm))
    bus.post(new AuthDataChanged)
  }

  def getPermissions(group: String*) =
    permStore.flatMap(store => store.getPermissions(group: _*)).toSet

  def getUserPermissions(login: String) = permStore.flatMap(_.getUserPermissions(login)).toSet

  def getAllUserPermissions(login: String) =
    getGroups(login).flatMap(group => getPermissions(group)) ++ permStore.flatMap(_.getUserPermissions(login))

  def addGroup(login: String, group: String) {
    findUserStores(login) map { store => store.addGroup(login, group) }
    bus.post(new AuthDataChanged)
  }

  def dropGroup(login: String, group: String) {
    findUserStores(login) map { store => store.dropGroup(login, group) }
    bus.post(new AuthDataChanged)
  }

  def getGroups(login: String) = {
    val results = findUserStores(login) flatMap { store => store.getGroups(login) }
    results.toSet
  }

  def anonPatterns = resourceStore.toList.flatMap(store => store.anonPatterns)

  def addAnonPattern(pattern: Glob) {
    resourceStore.map(store => store.addAnonPattern(pattern))
  }

  def removeAnonPattern(pattern: Glob) {
    resourceStore.map(_.removeAnonPattern(pattern))
  }

  def restrictedResources = resourceStore.flatMap(store => store.restrictedResources).toSet

  def updateRestrictedResource(rdef: ResourcePatternDef) = {
    resourceStore.flatMap(store => store.updateRestrictedResource(rdef)).headOption
  }

  def removeRestrictedResource(pattern: Glob) = {
    def recursiveDo(list: List[ResourceSetStore]): Option[ResourcePatternDef] = {
      list match {
        case a::as => a.removeRestrictedResource(pattern) match {
          case v@Some(r) => v
          case None => recursiveDo(as)
        }
        case _ => None
      }
    }
    recursiveDo(resourceStore.toList)
  }
}