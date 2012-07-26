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

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.05.12 19:20
 */
object Method extends Enumeration {

  val options = Value("OPTIONS")
  val head = Value("HEAD")
  val get = Value("GET")
  val post = Value("POST")
  val delete = Value("DELETE")
  val propfind = Value("PROPFIND")
  val proppatch = Value("PROPPATCH")
  val mkcol = Value("MKCOL")
  val mkcalendar = Value("MKCALENDAR")
  val copy = Value("COPY")
  val move = Value("MOVE")
  val lock = Value("LOCK")
  val unlock = Value("UNLOCK")
  val put = Value("PUT")
  val trace = Value("TRACE")
  val acl = Value("ACL")
  val connect = Value("CONNECT")
  val report = Value("REPORT")

}
