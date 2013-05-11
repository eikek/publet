package org.eknet.publet.gitr

import java.nio.file.{Path => JPath, FileVisitResult, SimpleFileVisitor, StandardCopyOption, Files}
import org.slf4j.LoggerFactory
import org.eclipse.jgit.api.Git
import scala.util.Try
import org.eclipse.jgit.transport.RefSpec
import java.io.{FileWriter, File}
import org.eclipse.jgit.lib.RepositoryCache
import org.eclipse.jgit.util.FS
import java.nio.file.DirectoryStream.Filter
import java.nio.file.attribute.BasicFileAttributes
import scala.Some
import scala.util.Success
import org.eknet.publet.content.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 01:36
 */
class GitrManager(root: JPath) {

  Files.createDirectories(root)

  import org.eknet.publet.content.FsPath._

  private def repoFile(name: RepoName) = root.resolve(name.dotGit.toString)
  private def workTree(name: RepoName) = root.resolve(name.simpleName.toString)

  def get(name: RepoName) = {
    findRepo(name, repoFile).orElse(findRepo(name, workTree))
  }

  private def findRepo(name: RepoName, fun: RepoName => JPath) = {
    val file = fun(name)
    if (file.exists) {
      Some(GitrRepository(Git.open(file.toFile).getRepository, name))
    } else {
      None
    }
  }

  def create(name: RepoName, bare: Boolean = true) = Try {
    val file = if (bare) repoFile(name) else workTree(name)
    if (file.exists) sys.error(s"The repository with name $name already exists.")
    val repo = Git.init().setBare(bare).setDirectory(file.toFile).call().getRepository
    GitrRepository(repo, name)
  }

  def clone(source: RepoName, target: RepoName, bare: Boolean = false) = Try {
    val repo = get(source).get
    val file = if (bare) repoFile(target) else workTree(target)
    if (file.exists) sys.error(s"The repository with name $target already exists")
    val cloned = Git.cloneRepository()
      .setBare(bare)
      .setCloneAllBranches(true)
      .setURI(repo.getDirectory.toURI.toString)
      .setDirectory(file.toFile)
      .call()
      .getRepository
    GitrRepository(cloned, target)
  }

  def delete(name: RepoName) = Try {
    get(name).map(repo => {
      if (repo.isTandem) {
        val td = getTandem(name).get
        repoFile(td.bare.name).deleteDirectory()
        workTree(td.workTree.name).deleteDirectory()
      } else {
        repoFile(name).deleteDirectory()
      }
      true
    }).getOrElse(false)
  }

  def rename(oldname: RepoName, newname: RepoName) = {
    def moveRepo(repo: GitrRepository, name: RepoName) {
      val source = repoFile(repo.name)
      val target = repoFile(name)
      Files.move(source, target, StandardCopyOption.ATOMIC_MOVE)
    }
    val old = get(oldname).getOrElse(sys.error(s"Repository '$oldname' does not exist"))
    if (get(newname).isDefined) sys.error(s"Target repository '$newname' already exists")

    if (oldname != newname) {
      if (old.isTandem) {
        //must move both directories and update the remote
        //I'll go for moving the bare and creating a new working copy
        val td = getTandem(oldname).get
        moveRepo(td.bare, newname)
        createTandemFromBare(get(newname).get, td.branch)
      } else {
        moveRepo(old, newname)
      }
    }
  }

  def allRepositories(f: (RepoName) => Boolean) = {

    def isRepo(f: JPath) = Option(RepositoryCache.FileKey.resolve(f.toFile, FS.detect()))

    val dfilter = new Filter[JPath] {
      def accept(entry: JPath) = {
        entry.isDirectory && entry.getFileName.toString != ".git" &&
         entry.getFileName.toString.endsWith(".git") &&
         isRepo(entry).isDefined
      }
    }

    val buffer = collection.mutable.ListBuffer.empty[JPath]
    def walk() {
      Files.walkFileTree(root, new SimpleFileVisitor[JPath]() {
        override def preVisitDirectory(dir: JPath, attrs: BasicFileAttributes) = {
          if (dfilter.accept(dir)) {
            buffer += dir
          }
          if (isRepo(dir).isDefined) {
            FileVisitResult.SKIP_SUBTREE
          } else {
            FileVisitResult.CONTINUE
          }
        }
      })
    }
    walk()
    buffer.map(p => (RepoName(Path(p).drop(root.getNameCount)), p))
      .withFilter(t => f(t._1))
      .map(t => GitrRepository(Git.open(t._2.toFile).getRepository, t._1))
      .toList
  }

  def createTandem(name: RepoName, branch: String = "master") = {
    val bare = get(name).map(Success(_)).getOrElse(create(name, true))
    bare.flatMap(r => createTandemFromBare(r, branch))
  }

  def createTandemFromBare(bare: GitrRepository, branch: String = "master") = {
    val check = Try(if (!bare.isBare) sys.error(s"Cannot create tandem from non-bare repository $bare"))
    val (base, wsname) = splitName(bare.name)
    lazy val create = clone(bare.name, wsname, bare = false).map(ws => {

      //create some initial content
      val readmeFile = "_README.txt"
      val writer = new FileWriter(new File(ws.getWorkTree, readmeFile))
      writer.write("Initial Readme")
      writer.close()
      ws.git.add().addFilepattern(readmeFile).setUpdate(false).call()
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

      Tandem(RepoName(bare.name.simpleName), bare, ws)
    })

    check.flatMap(x => create)
  }

  def getTandem(name: RepoName) = {
    get(name) filter (_.isTandem) map { bare =>
      val (base, ws) = splitName(name)
      val workTree = get(ws).get
      Tandem(base, bare, workTree)
    }
  }

  private def splitName(name: RepoName): (RepoName, RepoName) = {
    val base = name.simpleName.toString
    val ws = base + "_gitrcheckout"
    (RepoName(base), RepoName(ws))
  }

  def closeAll() {
    allRepositories(r => true).foreach(_.close())
  }
}
