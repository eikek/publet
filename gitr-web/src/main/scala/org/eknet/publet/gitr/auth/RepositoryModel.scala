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

import org.eknet.gitr.RepositoryName

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 11.05.12 11:18
 */
case class RepositoryModel(name: RepositoryName, tag: RepositoryTag.Value, owner: String) {
  def hasOwner = !owner.isEmpty
}
object RepositoryModel {
  def apply(name: RepositoryName, tag: RepositoryTag.Value): RepositoryModel =
    RepositoryModel(name, tag, "")

  def apply(name: RepositoryName): RepositoryModel =
    RepositoryModel(name, RepositoryTag.open)

  def apply(name: RepositoryName, owner: String): RepositoryModel =
    RepositoryModel(name, RepositoryTag.open, owner)
}