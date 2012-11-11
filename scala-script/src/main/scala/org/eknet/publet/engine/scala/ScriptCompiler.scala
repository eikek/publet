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
import tools.nsc.interpreter.AbstractFileClassLoader
import java.io.File
import scala.tools.nsc._
import tools.nsc.Settings
import tools.nsc.io.AbstractFile
import java.net.{URL, URLClassLoader}
import java.util.jar.JarFile
import util.BatchSourceFile
import org.eknet.publet.vfs._
import grizzled.slf4j.Logging

/**
 * Compiles scala snippets to ScalaScript classes.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 21:31
 */
class ScriptCompiler(target: AbstractFile, classPath: Option[String],
                     imports: List[String]) extends Logging {


  private val lastCompileCache = mutable.Map[Path, Long]()


  def scriptLoader(mp: Option[MiniProject], path: Path, resource: ContentResource): ScalaScript = {
    val settings = ScriptCompiler.compilerSettings(target, classPath, mp)
    val cl = mp.map(_.classLoader()).getOrElse(classOf[MiniProject].getClassLoader)
    val compiler = new Compiler(settings,
      classPath.map(cp => new URLClassLoader(cp.split("\\s*[,;]\\s*").map(new URL(_)), cl)).getOrElse(cl))
    compiler.loadScalaScriptClass(path, resource)
  }

  private def hasChanged(path: Path, script: ContentResource):Boolean = {
    lastCompileCache.get(path).map(_ < script.lastModification.get).getOrElse(true)
  }


  private def className(script: ContentResource) = script.name.name

  private class Compiler(settings: Settings, parentCl: ClassLoader) {
    val reporter = new ErrorReporter(settings, 8+imports.size)
    private val global = new Global(settings, reporter)

    val targetClassLoader: ClassLoader = new AbstractFileClassLoader(target, parentCl)

    def loadScalaScriptClass(path: Path, resource: ContentResource): ScalaScript = {
      val cls = compile(path, resource)
      cls.getConstructor().newInstance().asInstanceOf[ScalaScript]
    }

    def findClass(className: String): Option[Class[_]] = {
      synchronized {
        try {
          val cls = targetClassLoader.loadClass(className)
          Some(cls)
        } catch {
          case e: ClassNotFoundException => None
        }
      }
    }

    private def wrapScript(path: Path, cn: String, script: ContentResource): String = {
      "package " + path.parent.segments.mkString(".") +"\n\n"
      "import org.eknet.publet.engine.scala.ScalaScript\n" +
        (if (!imports.isEmpty)
          imports.mkString("import ", "\nimport ", "\n\n")
        else "") +
        "class "+ cn+ " extends ScalaScript { \n\n" +
        " def serve() = { \n" +
        script.contentAsString() + "\n" +
        " }\n" +
        "}"
    }


    def compile(path: Path, script: ContentResource): Class[_] = {
      val start = System.currentTimeMillis()
      def throwOnError() {
        if (global.reporter.hasErrors) {
          val ex = new CompileException(path, reporter.messages.toList)
          reporter.reset()
          throw ex
        }
        info("Compilation successful in "+ (System.currentTimeMillis()-start)+"ms")
      }

      val filename = script.name.fullName
      val cn = className(script)
      synchronized {
        if (hasChanged(path, script)) {
          info("Script '"+path.asString+"' has changed sinced last compile.")
          compileSource(cn, wrapScript(path, cn, script), filename)
          throwOnError()
          lastCompileCache.put(path, System.currentTimeMillis())
        }

        findClass(cn).get
      }
    }

    private def compileSource(className: String, script: String, fileName: String) {
      info("Creating compiler run...")
      val run = new global.Run
      info("Compiling script: "+ fileName)
      val sfiles = List(new BatchSourceFile(fileName, script))
      run.compileSources(sfiles)
    }

  }
}

object ScriptCompiler extends Logging {

  def compilerSettings(target: AbstractFile, customClasspath: Option[String], mp: Option[MiniProject]): Settings = {
    val settings = new Settings()

    // got weird exception when setting `usejavacp` to true:
    // scala.tools.nsc.FatalError: object List does not have a member apply
    //   at scala.tools.nsc.symtab.Definitions$definitions$.getMember(Definitions.scala:623)
    //   at scala.tools.nsc.symtab.Definitions$definitions$.List_apply(Definitions.scala:320)
    // the problem is a wrong classpath setup. the scala-lib and scala-compiler jar must be
    // the first ones in the class path! see this thread:
    // http://groups.google.com/group/scalate/browse_thread/thread/b17acb9a345badbc/3a9cbc742edf6cda?#3a9cbc742edf6cda
    settings.usejavacp.value = false

    settings.deprecation.value= true
    settings.unchecked.value = true
    settings.bootclasspath.value = (compilerPath ::: libPath).mkString(File.pathSeparator)
    settings.outputDirs.setSingleOutput(target)

    val cp = (libPath::: compilerPath ::: servletPath) ++ impliedClassPath ++ mp.map(_.libraryClassPath).getOrElse(List())
    settings.classpath.value = cp.mkString(File.pathSeparator) + customClasspath.map(File.pathSeparator+ _).getOrElse("")
    settings
  }

  lazy val compilerPath = try {
    jarPathOfClass("scala.tools.nsc.Interpreter")
  } catch {
    case e =>
      throw new RuntimeException("Unable lo load scala interpreter from classpath (scala-compiler jar is missing?)", e)
  }

  lazy val libPath = try {
    jarPathOfClass("scala.ScalaObject")
  } catch {
    case e =>
      throw new RuntimeException("Unable to load scala base object from classpath (scala-library jar is missing?)", e)
  }
  lazy val servletPath = try {
    jarPathOfClass("javax.servlet.Servlet")
  } catch {
    case e => sys.error("Unable to load servlet classpath.")
  }

  /*
  * For a given FQ classname, trick the resource finder into telling us the containing jar.
  */
  private def jarPathOfClass(className: String) = try {
    val resource = className.split('.').mkString("/", "/", ".class")
    val path = getClass.getResource(resource).getPath
    jarPathOfUrlPath(path)
  }

  private def jarPathOfUrlPath(path: String) = {
    val indexOfFile = path.indexOf("file:") + 5
    val indexOfSeparator = path.lastIndexOf('!')
    List(path.substring(indexOfFile, indexOfSeparator))
  }


  /*
   * Try to guess our app's classpath.
   * This is probably fragile.
   */
  lazy val impliedClassPath: List[String] = {
    val currentClassPath = this.getClass.getClassLoader.asInstanceOf[URLClassLoader].getURLs.
      map(_.toString).filter(_.startsWith("file:")).map(_.substring(5)).toList

    // if there's just one thing in the classpath, and it's a jar, assume an executable jar.
    currentClassPath ::: (if (currentClassPath.size == 1 && currentClassPath(0).endsWith(".jar")) {
      val jarFile = currentClassPath(0)
      val relativeRoot = new File(jarFile).getParentFile()
      val nestedClassPath = new JarFile(jarFile).getManifest.getMainAttributes.getValue("Class-Path")
      if (nestedClassPath eq null) {
        Nil
      } else {
        nestedClassPath.split(" ").map {
          f => new File(relativeRoot, f).getAbsolutePath
        }.toList
      }
    } else {
      Nil
    })
  }
}
