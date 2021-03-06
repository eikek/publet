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

import org.eknet.publet.vfs.Path
import org.eknet.publet.web.util.PubletWeb

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.09.12 18:51
 */
trait AssetManager {

  /**
   * Sets a group with resources. If the group is already
   * defined, the resources are added to it.
   *
   * @param groups
   */
  def setup(groups: Group*)

  /**
   * Replaces all resources of an existing group with the resources
   * of the given group. If `before`, `after` and `uses` are non empty
   * they are used in favor for the existing ones.
   *
   * @param groups
   * @return the groups that have been replaced
   */
  def replace(groups: Group*): Seq[Group]

  /**
   * Returns the path to the compressed `kind` resource that
   * is made of all resources belonging to the given group and
   * that match the given path.
   *
   * If this is the first time the resource is requested, it
   * is created.
   *
   * @param groups a list of group names
   * @param path the path that is used to match groups
   * @param kind the type of resource, see [[org.eknet.publet.web.asset.Kind]]
   * @return
   */
  def getCompressed(groups: Iterable[String], path: Option[Path], kind: Kind.KindVal): Path


  /**
   * Returns a list of path to the resources belonging to the specified
   * group.
   *
   * @param group
   * @param path
   * @param kind
   * @return
   */
  def getResources(group: Iterable[String], path: Option[Path], kind: Kind.KindVal): List[Path]

  /**
   * Returns all registered groups in some order.
   *
   * @return
   */
  def getGroups: Iterable[Group]

}

object AssetManager {

  val assetPath = "/publet/assets/"
  val compressedPath = assetPath + "compressed/"
  val groupsPath = assetPath + "groups"

  /**
   * Retrieves an instance from the web environment.
   *
   * @return
   */
  def service = PubletWeb.instance[AssetManager].get

}