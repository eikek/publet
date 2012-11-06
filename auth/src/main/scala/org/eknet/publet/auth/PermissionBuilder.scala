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

import org.eknet.publet.Glob

/**
 * Defines helper methods for creating permission strings, trying to avoid
 * using strings where possible.
 *
 * For example:
 *
 * {{{
 *  import GitAction._
 *  val x = git grant(pull, push) on "bla"
 * }}}
 *
 * would generate the string
 *
 * {{{
 *   git:pull,push:bla
 * }}}
 *
 *@author Eike Kettner eike.kettner@gmail.com
 * @since 03.11.12 10:13
 */
trait PermissionBuilder {

  import PermissionBuilder._

  def resource = new ResourceDomainPart

  class DomainPart(part: String) extends PermDef {
    def grant (next: String*): ActionPart =
      new ActionPart(this, next.mkString(subpartDivider))
    def grantAll = new ActionPart(this, all)
    def perm = part
  }
  class ResourceDomainPart extends DomainPart(ResourcePermission.domain) {
    def action(action: ResourceAction.Action*) =
      new ActionPart(this, action.map(_.name).mkString(subpartDivider))
  }

  implicit def strToDomainPart(str: String) = new DomainPart(str)
  implicit def permdefToString(pd: PermDef) = pd.perm

  class ActionPart(dp: DomainPart, part: String) extends PermDef  {
    def on (next: String): InstPart = new InstPart(this, next)
    def perm = dp.perm + partDivider + part
  }

  class InstPart(ap: ActionPart, part: String) extends PermDef  {
    def perm = ap.perm + partDivider + part
  }
  sealed abstract class PermDef {
    def perm: String
  }

  implicit def globToString(glob:Glob) = glob.pattern

}

object PermissionBuilder {

  /**
   * Used for wildcard permissions to match everything for a part.
   */
  val all = "*"

  /**
   * The divider character for wildcard permission parts. Default is ":".
   *
   */
  val partDivider = ":"

  /**
   * The divider character for sub parts. Default is ",".
   */
  val subpartDivider = ","
}