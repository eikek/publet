package org.eknet.publet.gitr

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import java.io.{FileWriter, File}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 00:58
 */
trait GitrTandem {
  this: GitrManListenerSupport with GitrMan =>

  def createTandem(name: RepositoryName, branch: String = "master"): Tandem = {
    val bareName = name.toDotGit
    val bare = getOrCreate(bareName, true)
    createTandemFromBare(bare, branch)
  }

  def createTandemFromBare(bare: GitrRepository, branch: String = "master"): Tandem = {
    if (!bare.isBare) sys.error("Cannot createa  tandem from a non-bare repository: "+ bare.name)
    val split = splitName(bare.name)
    val ws = clone(bare.name, split._2, false)

    //create some initial content
    val writer = new FileWriter(new File(ws.getWorkTree, "_README.txt"))
    writer.write("Initial Readme")
    writer.close()
    ws.git.add().addFilepattern("README").setUpdate(false).call()
    ws.git.commit()
      .setAuthor("Publet Install", "none@none")
      .setMessage("Initial commit.")
      .setAll(true)
      .call()

    //rename branch, in case different from "master"
    val git = Git.wrap(ws)
    if (branch != "master") {
      git.branchRename()
        .setOldName("master")
        .setNewName(branch)
        .call()
    }
    //push initial content to bare repo
    git.push()
      .setRemote("origin")
      .setRefSpecs(new RefSpec(branch))
      .call()

    //add remote tracking
    val cfg = ws.getConfig
    cfg.setString("branch", branch, "remote", "origin")
    cfg.setString("branch", branch, "merge", "refs/heads/"+ branch)
    cfg.setBoolean("gitr", null, "tandem", true)
    cfg.save()

    val bareCfg = bare.getConfig
    bareCfg.setBoolean("gitr", null, "tandem", true)
    bareCfg.save()

    emit(Tandem(split._1, bare, ws))
  }

  private def splitName(name: RepositoryName): (RepositoryName, RepositoryName) = {
    val base = name.toDotGit.name.replaceAll("\\.git$", "")
    val ws = base + "_gitrcheckout"
    (RepositoryName(base), RepositoryName(ws))
  }

  def getTandem(name: RepositoryName): Option[Tandem] = {
    val bareName = name.toDotGit
    get(bareName) filter (_.isTandem) map { bare =>
      val split = splitName(bareName)
      val workname = split._2
      val workTree = get(workname).get
      Tandem(split._1, bare, workTree)
    }
  }
}
