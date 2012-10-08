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
import org.eknet.publet.vfs.{ResourceName, Resource}

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
    reg.getSources(Seq("test"), Some("/".p), Kind.js) should have size (2)
  }

  test ("merge group uses") {
    reg setup (jqueryGroup, spinGroup, loadmaskGroup, publetGroup, highlightGroup, bootstrapGroup)
    reg setup Group("default").use("bootstrap", "highlightjs")
    reg setup Group("default").use("jquery.loadmask", "publet")
    val sources = reg.getSources(Seq("default"), Some("/".p), Kind.js)
    sources should have size 7
  }

  test ("transitive dependencies") {
    //a
    val a = Group("a")
      .add(mockResource("1.css"))
      .add(mockResource("1.js"))

    //b
    val b = Group("b")
      .add(mockResource("2.css"))
      .add(mockResource("2.js"))
      .require(a.name)

    //c
    val c = Group("c")
      .add(mockResource("3.css"))
      .add(mockResource("3.js"))
      .require(b.name)

    reg setup (a, b, c)

    val js = reg.getSources(Seq(c.name), None, Kind.js)
    js should have size 3

    val css = reg.getSources(Seq(c.name), None, Kind.css)
    css should have size 3
    toString
  }

  test ("multiple groups resources") {

    reg setup Group("a1")
      .add(mockResource("a1resource1.js"))
      .add(mockResource("a1resource2.js"))

    reg setup Group("a2")
      .add(mockResource("a2resource1.js"))
      .add(mockResource("a2resource2.js"))
      .require("a1")

    reg setup Group("b1")
      .add(mockResource("b1resource1.js"))
      .add(mockResource("b1resource2.js"))
      .require("a1")

    reg setup Group("b2")
      .add(mockResource("b2resource1.js"))
      .add(mockResource("b2resource2.js"))
      .require("b1", "a1")

    val js = reg.getSources(Seq("a2", "b2"), None, Kind.js)
    js should have size 8

  }

  test ("multiple groups uses") {

    reg setup Group("a1")
      .add(mockResource("a1resource1.js"))
      .add(mockResource("a1resource2.js"))

    reg setup Group("a2")
      .add(mockResource("a2resource1.js"))
      .add(mockResource("a2resource2.js"))
      .require("a1")

    reg setup Group("b1")
      .add(mockResource("b1resource1.js"))
      .add(mockResource("b1resource2.js"))

    reg setup Group("b2")
      .add(mockResource("b2resource1.js"))
      .add(mockResource("b2resource2.js"))
      .require("b1")

    reg setup Group("def1")
      .use("a1", "a2")
    reg setup Group("def2")
      .use("b2")

    val js = reg.getSources(Seq("def1", "b1"), None, Kind.js)
    js should have size 6
  }
}
