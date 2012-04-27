package org.eknet.publet.engine.scala

import org.eknet.publet.vfs.{ContainerResource, Path}
import org.eknet.publet.vfs.fs.FileResource
import org.eknet.publet.{Includes, Publet}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 21:39
 */
class MiniProject(val projectDir: ContainerResource, publet: Publet) {

  projectDir.ensuring(_.exists, "Project path must exist!")

  def libraryDir:Option[ContainerResource] = projectDir.child("lib")
    .collect({case r:ContainerResource if (r.exists) => r})

  // src/main/scala and target/classes to come...

  /**
   * Creates a string containing all files from `$project/lib` using
   * the separator `:`.
   *
   * @return
   */
  def libraryClassPath = libraryDir.get.children
    .collect({case r:FileResource if (r.exists) => r})
    .map(fr=>fr.file.getAbsolutePath)

}

object MiniProject {

  val projectDir = "project/"

  def find(path: Path, publet: Publet, pathPrefix: String): Option[MiniProject] = {
    val max = Path(pathPrefix).size +2
    def findProjectDir(p: Path): Option[MiniProject] = {
      publet.rootContainer.lookup(path /Includes.includesPath /projectDir) match {
        case Some(cr: ContainerResource) if (cr.exists) => Some(new MiniProject(cr, publet))
        case None => if (p.size>max) findProjectDir(p.tail) else None
      }
    }
    findProjectDir(if (path.directory) path else path.parent)
  }

  def rootProject(pathPrefix: String, publet: Publet): Option[MiniProject] =  {
    val path = Path(pathPrefix) / Includes.allIncludesPath / projectDir
    publet.rootContainer.lookup(path)
      .collect({case cr:ContainerResource if (cr.exists)=> cr})
      .map(cont => new MiniProject(cont, publet))
  }
}
