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

package org.eknet.publet.web

import java.util.Locale

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.05.12 19:20
 */
object Method extends Enumeration {

  val options = MethodValue("OPTIONS")
  val head = MethodValue("HEAD")
  val get = MethodValue("GET")
  val post = MethodValue("POST")
  val delete = MethodValue("DELETE", write = true)
  val propfind = MethodValue("PROPFIND")
  val proppatch = MethodValue("PROPPATCH", write = true)
  val mkcol = MethodValue("MKCOL", write = true)
  val mkcalendar = MethodValue("MKCALENDAR", write = true)
  val copy = MethodValue("COPY", write = true)
  val move = MethodValue("MOVE", write = true)
  val lock = MethodValue("LOCK", write = true)
  val unlock = MethodValue("UNLOCK", write = true)
  val put = MethodValue("PUT", write = true)
  val trace = MethodValue("TRACE")
  val acl = MethodValue("ACL", write = true)
  val connect = MethodValue("CONNECT", write = true)
  val report = MethodValue("REPORT")

  def forName(name: String): Method.MethodValue =
    withName(name.toUpperCase(Locale.ROOT)).asInstanceOf[MethodValue]

  case class MethodValue(name: String, write: Boolean = false) extends Val(name)
}
