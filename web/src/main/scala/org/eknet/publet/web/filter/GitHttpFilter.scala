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

package org.eknet.publet.web.filter

import org.eclipse.jgit.http.server.GitFilter
import javax.servlet.FilterConfig
import org.eknet.publet.web.Config
import org.eclipse.jgit.http.server.resolver.DefaultReceivePackFactory
import javax.servlet.http.HttpServletRequest
import org.eclipse.jgit.lib.Repository
import java.util.Collection
import org.eclipse.jgit.transport.{ReceiveCommand, PostReceiveHook, ReceivePack}
import org.eknet.publet.gitr.{RepositoryName, GitrMan}
import grizzled.slf4j.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 22:56
 */
class GitHttpFilter(gitr: GitrMan) extends GitFilter with HttpFilter with Logging {

  class TandemUpdateHook(name: RepositoryName) extends PostReceiveHook {
    def onPostReceive(rp: ReceivePack, commands: Collection[ReceiveCommand]) {
      gitr.getTandem(name).map { tandem =>
        info("Update work tree at "+ name)
        tandem.updateWorkTree()
      }
    }
  }

  override def init(filterConfig: FilterConfig) {
    setReceivePackFactory(new DefaultReceivePackFactory() {
      override def create(req: HttpServletRequest, db: Repository) = {
        val pack = super.create(req, db)
        val utils = getRequestUtils(req)
        pack.setPostReceiveHook(new TandemUpdateHook(utils.getRepositoryName.get))
        pack
      }
    })
    super.init(new PubletFilterConfig(filterConfig))
  }

  class PubletFilterConfig(val delegate: FilterConfig) extends FilterConfig {
    def getFilterName = delegate.getFilterName

    def getServletContext = delegate.getServletContext

    def getInitParameter(name: String) = {
      if (Set("base-path", "export-all") contains name) {
        val value = Option(delegate.getInitParameter(name))
        value.getOrElse {
          if (name == "base-path") Config.repositories.getAbsolutePath
          else "true"
        }
      } else {
        delegate.getInitParameter(name)
      }
    }

    def getInitParameterNames = delegate.getInitParameterNames
  }
}
