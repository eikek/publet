package org.eknet.publet.gitr.webui.scripts

import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.revwalk.RevCommit
import java.util.concurrent.TimeUnit
import org.eclipse.jgit.lib.PersonIdent
import java.text.DateFormat
import java.util.{Locale => JLocale, Date => JDate}
import xml.XML
import org.eknet.publet.web.util.PubletWebContext

case class CommitInfo(name: String, path: String, container: Boolean, author: PersonIdent, fullMessage: String, commitTime: Int, id: String, loc: JLocale) extends Ordered[CommitInfo] {

  def toMap: Map[String, Any] = Map(
    "name" -> name,
    "container" -> container,
    "commitDate" -> getCommitDateAsString,
    "author" -> author.getName,
    "authorEmail" -> author.getEmailAddress,
    "message" -> xml.Utility.escape(getShortMessage),
    "fullMessage" -> xml.Utility.escape(fullMessage),
    "age" -> getAge,
    "icon" -> (if (container) "icon-folder-close" else "icon-file"),
    "gravatar" -> gravatarUrl,
    "id" -> shortId,
    "fullId" -> id
  )

  lazy val shortId = id.substring(0, 8)

  def gravatarUrl = Gravatar.imageUrl(PubletWebContext.urlBase.startsWith("https"))(author.getEmailAddress).toString

  def getAge = (Duration(TimeUnit.SECONDS.toMillis(commitTime)).distanceAgo)

  def getCommitDate = new JDate(TimeUnit.SECONDS.toMillis(commitTime))

  def getShortMessage = CommitInfo.getShortMessage(fullMessage)
  def getLongMessage = CommitInfo.getLongMessage(fullMessage)

  def getCommitDateAsString = {
    val df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, loc)
    df.format(getCommitDate)
  }

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
    if (line.length > 80) line.substring(0, 77) + "..."
    else line
  }

  private def getLongMessage(msg: String): String = {
    val idx = msg.indexOf('\n')
    if (idx > 0) msg.substring(idx +1).trim
    else msg.trim
  }

  def apply(tree: TreeWalk, commit: RevCommit, loc: JLocale): CommitInfo = {
    CommitInfo(tree.getNameString,
      tree.getPathString,
      tree.isSubtree,
      commit.getAuthorIdent,
      commit.getFullMessage,
      commit.getCommitTime,
      commit.getId.getName,
      loc)
  }

  def apply(name: String, path: String, container: Boolean, commit: RevCommit, loc: JLocale): CommitInfo = {
    CommitInfo(name,
      path,
      container,
      commit.getAuthorIdent,
      commit.getFullMessage,
      commit.getCommitTime,
      commit.getId.getName,
      loc)
  }
}
