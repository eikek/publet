package org.eknet.publet.partition.git

import java.io.File

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 11:45
 */
trait GitFileTools {

  protected def file: File
  protected def root: GitPartition

  def getFilePattern = {
    file.getAbsolutePath.substring(root.tandem.workTree.getWorkTree.getAbsolutePath.length()+1)
  }

  def git = root.tandem.workTree.git

  def lastCommit = {
    val path = getFilePattern
    val logs = git.log().addPath(path).call().iterator()
    if (logs.hasNext)
      Some(logs.next())
    else
      None
  }

}
