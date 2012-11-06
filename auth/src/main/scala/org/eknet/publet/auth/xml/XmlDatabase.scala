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

package org.eknet.publet.auth.xml


import org.eknet.publet.vfs._
import org.eknet.publet.auth._
import grizzled.slf4j.Logging
import org.eknet.publet.{Publet, Glob}
import org.eknet.publet.auth.store._
import com.google.inject.{Inject, Singleton}
import com.google.common.eventbus.Subscribe
import org.eknet.publet.vfs.events.ContentWrittenEvent
import org.eknet.publet.vfs.events.ContentWrittenEvent
import scala.Some

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.05.12 21:57
 */
class XmlDatabase(source: ContentResource) extends UserStore with PermissionStore with ResourceSetStore with Logging {

  val data = new XmlData(source)

  @Subscribe
  def reloadOnChange(event: ContentWrittenEvent) {
    data.reloadIfChanged()
  }

  //user
  def findUser(login: String) = data.users.get(login)

  def allUser = data.users.values

  def userOfGroups(groups: String*) = {
    val test = groups.toSet
    data.groups.flatMap(t => if (test.subsetOf(t._2)) Some(t._1) else None)
      .map(findUser(_).get)
  }

  def updateUser(user: User) = null

  def removeUser(login: String) = null

  def addPermission(group: String, perm: String) {}

  def dropPermission(group: String, perm: String) {}

  def getUserPermissions(user: User) = Set[String]()

  def getPermissions(groups: String*) =
    groups.flatMap(g => data.permissions.get(g).getOrElse(Set[String]())).toSet

  def addGroup(login: String, group: String) {}

  def dropGroup(login: String, group: String) {}

  def getGroups(login: String) = data.groups.get(login).getOrElse(Set())

  def anonPatterns = data.anonPatterns.map(Glob(_))

  def addAnonPattern(pattern: Glob) {}

  def removeAnonPattern(pattern: Glob) {}

  def containsAnonPattern(pattern: Glob) = data.anonPatterns.contains(pattern.pattern)

  def restrictedResources = null

  def updateRestrictedResource(rdef: ResourcePatternDef) = null

  def removeRestrictedResource(pattern: Glob) = null
}
