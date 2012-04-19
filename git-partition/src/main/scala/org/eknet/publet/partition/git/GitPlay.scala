package org.eknet.publet.partition.git

import java.io.File
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import collection.JavaConversions._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.04.12 23:37
 */
object GitPlay {

  val part = new GitPartition('id, new File("/tmp/testrepo"), "myrepo")

  def main(args: Array[String]) {
    println(part.bareRepo.getRef("HEAD"))
    part.test("xaus231.test")
    println(part.bareRepo.getRef("HEAD"))

    val git = new Git(part.bareRepo)
    println("---")
    git.branchList().setListMode(ListMode.ALL).call().foreach(println)
  }
}
