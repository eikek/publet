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

import collection.mutable
import scala.xml.{Node, Elem}
import org.eknet.publet.auth.Permission

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.05.12 19:20
 */
case class PermissionModel(groups: Set[String], perms: Set[String]) {

  def toPermissions: Map[String, Set[Permission]] = {
    val tuples = for (g <- groups) yield g -> perms.map(Permission(_))
    tuples.toMap
  }
}

