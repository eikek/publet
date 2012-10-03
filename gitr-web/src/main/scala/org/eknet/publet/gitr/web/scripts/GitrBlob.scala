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

import org.eknet.publet.engine.scala.ScalaScript
import org.eknet.publet.engine.scala.ScalaScript._
import GitrControl._
import org.eknet.publet.vfs.ContentType
import org.eknet.publet.web.{ErrorResponse, StreamResponse}
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.auth.GitAction

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.06.12 10:09
 */
class GitrBlob extends ScalaScript {
  def serve() = {
    getRepositoryFromParam match {
      case None => makeJson(Map("success"->false, "message"->"No repository found."))
      case Some(repo) => {
        getRepositoryModelFromParam.map(Security.checkGitAction(GitAction.pull, _))
        getCommitFromRequest(repo) flatMap { commit =>
          val file = getPath
          repo.getBlobLoader(commit.getTree, file.asString) map (loader => {
            val in = loader.openStream()
            val len = loader.getSize
            val mime = ContentType.getMimeType(file.name.fullName)
            Some(new StreamResponse(in, mime, Some(len), file.name.fullName))
          })
        } getOrElse {
          Some(ErrorResponse.notFound)
        }
      }
    }
  }
}
