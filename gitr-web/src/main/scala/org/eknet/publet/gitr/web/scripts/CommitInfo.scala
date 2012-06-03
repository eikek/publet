package org.eknet.publet.gitr.web.scripts

import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.revwalk.RevCommit
import java.util.concurrent.TimeUnit
import org.eclipse.jgit.lib.PersonIdent

case class CommitInfo(name: String, path: String, container: Boolean, author: PersonIdent, fullMessage: String, commitTime: Int, id: String) extends Ordered[CommitInfo] {

  def toMap: Map[String, Any] = Map(
    "name" -> name,
    "container" -> container,
    "author" -> author.getName,
    "authorEmail" -> author.getEmailAddress,
    "message" -> CommitInfo.getShortMessage(fullMessage),
    "fullMessage" -> fullMessage,
    "age" -> (Duration(TimeUnit.SECONDS.toMillis(commitTime)).distanceAgo),
    "icon" -> (if (container) "icon-folder-close" else "icon-file"),
    "gravatar" -> Gravatar.imageUrl(author.getEmailAddress).toString,
    "id" -> id.substring(0, 8),
    "fullId" -> id
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

object CommitInfo {

  private def getShortMessage(msg: String): String = {
    val line = msg.split("\n")(0)
    if (line.length > 70) line.substring(0, 70) + "..."
    else line
  }

  def apply(tree: TreeWalk, commit: RevCommit): CommitInfo = {
    CommitInfo(tree.getNameString,
      tree.getPathString,
      tree.isSubtree,
      commit.getAuthorIdent,
      commit.getFullMessage,
      commit.getCommitTime,
      commit.getId.getName)
  }

  def apply(name: String, path: String, container: Boolean, commit: RevCommit): CommitInfo = {
    CommitInfo(name,
      path,
      container,
      commit.getAuthorIdent,
      commit.getFullMessage,
      commit.getCommitTime,
      commit.getId.getName)
  }
}
