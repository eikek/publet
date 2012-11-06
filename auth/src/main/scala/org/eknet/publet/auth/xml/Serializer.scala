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

import org.eknet.publet.auth.user.{UserProperty, User}
import scala.xml.{Node, Elem}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.11.12 18:02
 */
object Serializer {

  def userToXml(user: User, groups: Set[String]): Elem = {
    val props = for (uv <- UserProperty.values if (user.properties.contains(uv))) yield {
      <a>{user.properties.get(uv).get}</a>.copy(label = uv.toString)
    }
    <user login={ user.login }>
      { props ++ groupsToXml(groups) }
    </user>
  }

  def userFromXml(userN: Node): User = {
    val login = (userN \ "@login").text
    val attr = (for (c <- userN.child if (UserProperty.exists(c.label))) yield (UserProperty.withName(c.label) -> c.text)).toMap
    User(login, attr)
  }

  def groupsFromXml(userN: Node): Set[String] = {
    (for (c <- userN \ "group") yield c.text).toSet
  }

  def groupsToXml(groups: Set[String]) = for (g <- groups) yield <group>{g}</group>

  def permissionToXml(pm: PermissionModel): Elem = {
    <grant>
      { for (g <- pm.groups) yield <to>{g}</to> }
      { for (p <- pm.perms) yield <perm>{ p }</perm> }
    </grant>
  }

  def permissionFromXml(node: Node): PermissionModel = {
    val groups = (node \ "to").map(_.text).toList
    val perms = (node \ "perm").map(_.text).toList
    PermissionModel(groups.toSet, perms.toSet)
  }

}
