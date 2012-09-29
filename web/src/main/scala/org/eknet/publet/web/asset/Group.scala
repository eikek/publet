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

package org.eknet.publet.web.asset

import org.eknet.publet.vfs.{Container, ContentResource}
import org.eknet.publet.Glob

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.09.12 20:38
 */
final case class Group(name: String,
                  pathPattern: Glob = Glob("**"),
                  resources: List[ContentResource] = Nil,
                  befores: Set[String] = Set(),
                  afters: Set[String] = Set(),
                  uses: Set[String] = Set()) {

  /**
   * Creates a new group with the resource.
   *
   * @param r
   * @return
   */
  def add(r: ContentResource): Group =
    Group(name, pathPattern, r :: resources, befores, afters, uses)

  /**
   * Creates a new group with the given resources.
   * @param rs
   * @return
   */
  def add(rs: Iterable[ContentResource]): Group =
    Group(name, pathPattern, rs.toList ::: resources, befores, afters, uses)

  /**
   * Creates a new group with all child resources of the container whose
   * name ends in `.js`.
   *
   * @param c
   * @return
   */
  def add(c: Container): Group = {
    val list = c.children.collect({ case r: ContentResource => r})
    add(list)
  }

  def forPath(glob: String) =
    Group(name, Glob(glob), resources, befores, afters, uses)

  /**
   * Specifies the given group to be included _after_ this group or in
   * other words this group comes _before_ the given one.
   * @param group
   * @return
   */
  def before(group: String*) =
    Group(name, pathPattern, resources, befores ++ group, afters, uses)

  /**
   * Specifies the given group to be included _before_ this group. In other
   * words this group comes _after_ the given one.
   * @param group
   * @return
   */
  def require(group: String*) =
    Group(name, pathPattern, resources, befores, afters ++ group, uses)

  /**
   * Specifies to use the given group in this one. That means that the given
   * group is just added as a child. The order how childs are finally loaded
   * is determined by `before` and `after`.
   *
   * @param group
   * @return
   */
  def use(group: String*) =
    Group(name, pathPattern, resources, befores, afters, uses ++ group)
}
