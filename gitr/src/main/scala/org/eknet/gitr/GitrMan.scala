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

package org.eknet.gitr

import java.io.File

/**
 * "gitr-man" manages git repositories.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.12 01:58
 */
trait GitrMan extends GitrTandem with GitrManListenerSupport {

  /**
   * Checks whether a repository with the specified name
   * exists.
   *
   * @param name
   * @return
   */
  def exists(name: RepositoryName): Boolean

  /**
   * Returns an existing repository with the specified name.
   *
   * @param name
   * @return
   */
  def get(name: RepositoryName): Option[GitrRepository]

  /**
   * Creates a non-existing repository with the specified name. It
   * throws an exception if such a repository already exists.
   *
   * @param name
   * @param bare
   * @return
   */
  def create(name: RepositoryName, bare: Boolean): GitrRepository

  /**
   * Gets an existing repository or creates a new one if it
   * does not currently exist.
   *
   * @param name
   * @param bare
   * @return
   */
  def getOrCreate(name: RepositoryName, bare: Boolean): GitrRepository

  /**
   * Completely deletes a repository with the specified name. The
   * repository must exists, otherwise an exception is thrown.
   *
   * @param name
   */
  def delete(name: RepositoryName)

  /**
   * Renames the repository with `oldName` to the given `newName`.
   * If the `oldName` repository does not exists, an exception
   * is thrown.
   *
   * @param oldName
   * @param newName
   */
  def rename(oldName: RepositoryName, newName: RepositoryName)

  /**
   * Clones the repository `source` into the repository `target`.
   * The source repository must exist, or an exception is thrown.
   *
   * @param source
   * @param target
   * @return
   */
  def clone(source: RepositoryName, target: RepositoryName, bare: Boolean): GitrRepository

  /**
   * The root containing all repositories.
   *
   * @return
   */
  def getGitrRoot: File

  /**
   * Iterates over all repositories that match
   * the given filter
   *
   * @param f
   * @return
   */
  def allRepositories(f: RepositoryName => Boolean): Iterable[GitrRepository]

  /**
   * Invokes the `close()` method on all repositories.
   *
   */
  def closeAll()

}


