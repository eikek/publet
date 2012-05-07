package org.eknet.publet.partition.git

import org.eknet.publet.gitr.{GitrMan, RepositoryName}
import org.eclipse.jgit.api.Git
import org.eknet.publet.vfs._
import java.io.{FileOutputStream, File}
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.transport.RefSpec


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.12 16:38
 */
class GitPartManImpl(val gitr: GitrMan) extends GitPartMan {

  private def pathToBareReponame(p: Path): RepositoryName = RepositoryName(p.name.name+".git")
  private def pathToWorkspaceReponame(p: Path): RepositoryName = RepositoryName(p.name.name+"_wc")

  def create(location: Path, config: Config) = {
    val bareName = pathToBareReponame(location)
    val wsName = pathToWorkspaceReponame(location)

    val bare = gitr.create(bareName, true)
    val ws = gitr.clone(bareName, wsName, false)

    //create some initial content
    val init = config.initial.getOrElse(initialResources)
    createInitialContent(ws, init)

    //rename branch, in case different from "master"
    val git = Git.wrap(ws)
    if (config.branch != "master") {
      git.branchRename()
        .setOldName("master")
        .setNewName(config.branch)
        .call()
    }
    //push initial content to bare repo
    git.push()
      .setRemote("origin")
      .setRefSpecs(new RefSpec(config.branch))
      .call()

    //add remote tracking
    val cfg = ws.getConfig
    cfg.setString("branch", config.branch, "remote", "origin")
    cfg.setString("branch", config.branch, "merge", "refs/heads/master")
    cfg.setBoolean("http", null, "receivepack", true)
    cfg.save()

    new GitPartition(bare, ws)
  }

  def get(location: Path) = {
    val bareName = pathToBareReponame(location)
    val wsName = pathToWorkspaceReponame(location)

    val bare = gitr.get(bareName)
    val ws = gitr.get(wsName)
    if (ws.isDefined && bare.isDefined) {
      Some(new GitPartition(bare.get, ws.get))
    } else {
      None
    }
  }

  def getOrCreate(location: Path, config: Config) = {
    get(location) getOrElse {
      create(location, config)
    }
  }

  def setExportOk(location: Path, flag: Boolean) = gitr.setExportOk(pathToBareReponame(location), flag)

  def isExportOk(location: Path) = gitr.isExportOk(pathToBareReponame(location))

  private def createInitialContent(ws: Repository, init: Map[Path, Content]) {
    val git = Git.wrap(ws)
    for (t <- init) {
      val file = t._1.segments.foldLeft(ws.getWorkTree)((file, seg) => new File(file, seg))
      if (!file.getParentFile.exists()) file.getParentFile.mkdirs()
      t._2.copyTo(new FileOutputStream(file))

      git.add().addFilepattern(t._1.segments.mkString(File.separator)).setUpdate(false).call()
    }

    git.commit()
      .setAuthor("Publet Install", "none@none")
      .setMessage("Initial commit.")
      .setAll(true)
      .call()

  }

  private lazy val initialResources = {
    val ct = ContentType.markdown
    val index = """# Welcome

<p class="box success">Publet installation was successful!</p>

Publet has been succesfully installed. You're viewing its sample page
right now. Please have a look at the [user guide](guide) to get started.
"""
    Map(
      Path("/index.md") -> Content(index, ct),
      Path("/.includes/nav.md") -> Content("* [Edit](?a=edit)", ct),
      Path("/.includes/header.md") -> Content("# ${pageTitle}", ct)
    )
  }
}
