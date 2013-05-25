package org.eknet.publet.gitr

import org.eclipse.jgit.api.ResetCommand
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 00:59
 */
case class Tandem(name: RepoName, bare: GitrRepository, workTree: GitrRepository) extends Logging {

  def updateWorkTree() = {
    synchronized {
      setupRemoteUrl()
      workTree.git.fetch().call()
      workTree.git.reset().setMode(ResetCommand.ResetType.HARD).setRef("origin/"+branch).call()
    }
  }

  def pushToBare() = synchronized {
    setupRemoteUrl()
    workTree.git.push().call()
  }

  def branch = workTree.getBranch

  private[this] def setupRemoteUrl() {
    val remoteUrl = workTree.getConfig.getString("remote", "origin", "url")
    val expected = bare.getDirectory.toURI.toString
    if (remoteUrl != expected) {
      logger.info("Tandem repositories moved. Update remote url ...")
      workTree.getConfig.setString("remote", "origin", "url", expected)
      workTree.getConfig.save()
    }
  }

}