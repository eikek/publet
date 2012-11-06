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

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.05.12 23:58
 */
object GitAction extends Enumeration {

  val pull = Action("pull")
  val push = Action("push")
  val edit = Action("edit")
  val create = Action("create")
  val createRoot = Action("createroot")

  val all = Action("*")

  case class Action(name:String) extends Val(name)

}
