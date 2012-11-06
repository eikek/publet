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

import org.eknet.publet.auth.user.{PermissionStore, UserStore}
import org.eknet.publet.auth.PermissionBuilder._
import org.eknet.publet.auth.PermissionBuilder

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.11.12 22:47
 */
class GitPermissionStore(db: RepositoryStore) extends PermissionStore with GitPermissionBuilder {
  import GitAction._

  def getPermissions(login: String) = {
    val permset = collection.mutable.Set[String]()
    for (rm <- db.allRepositories if (rm.owner == login)) {
      permset += git.action(pull,push,edit) on rm.name
    }
    permset.toSet
  }

  def addPermission(login: String, perm: String) {}

  def dropPermission(login: String, perm: String) {}
}
