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

package org.eknet.publet.gitr.webui.scripts

import GitrControl._
import org.eknet.publet.vfs.ContentType
import org.eknet.publet.web.{ErrorResponse, StreamResponse}
import org.eknet.publet.gitr.GitRequestUtils
import org.eknet.publet.gitr.auth.GitAction
import org.eknet.publet.engine.scala.ScalaScript

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.06.12 10:09
 */
class GitrBlob extends ScalaScript {
  import org.eknet.publet.web.util.RenderUtils.makeJson

  def serve() = {
    getRepositoryFromParam match {
      case None => makeJson(Map("success"->false, "message"->"No repository found."))
      case Some(repo) => {
        getRepositoryModelFromParam.map(GitRequestUtils.checkGitAction(GitAction.pull, _))
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
