package org.eknet.publet.gitr

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


