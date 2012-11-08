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

import java.util
import com.google.inject.{Inject, Singleton}
import org.eknet.gitr.RepositoryName

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.11.12 16:14
 */
@Singleton
class DefaultRepositoryStore extends RepositoryStore {
  import collection.JavaConversions._

  private var _repoStores: util.Set[RepositoryStore] = util.Collections.emptySet()

  @Inject(optional = true)
  def setRepoStores(stores: util.Set[RepositoryStore]) {
    this._repoStores = stores
  }

  def findRepository(name: RepositoryName) = {
    _repoStores.foldLeft(None:Option[RepositoryModel])((el, store) => if (el.isDefined) el else store.findRepository(name))
  }

  def getRepository(name: RepositoryName) = findRepository(name).getOrElse(RepositoryModel(name, RepositoryTag.open))

  def allRepositories = _repoStores.flatMap(rs => rs.allRepositories)
  def repositoriesByOwner(owner: String) = _repoStores.flatMap(rs => rs.repositoriesByOwner(owner))

  def findRepositoryStore(name: RepositoryName) =
    _repoStores.foldLeft(None:Option[RepositoryStore])((el, store) => if (el.isDefined) el else {
      if (store.findRepository(name).isDefined) Some(store)
      else None
    })


  def updateRepository(rm: RepositoryModel) =
    findRepositoryStore(rm.name).flatMap(store => store.updateRepository(rm))

  def removeRepository(name: RepositoryName) =
    findRepositoryStore(name).flatMap(store => store.removeRepository(name))

}
