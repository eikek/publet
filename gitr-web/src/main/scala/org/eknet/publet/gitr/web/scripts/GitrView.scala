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

package org.eknet.publet.gitr.web.scripts

import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import collection.mutable.ListBuffer
import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.gitr.GitrRepository
import ScalaScript._
import GitrControl._
import org.eknet.publet.vfs.{ContentType, Content, Path}
import org.eknet.publet.web.PubletWebContext
import org.eclipse.jgit.revwalk.RevCommit
import org.fusesource.scalate.support.StringTemplateSource
import eu.medsea.mimeutil.MimeUtil
import ContentType._

// Returns a json array containing a tree of a repository
// Expects the following parameter
//   r = repository name
//   h  = ref spec (master, tag/bla, #34da3), defaults to 'master'
//   p = path inside the tree, defaults to '/'

class GitrView extends ScalaScript {

  def serve() = {
    getRepositoryFromParam match {
      case None => makeJson(Map("success"->false, "message"->"No repository found."))
      case Some(repo) => getAction match {
        case "blob" => blobContents(repo)
        case _ => directoryContents(repo)
      }
    }
  }


  def getReadmeHtml(commit: RevCommit, repo: GitrRepository, base:Path, extensions: List[String]): Option[(String, String)] = {
    extensions match {
      case e::es => {
        val filename = "README." +e
        repo.getStringContents(commit.getTree, (base/filename).toRelative.asString) match {
          case Some(readme) => {
            if (GitrControl.nocachingTemplateEngine.extensions.contains(e)) {
              val name = if (markdownExtensions.contains(e)) "readme-"+commit.getId.getName+".md" else "readme-"+commit.getId.getName+"."+e
              val source = new StringTemplateSource(name, readme) {
                override def lastModified = commit.getCommitTime * 1000
              }
              Some(filename, GitrControl.nocachingTemplateEngine.layout(source, Map("layout" -> "")))
            } else {
              Some(filename, "<pre>"+readme+"</pre>")
            }
          }
          case _ => getReadmeHtml(commit, repo, base, extensions.tail)
        }
      }
      case Nil => None
    }
  }

  def directoryContents(repo: GitrRepository) : Option[Content] = {
    val cpPath = getPath
    getCommitFromRequest(repo) map { commit =>
      val lastCommit = repo.getLastCommit(commit.getId.name(), Some(cpPath.toRelative.asString)) map { c =>
        CommitInfo(cpPath.name.fullName, cpPath.asString, false, c, PubletWebContext.getLocale)
      }
      val treewalk = new TreeWalk(repo)
      treewalk.addTree(commit.getTree)
      if (!cpPath.isRoot) treewalk.setFilter(PathFilter.create(cpPath.asString))
      val result= new ListBuffer[CommitInfo]()
      while (treewalk.next()) {
        if (cpPath.prefixedBy(Path(treewalk.getPathString))) {
          treewalk.enterSubtree()
        } else {
          repo.getLastCommit(commit.getId.name(), Some(treewalk.getPathString)) map { lastCommit =>
            result.append(CommitInfo(treewalk, lastCommit, PubletWebContext.getLocale))
          }
        }
      }
      var readmeHtml = getReadmeHtml(commit, repo, cpPath, wikiExtensions.toList ::: markdownExtensions.toList)

      makeJson {
        Map(
          "success"->true,
          "files"-> result.sorted.map(_.toMap),
          "parent" -> !cpPath.isRoot,
          "parentPath"-> (if (cpPath.isRoot) "" else cpPath.parent.asString),
          "containerPath"-> Path(cpPath.segments, true, true).asString,
          "currentHead" -> (commit.getId.toString),
          "lastCommit" -> (lastCommit.map(_.toMap).getOrElse(Map())),
          "readmeFile" -> (readmeHtml.map(_._1).getOrElse("")),
          "readme" -> (readmeHtml.map(_._2).getOrElse(""))
        )
      }
    } getOrElse {
      makeJson(Map("success"->false, "message"->"Repository is empty."))
    }
  }

  def blobContents(repo: GitrRepository):Option[Content] = {
    val file = getPath
    getCommitFromRequest(repo) map { commit =>
      val lastCommit = repo.getLastCommit(commit.getId.name(), Some(file.toRelative.asString)) map { c =>
        CommitInfo(file.name.fullName, file.asString, false, c, PubletWebContext.getLocale)
      }
      val resultBase = Map(
        "success"->true,
        "fileName" -> file.name.fullName,
        "parent" -> !file.isRoot,
        "parentPath"-> (if (file.isRoot) "" else file.parent.asString),
        "containerPath"-> Path(file.segments, true, true).asString,
        "currentHead" -> (commit.getId.toString),
        "lastCommit" -> (lastCommit.map(_.toMap).getOrElse(Map()))
      )
      val mimetype = ContentType.getMimeType(file.name.fullName) match {
        case a@"application/octet-stream" => {
          repo.getObject(commit.getTree, file.asString) flatMap ( in => {
            import collection.JavaConversions._
            MimeUtil.getMimeTypes(in).headOption.map(_.toString)
          }) getOrElse(a)
        }
        case a@_ => a
      }
      if (mimetype.startsWith("text")) {
        //show text contents
        val content = repo.getStringContents(commit.getTree, file.asString).getOrElse("")
        if (wikiExtensions.contains(file.name.ext) || markdownExtensions.contains(file.name.ext)) {
          val name = if (markdownExtensions.contains(file.name.ext)) file.name.withExtension("md") else file.name.fullName
          val source = new StringTemplateSource(commit.getId.name()+"."+name, content) {
            override def lastModified = commit.getCommitTime * 1000
          }
          makeJson(resultBase ++ Seq("processed"->true, "source"-> content, "contents" -> GitrControl.nocachingTemplateEngine.layout(source, Map("layout"->""))))
        } else {
          makeJson {
            resultBase ++ Seq("processed"->false, "contents" -> (scala.xml.Utility.escape(content)), "mimeType" -> mimetype)
          }
        }
      } else {
        //provide download link or show image
        val url = PubletWebContext.urlOf("/gitr/gitrblob?"+rParam+"="+repo.name.name+"&"+hParam+"="+getRev+"&"+pParam+"="+file.asString)
        makeJson {
          resultBase ++ Seq("mimeType" -> mimetype, "url"->url)
        }
      }
    } getOrElse {
      makeJson(Map("success"->false, "message"->"Repository is empty."))
    }
  }
}
