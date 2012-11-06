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

package org.eknet.publet.gitr.auth

import org.eknet.publet.auth.{ResourceAction, PermissionBuilder}
import org.eknet.gitr.RepositoryName

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.11.12 00:02
 */
trait GitPermissionBuilder extends PermissionBuilder {

  def git = new GitDomainPart

  class GitDomainPart extends DomainPart(GitPermissionBuilder.domain) {
    def action(action: GitAction.Action*) =
      new GitActionPart(this, action.map(_.name).mkString(PermissionBuilder.subpartDivider))
    override def grant(next:String*) =
      new GitActionPart(this, next.mkString(PermissionBuilder.subpartDivider))
  }

  class GitActionPart(gp: GitDomainPart, repos:String) extends ActionPart(gp, repos) {
    def on (rn: RepositoryName*) = new InstPart(this, rn.map(_.name).mkString(PermissionBuilder.subpartDivider))
  }

}

object GitPermissionBuilder {
  /**
   * The domain "git"
   */
  val domain = "git"

}
