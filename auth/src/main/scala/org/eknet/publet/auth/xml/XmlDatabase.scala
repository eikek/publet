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
import com.google.inject.name.Named
import org.eknet.publet.vfs.events.ContentWrittenEvent
import scala.Some

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.05.12 21:57
 */
@Singleton
class XmlDatabase @Inject() (@Named("contentroot") container: Container) extends UserStore with PermissionStore with ResourceSetStore with Logging {

  val data = new XmlData(
    source = container
      .lookup(Publet.allIncludesPath / "config" / "permissions.xml")
      .collect({case c:ContentResource=>c})
      .getOrElse(Resource.emptyContent(ResourceName("permissions.xml"), ContentType.xml))
  )

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

  def updateUser(user: User) = data.modify { data =>
    val old = data.users.get(user.login)
    data.users = data.users + (user.login -> user)
    old
  }

  def removeUser(login: String) = data.modify { data =>
    val old = data.users.get(login)
    data.users = data.users - login
    old
  }

  def addPermission(group: String, perm: String) {
    data.modify { data =>
      val set = data.permissions.get(group).getOrElse(Set[String]()) + perm
      data.permissions = data.permissions + (group -> set)
    }
  }

  def dropPermission(group: String, perm: String) {
    data.modify { data =>
      data.permissions.get(group) match {
        case Some(set) => data.permissions = data.permissions + (group -> (set - perm))
        case _ =>
      }
    }
  }

  def getUserPermissions(user: User) = Set[String]()

  def getPermissions(groups: String*) =
    groups.flatMap(g => data.permissions.get(g).getOrElse(Set[String]())).toSet

  def addGroup(login: String, group: String) {
    data.modify { data =>
      val set = data.groups.get(login).getOrElse(Set[String]())
      data.groups = data.groups + (login -> (set + group))
    }
  }

  def dropGroup(login: String, group: String) {
    data.modify { data =>
      data.groups.get(login) match {
        case Some(set) => data.groups = data.groups + (login -> (set- group))
        case _ =>
      }
    }
  }

  def getGroups(login: String) = data.groups.get(login).getOrElse(Set())

  def anonPatterns = data.anonPatterns.map(Glob(_))

  def addAnonPattern(pattern: Glob) {
    data.modify { data =>
      data.anonPatterns = pattern.pattern :: data.anonPatterns
    }
  }

  def removeAnonPattern(pattern: Glob) {
    data.modify { data =>
      data.anonPatterns = data.anonPatterns.filterNot(_ == pattern.pattern)
    }
  }

  def containsAnonPattern(pattern: Glob) = data.anonPatterns.contains(pattern.pattern)

  def restrictedResources = data.restrictedPatterns.toSet

  def updateRestrictedResource(rdef: ResourcePatternDef) = data.modify {data =>
    val old = data.restricted.find(_.pattern == rdef.pattern)
    data.restricted = rdef :: old.map(o => data.restricted.filterNot(_ == o)).getOrElse(data.restricted)
    old
  }

  def removeRestrictedResource(pattern: Glob) = data.modify { data =>
    val old = data.restricted.find(_.pattern == pattern)
    data.restricted = data.restricted.filterNot(_.pattern == pattern)
    old
  }
}
