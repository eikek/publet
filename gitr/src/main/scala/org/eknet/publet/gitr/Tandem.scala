package org.eknet.publet.gitr

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 00:59
 */
case class Tandem(name: RepositoryName, bare: GitrRepository, workTree: GitrRepository) {

  //todo check for force-update
  def updateWorkTree() = workTree.git.pull().call()

  def pushToBare() = workTree.git.push().call()

  def branch = workTree.getBranch

}

object Tandem {

  implicit def tandemToRepo(tandem: Tandem): GitrRepository = tandem.bare
}
