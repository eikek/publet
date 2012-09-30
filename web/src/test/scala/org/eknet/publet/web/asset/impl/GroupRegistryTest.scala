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

package org.eknet.publet.web.asset.impl

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import org.eknet.publet.web.asset._
import ResourceHelper._
import org.eknet.publet.vfs.Path._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 16:30
 */
class GroupRegistryTest extends FunSuite with ShouldMatchers with BeforeAndAfter  {

  var reg: GroupRegistry = _

  before {
    reg = new GroupRegistry
  }

  test ("merge group resources") {
    reg setup Group("test").add(resource("jquery/jquery-1.8.2.min.js"))
    reg setup Group("test").add(resource("jquery/jquery.form.js"))
    reg.getSources("test", Some("/".p), Kind.js) should have size (2)
  }

  test ("merge group uses") {
    reg setup (jqueryGroup, spinGroup, loadmaskGroup, publetGroup, highlightGroup, bootstrapGroup)
    reg setup Group("default").use("bootstrap", "highlightjs")
    reg setup Group("default").use("jquery.loadmask", "publet")
    val sources = reg.getSources("default", Some("/".p), Kind.js)
    sources should have size 7
  }
}
