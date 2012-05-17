package org.eknet.publet.partition.git

import org.eclipse.jgit.api.Git
import org.eknet.publet.vfs._
import java.io.{FileOutputStream, File}
import org.eclipse.jgit.lib.Repository
import org.eknet.publet.gitr.{RepositoryName, GitrMan}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.12 16:38
 */
class GitPartManImpl(val gitr: GitrMan) extends GitPartMan {

  def create(location: Path, config: Config) = {
    val tandem = gitr.createTandem(RepositoryName(location.asString), config.branch)

    //create some initial content
    val init = config.initial.getOrElse(initialResources)
    createInitialContent(tandem.workTree, init)
    tandem.pushToBare()

    new GitPartition(tandem)
  }

  def get(location: Path) = {
    gitr.getTandem(RepositoryName(location.asString)) map {
      new GitPartition(_)
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
      t._2.copyTo(new FileOutputStream(file))

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
