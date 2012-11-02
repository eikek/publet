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

package org.eknet.publet.auth.user

import UserProperty._
import org.eknet.publet.auth.{DigestGenerator, Algorithm}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 14:38
 */
final class User(val login: String, val properties: Map[UserProperty.Value, String]) extends Serializable {
  require(login != null && !login.isEmpty)

  def get(name: UserProperty.Value) = properties.get(name)
  def isEnabled = !get(enabled).exists(_ == "false")

  override def equals(obj: Any) = obj match {
    case u:User => login == u.login
    case _ => false
  }

  override def hashCode() = login.hashCode
  override def toString = "User("+login+", "+ properties.filter(t => !Set(password, digest).contains(t._1))+")"

}

object User {

  def apply(login: String, pw: String, alg: Option[String], digest: String, props:Map[UserProperty.Value, String]): User = {
    val p = collection.mutable.Map[Value, String]()
    props.foreach(t => p += t)
    if (!digest.isEmpty) p.put(UserProperty.digest, digest)
    if (alg.isDefined) p.put(algorithm, alg.get)
    new User(login, p.toMap)
  }

  def apply(login: String, props:Map[UserProperty.Value, String]) = new User(login, props)

}
