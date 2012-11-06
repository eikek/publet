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

package org.eknet.publet.gitr.auth

import org.apache.shiro.authz.Permission
import org.apache.shiro.authz.permission.WildcardPermission
import org.eknet.publet.auth.PermissionBuilder

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.11.12 14:11
 */
class GitPermission(str: String) extends WildcardPermission(str) with GitPermissionBuilder {

  import PermissionBuilder._
  import collection.JavaConversions._

  str.ensuring(_.startsWith(GitPermissionBuilder.domain+ partDivider), "Not a valid git permission: " +str)


  def actions = getParts.drop(1).headOption.map(_.toSet)

  override def implies(p: Permission) = {
    if (!p.isInstanceOf[GitPermission])
      false
    else {
      import GitAction._
      // push implies pull.
      actions.map(set => {
        val newset = if (set.contains(push.name)) (set + pull.name) else set
        if (newset.size == set.size)
          super.implies(p)
        else
          newPerm(newset).implies(p)
      }) getOrElse(false)
    }
  }

  private[auth] def newPerm(actions:Set[String]) = {
    val rest = getParts.drop(2).map(s => s.mkString(subpartDivider)).mkString(partDivider)
    new WildcardPermission(git.grant(actions.toSeq: _*).on(rest))
  }
}
