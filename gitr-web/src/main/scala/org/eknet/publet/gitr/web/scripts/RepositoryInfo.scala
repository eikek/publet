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

import org.eknet.publet.gitr.GitrRepository
import org.eknet.publet.auth.RepositoryTag
import org.eknet.publet.web.PubletWebContext
import org.eknet.publet.web.shiro.Security

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.05.12 20:24
 */
class RepositoryInfo(repo:GitrRepository, val tag: RepositoryTag.Value) {

  val name = repo.name
  val gitUrl = PubletWebContext.urlOf("/git/"+ repo.name.name)
  val owner = repo.name.segments(0)
  val owned = repo.name.segments(0) == Security.username
  val description = repo.getDescription.getOrElse("")

  lazy val toMap: Map[String, Any] = {
    Map(
      "name" -> (name.segments.last),
      "fullName" -> name.name,
      "giturl" -> gitUrl,
      "owner" -> name.segments(0),
      "owned" -> owned,
      "tag" -> tag.toString,
      "description" -> description
    )
  }
}
