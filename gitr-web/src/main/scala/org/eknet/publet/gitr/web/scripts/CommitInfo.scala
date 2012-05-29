package org.eknet.publet.gitr.web.scripts

import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.revwalk.RevCommit
import java.util.concurrent.TimeUnit

class CommitInfo(tree: TreeWalk, commit: RevCommit) extends Ordered[CommitInfo] {
  val name = tree.getNameString
  val path = tree.getPathString
  val container = tree.isSubtree
  val author = commit.getAuthorIdent.getName
  val message = commit.getFullMessage.split("\n")(0)
  val age = Duration(TimeUnit.SECONDS.toMillis(commit.getCommitTime)).distanceAgo

  def toMap: Map[String, Any] = Map(
    "name" -> name,
    "container" -> container,
    "author" -> author,
    "message" -> message,
    "age" -> age,
    "icon" -> (if (container) "icon-folder-close" else "icon-file")
  )

  def compare(that: CommitInfo) = {
    if (container == that.container) {
      name.compare(that.name)
    } else {
      if (container) -1
      else +1
    }
  }

  override def toString = "CommitInfo[" + name + ":" + container + "]"
}
