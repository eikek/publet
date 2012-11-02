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

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 17:20
 */
case class Permission(domain: String, action: Set[String], inst: Set[String]) {
  import Permission._

  override def toString = domain + setToString(action) + setToString(inst)

  private def setToString(set: Set[String]) =
    if (set.isEmpty) "" else partDivider+set.mkString(subpartDivider)

}
object Permission {

  val wildcardToken = "*"
  val partDivider = ":"
  val subpartDivider = ","

  val all = Set(wildcardToken)

  val gitDomain = "git"
  val resourceDomain = "resource"

  private val regex = """([^:,]+)(:([^:]+))?(:([^:]+))?""".r

  def apply(domain: String, action: String, inst: String):Permission = Permission(domain, Set(action), Set(inst))
  def apply(str: String): Permission = {
    str match {
      case regex(domain, _, action, _, inst) => {
        val act = Option(action).map(_.split(subpartDivider).toSet).getOrElse(Set[String]())
        val ins = Option(inst).map(_.split(subpartDivider).toSet).getOrElse(Set[String]())
        new Permission(domain, act, ins)
      }
      case _ => sys.error("Invalid permission string: "+ str)
    }
  }
  def forGit(action:Set[String], repos: Set[String]) = Permission(gitDomain, action, repos)
}
