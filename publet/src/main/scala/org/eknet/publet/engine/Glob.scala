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

package org.eknet.publet.engine

/**
 * Supports only `?` and a single `*`
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 13:17
 */
protected[engine] case class Glob(pattern: String) extends Ordered[Glob] {

  //create a regex and match against that
  lazy val regex = {
    val r = "^" + pattern.replaceAll("\\?", ".?").replaceAll("\\*", ".*")
    if (!pattern.endsWith("*")) {
      (r+ "$").r
    } else if (!pattern.startsWith("*")) {
      ("^" + r).r
    } else {
      r.r
    }
  }

  def matches(str: String): Boolean = regex.findFirstIn(str).isDefined

  def compare(that: Glob) = pattern.length().compare(that.pattern.length())

}
