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

package org.eknet.publet.engine.scala

import scala.collection.mutable
import org.eknet.publet.vfs.fs.FileResource
import org.eknet.publet.Publet
import org.eknet.publet.vfs.{Resource, ContainerResource, Path}
import io.Source
import java.io._
import grizzled.slf4j.Logging
import tools.nsc.util.ScalaClassLoader.URLClassLoader
import java.net.URL
import tools.nsc.Global
import tools.nsc.interpreter.AbstractFileClassLoader
import tools.nsc.io.AbstractFile

/**
 *
 * @param projectDir
 * @param dependsOn
 * @param publet
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 21:39
 */
class MiniProject(val path: Path,
                  val projectDir: ContainerResource,
                  val dependsOn: List[MiniProject],
                  publet: Publet) extends Logging {

  import Path._

  projectDir.ensuring(_.exists, "Project path must exist!")

  private val targetDir = new File(MiniProject.tempDir.getAbsolutePath, path.segments.mkString(File.separator))
  if (!targetDir.exists()) targetDir.mkdirs()

  def libraryDir:Option[ContainerResource] = projectDir.child("lib")
    .collect({case r:ContainerResource if (r.exists) => r})

  /**
   * Creates a string containing all files from `$project/lib`
   *
   * It recursively includes all jars from the dependent projects.
   *
   * @return
   */
  def libraryClassPath:List[String] = dependsOn.flatMap(_.libraryClassPath) ++ libraryDir.map(_.children
    .collect({case r:FileResource if (r.exists) => r})
    .map(fr=>fr.file.getAbsolutePath)).getOrElse(List()) ++ List(targetDir.getAbsolutePath)

  def classLoader(): ClassLoader = {
    //compile source files if necessary and return the classloader
    compile()
    val urls = libraryClassPath.map(s=>new URL("file://"+ s))
    val parent = new AbstractFileClassLoader(AbstractFile.getDirectory(targetDir), getClass.getClassLoader)
    new URLClassLoader(urls, parent)
  }

  def compile() {
    synchronized {
      val sourceDir = projectDir.lookup("/src/main/scala".p).collect({case cc:ContainerResource=>cc})
      if (sourceDir.isDefined && needRecompile()) {
        val start = System.currentTimeMillis()
        val settings = ScriptCompiler.compilerSettings(AbstractFile.getDirectory(targetDir), None, Some(this))
        settings.sourcepath.value = (path / "src/main/scala").toAbsolute.segments.mkString(File.separator)
        val reporter = new ErrorReporter(settings)
        val global = new Global(settings, reporter)
        val run = new global.Run
        val sourceFiles = new mutable.ListBuffer[String]()
        projectDir.foreach("src/main/scala".p, (p, r)=> {
          if (r.name.ext=="scala" && r.isInstanceOf[FileResource]) {
            sourceFiles.append(r.asInstanceOf[FileResource].file.getAbsolutePath)
          }
        })
        info("Compiling project with "+ sourceFiles.size+ " files.")
        run.compile(sourceFiles.toList)
        timestamp()
        info("Done in "+ (System.currentTimeMillis()-start) +"ms")
      }
    }
  }

  private def needRecompile(): Boolean = {
    var lastm = -1L
    def findLastm(path: Path, res: Resource) {
      if (path.name.ext=="scala" && res.lastModification.isDefined) {
        if (lastm < res.lastModification.get) {
          lastm = res.lastModification.get
        }
      }
    }
    val last = new File(targetDir, "last")
    if (!last.exists()) {
      true
    } else {
      projectDir.foreach("src/main/scala".p, findLastm)
      Source.fromFile(last).getLines().mkString.toLong < lastm
    }
  }

  private def timestamp() {
    val f = new File(targetDir, "last")
    val out = new BufferedWriter(new FileWriter(f))
    out.write(System.currentTimeMillis() +"")
    out.flush()
    out.close()
  }

}

object MiniProject {
  private val tempDir = new File(System.getProperty("java.io.tmpdir"), "publetprojectout")

  val projectDir = "project/"

  def find(path: Path, publet: Publet, pathPrefix: String): Option[MiniProject] = {
    val max = Path(pathPrefix).size +2
    val root = rootProject(pathPrefix, publet)
    def findProjectDir(p: Path): Option[MiniProject] = {
      val projectPath = p / Publet.includesPath / projectDir
      publet.rootContainer.lookup(projectPath) match {
        case Some(cr: ContainerResource) if (cr.exists) => Some(new MiniProject(projectPath, cr, root, publet))
        case None => if (p.size>max) findProjectDir(p.tail) else root.headOption
      }
    }
    findProjectDir(if (path.directory) path else path.parent)
  }

  def rootProject(pathPrefix: String, publet: Publet): List[MiniProject] =  {
    val path = Path(pathPrefix) / Publet.allIncludesPath / projectDir
    publet.rootContainer.lookup(path)
      .collect({case cr:ContainerResource if (cr.exists)=> cr})
      .map(cont => List(new MiniProject(path, cont, List(), publet))).getOrElse(List())
  }

}
