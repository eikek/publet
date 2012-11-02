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

import org.apache.shiro.authz.permission.WildcardPermission
import org.apache.shiro.authz.{Permission => ShiroPermission}
import org.eknet.publet.Glob

/**
 * Resource permissions are standard wildcard permissions with the
 * domain `resource` followed by actions and the last part is an
 * url pattern.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.11.12 18:56
 */
final case class ResourcePermission(str: String) extends WildcardPermission(str) {

  import collection.JavaConversions._

  if (!str.startsWith("resource:"))
    sys.error("Not a resource permission: "+ str)

  def actions = getParts.drop(1).headOption.map(_.toSet)
  def patterns = getParts.drop(2).headOption.map(_.toSet)

  override def implies(p: ShiroPermission) = {
    //only for resource permissions
    if (!p.isInstanceOf[ResourcePermission])
      false
    else {
      val rp = p.asInstanceOf[ResourcePermission]
      (actions, rp.actions) match {
        case (Some(ta), Some(oa)) if ((oa.diff(ta)).isEmpty || ta.contains("*")) => {
          //all other patterns must be implied by any of this' patterns
          patternsImplies(patterns.map(_.toList).getOrElse(Nil), rp.patterns.map(_.toList).getOrElse(Nil))
        }
        case _ => false
      }
    }
  }

  /**
   * Returns `true` if all url patterns of `others` are implied by at least
   * one pattern of `patterns`.
   *
   * @param patterns
   * @param others
   * @return
   */
  private def patternsImplies(patterns: List[String], others: List[String]):Boolean = {
    (patterns, others) match {
      case (p::ps, o::os) => if (Glob(p).implies(Glob(o))) true
        else patternsImplies(patterns, os) || patternsImplies(ps, others)

      case _ => false
    }
  }

}
