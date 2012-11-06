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
 * @since 03.11.12 19:41
 */
object ResourceAction extends Enumeration {

  val read = Action("read")
  val write = Action("write")
  val all = Action("*")

  case class Action(name: String) extends Val(name)
}
