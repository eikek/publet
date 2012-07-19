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

import scala.xml._


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 14:38
 */
final class User(
    val login: String,
    val password: Array[Char],
    val algorithm: Option[String],
    val groups: Set[String],
    val properties: Map[String, String]
) extends Serializable {
  require(login != null && !login.isEmpty)

  def getProperty(name: UserProperty.Value) = properties.get(name.toString)
  def isEnabled = !getProperty(UserProperty.enabled).exists(_ == "false")

  def toXml: Elem = {
    val props = for (uv <- UserProperty.values if (properties.keySet.contains(uv.toString))) yield {
      <a>{getProperty(uv).get}</a>.copy(label = uv.toString)
    }
    val gs = groups.map(g => <group>{g}</group>)

    val user = <user login={login} password={ new String(password) }>
      { props ++ gs }
    </user>
    if (algorithm.isDefined) {
      user % Attribute("algorithm", Text(algorithm.get), Null)
    } else {
      user
    }
  }

  override def equals(obj: Any) = obj match {
    case u:User => login == u.login
    case _ => false
  }

  override def hashCode() = login.hashCode

  override def toString = "User("+login+", ***, "+ groups +", "+ properties+")"

}

object User {

  def apply(login: String, pw: Array[Char], alg: Option[String], groups: Set[String], props:Map[String,String]): User = {
    new User(login, pw, alg, groups, props)
  }

  def apply(userN: Node): User = {
    val login = (userN \ "@login").text
    val pw = (userN \ "@password").text.toCharArray
    val alg = Option((userN \ "@algorithm").text).filter(!_.isEmpty)
    val attr = (for (c <- userN.child if (UserProperty.exists(c.label))) yield (c.label -> c.text)).toMap
    val groups = (for (c <- userN \ "group") yield c.text).toSet
    User(login, pw, alg, groups, attr)
  }

}
