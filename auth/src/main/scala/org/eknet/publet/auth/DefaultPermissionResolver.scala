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

import org.apache.shiro.authz.permission.{PermissionResolver, WildcardPermissionResolver}
import java.util
import org.apache.shiro.authz.Permission
import com.google.inject.{Inject, Singleton}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.11.12 23:04
 */
@Singleton
class DefaultPermissionResolver @Inject() (resolver: util.Set[PermissionResolver]) extends WildcardPermissionResolver {
  import collection.JavaConversions._

  override def resolvePermission(permissionString: String) = {
    resolvePermission(permissionString, resolver.toList) match {
      case Some(p) => p
      case _ => super.resolvePermission(permissionString)
    }
  }

  private def resolvePermission(str: String, resolver: List[PermissionResolver]): Option[Permission] = {
    resolver match {
      case a::as => Option(a.resolvePermission(str)) match {
        case Some(p) => Some(p)
        case None => resolvePermission(str, as)
      }
      case Nil => None
    }
  }

}
