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

package org.eknet.publet.gitr

import org.eclipse.jgit.http.server.GitFilter
import javax.servlet.{FilterChain, ServletResponse, ServletRequest, FilterConfig}
import org.eknet.publet.web.{PubletRequestWrapper, Config}
import org.eclipse.jgit.http.server.resolver.DefaultReceivePackFactory
import javax.servlet.http.{HttpServletRequestWrapper, HttpServletRequest}
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.{ReceiveCommand, PostReceiveHook, ReceivePack}
import grizzled.slf4j.Logging
import org.eknet.publet.vfs.Path
import com.google.common.eventbus.EventBus
import collection.JavaConversions._
import org.eknet.publet.web.util.ClientInfo
import org.eknet.gitr.{Tandem, GitrRepository, RepositoryName, GitrMan}
import org.eknet.publet.gitr.auth.{RepositoryModel, GitAction}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 22:56
 */
class GitHttpFilter(gitr: GitrMan, bus: EventBus) extends GitFilter with PubletRequestWrapper with Logging {

  import GitRequestUtils._

  class TandemUpdateHook(name: RepositoryName, clientInfo: ClientInfo) extends PostReceiveHook {
    def onPostReceive(rp: ReceivePack, commands: java.util.Collection[ReceiveCommand]) {
      gitr.getTandem(name).map { tandem =>
        info("Update work tree at "+ name)
        tandem.updateWorkTree()
        bus.post(new PostReceiveEvent(rp, commands.toSeq, tandem, clientInfo))
      }
    }
  }

  override def init(filterConfig: FilterConfig) {
    setReceivePackFactory(new DefaultReceivePackFactory() {
      override def create(req: HttpServletRequest, db: Repository) = {
        val pack = super.create(req, db)
        pack.setPostReceiveHook(new TandemUpdateHook(req.getRepositoryName.get, ClientInfo(req)))
        pack
      }
    })
    super.init(new PubletFilterConfig(filterConfig))
  }


  override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val req = new PathInfoServletReq(request)
    super.doFilter(req, response, chain)
    bus.post(GitEvent(req.getGitAction.get,
      gitr.get(req.getRepositoryName.get.nameDotGit).get,
      req.getRepositoryModel.get,
      ClientInfo(request)))
  }

  class PubletFilterConfig(val delegate: FilterConfig) extends FilterConfig {
    def getFilterName = delegate.getFilterName

    def getServletContext = delegate.getServletContext

    def getInitParameter(name: String) = {
      if (Set("base-path", "export-all") contains name) {
        val value = Option(delegate.getInitParameter(name))
        value.getOrElse {
          if (name == "base-path") Config.get.repositories.getAbsolutePath
          else "true"
        }
      } else {
        delegate.getInitParameter(name)
      }
    }

    def getInitParameterNames = delegate.getInitParameterNames
  }

  private class PathInfoServletReq(req: HttpServletRequest) extends HttpServletRequestWrapper(req) {

    val gitMount = Config("publet.gitMount").getOrElse("git")

    override def getPathInfo = {
      val len = req.getContextPath.length + gitMount.length +1
      getRequestURI.substring(len)
      //+ (if (getQueryString != null) "?"+getQueryString else "")
    }

    override def getServletPath = Path(gitMount).toAbsolute.asString
  }
}

/**
 * Event posted after every successful git request.
 *
 * @param action
 * @param repository
 */
case class GitEvent(action: GitAction.Value, repository: GitrRepository, repoModel: RepositoryModel, clientInfo: ClientInfo)

/**
 * Event posted after successful receive (git push). The working copy has already been updated.
 *
 * @param rp
 * @param commands
 * @param tandem
 */
case class PostReceiveEvent(rp: ReceivePack, commands: Seq[ReceiveCommand], tandem: Tandem, clientInfo: ClientInfo)