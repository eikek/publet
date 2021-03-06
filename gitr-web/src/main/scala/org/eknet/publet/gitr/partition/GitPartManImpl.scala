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

package org.eknet.publet.gitr.partition

import com.google.common.eventbus.{Subscribe, EventBus}
import java.io.{FileOutputStream, File}
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eknet.publet.vfs._
import org.eknet.publet.vfs.events.{ContentDeletedEvent, ContentWrittenEvent}
import com.google.inject.{Inject, Singleton}
import org.eknet.gitr.{Tandem, RepositoryName, GitrMan}
import org.eknet.publet.gitr.auth.DefaultRepositoryStore


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.12 16:38
 */
@Singleton
class GitPartManImpl @Inject() (val gitr: GitrMan, bus: EventBus, repoStore: DefaultRepositoryStore) extends GitPartMan {

  bus.register(this)

  private val partitionProperty = "gitPartition"

  @Subscribe
  def updateRepository(event: ContentWrittenEvent) {
    event.resource match {
      case gitfile: GitFile => gitfile.commitWrite(event.changeInfo)
      case _ =>
    }
  }

  @Subscribe
  def updateRepsitoryOnDelete(event: ContentDeletedEvent) {
    event.resource match {
      case gitfile: GitFile => gitfile.commitDelete()
      case _ =>
    }
  }

  def getAllPartitions = {
    gitr.allRepositories(x=>true)
      .collect({case r if (r.isTandem) => gitr.getTandem(r.name)})
      .flatten
      .map(tandem => new GitPartition(tandem, bus, repoStore))
  }

  def create(location: Path, config: Config) = {
    val tandem = gitr.createTandem(RepositoryName(location.asString), config.branch)

    //create some initial content
    val init = config.initial.getOrElse(initialResources)
    createInitialContent(tandem.workTree, init)
    tandem.pushToBare()

    val gitconf = tandem.workTree.getConfig
    gitconf.setBoolean("publet", null, partitionProperty, true)
    gitconf.save()

    val gitp = new GitPartition(tandem, bus, repoStore)
    gitp
  }

  def isGitPartition(tandem: Tandem): Boolean = {
    val conf = tandem.workTree.getConfig
    conf.getBoolean("publet", null, partitionProperty, false)
  }

  def get(location: Path) = {
    gitr.getTandem(RepositoryName(location.asString)) filter (isGitPartition) map { tandem =>
      new GitPartition(tandem, bus, repoStore)
    }
  }

  def getOrCreate(location: Path, config: Config) = {
    get(location) getOrElse {
      create(location, config)
    }
  }

  def setExportOk(location: Path, flag: Boolean) = get(location).get.tandem.setExportOk(flag)

  def isExportOk(location: Path) = get(location).get.tandem.isExportOk

  private def createInitialContent(ws: Repository, init: Map[Path, Content]) {
    val git = Git.wrap(ws)
    for (t <- init) {
      val file = t._1.segments.foldLeft(ws.getWorkTree)((file, seg) => new File(file, seg))
      if (!file.getParentFile.exists()) file.getParentFile.mkdirs()
      t._2.copyTo(new FileOutputStream(file), close = true)

      val pattern = t._1.segments.mkString(File.separator)
      git.add().addFilepattern(pattern).setUpdate(false).call()
    }

    git.commit()
      .setAuthor("Publet Install", "none@none")
      .setMessage("Initial sample content.")
      .setAll(true)
      .call()
  }

  private lazy val initialResources = {
    val index =
      """<div class="page-header"><h1>Welcome</h1></div>
        | <div class="alert alert-success">Publet installation was successful!</div>
        | Publet has been succesfully installed. You're viewing its sample page
        | right now. Please have a look at the [user guide](../publet/doc/index.html) to get started.
        """.stripMargin
    val nav =
      """- def urlOf(str: String) = PubletWebContext.urlOf(str)
        |- val path = PubletWebContext.getResourceUri
        |- val loginUrl = urlOf("/publet/templates/login.html") + "?redirect="+ urlOf(PubletWebContext.applicationUri)
        |- val logoutUrl = urlOf("/publet/scripts/logout.json")+ "?redirect=" + urlOf("/")
        |- val editUrl = urlOf("/publet/webeditor/scripts/edit.html")+ "?resource="+path
        |#nav.navbar.navbar-fixed-top
        |  .navbar-inner
        |    .container
        |      a(class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse")
        |        span(class="icon-bar")
        |        span(class="icon-bar")
        |        span(class="icon-bar")
        |      a(class="brand" href={ urlOf("/") }) =PubletWeb.publetSettings("applicationName").getOrElse("Project")
        |      .nav-collapse
        |        ul.nav
        |          - if (isResourceEditable)
        |            li
        |              a(href={ editUrl }) Edit
        |        ul.nav.pull-right
        |          - if (Security.isAuthenticated)
        |            li
        |              .btn-group
        |                a.btn.dropdown-toggle(data-toggle="dropdown" href="#")
        |                  i.icon-user
        |                  =Security.username
        |                  span.caret
        |                ul.dropdown-menu
        |                  li
        |                    a(href={ logoutUrl })
        |                      i.icon-hand-right
        |                      | Logout
        |          - if (!Security.isAuthenticated)
        |            li
        |              .btn-group
        |                a.btn(href={ loginUrl })
        |                  i.icon-user
        |                  | Login
      """.stripMargin
    Map(
      Path("/index.md") -> Content(index, ContentType.markdown),
      Path("/.allIncludes/nav.jade") -> Content(nav, ContentType.jade)
    )
  }
}
