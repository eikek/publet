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
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.11.12 16:57
 */
trait RepositoryStore {

  def findRepository(name: RepositoryName): Option[RepositoryModel]

  def allRepositories: Iterable[RepositoryModel]

  def repositoriesByOwner(owner: String): Iterable[RepositoryModel]

  def updateRepository(rm: RepositoryModel): Option[RepositoryModel]

  def removeRepository(name: RepositoryName): Option[RepositoryModel]
}
