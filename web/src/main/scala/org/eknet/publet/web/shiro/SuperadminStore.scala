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

import org.eknet.publet.auth.store.{PermissionStore, UserProperty, User, UserStoreAdapter}
import org.eknet.publet.web.Config
import com.google.inject.{Singleton, Inject}
import org.eknet.publet.auth.DigestGenerator

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 18:44
 */
@Singleton
class SuperadminStore @Inject() (config: Config) extends UserStoreAdapter with PermissionStore {

  lazy val superadmin = new User("superadmin", Map(
    UserProperty.fullName -> "Super Admin",
    UserProperty.email -> "super@admin",
    UserProperty.password -> getPassword,
    UserProperty.digest -> createDigest,
    UserProperty.enabled -> isEnabled
  ))

  private def getPassword = config("superadminPassword").getOrElse("superadmin")
  private def createDigest = DigestGenerator.encodePasswordInA1Format("superadmin", "WebDav Area", getPassword)
  private def isEnabled = config("superadminEnabled").getOrElse("true")

  lazy val enabled = isEnabled.toBoolean

  override def findUser(login: String) = if (enabled && login == "superadmin") Some(superadmin) else None

  override def allUser = if (enabled) List(superadmin) else Nil

  override def userOfGroups(groups: String*) = allUser

  override def getGroups(login: String) = if (enabled && login == "superadmin") Set("superadmin") else Set[String]()

  def addPermission(group: String, perm: String) {}

  def dropPermission(group: String, perm: String) {}

  def getPermissions(group: String*) = Set[String]()

  def getUserPermissions(user: User) = Set("*")
}
