package org.eknet.publet.engine.scala

import tools.nsc.interpreter.AbstractFileClassLoader
import java.io.File
import scala.tools.nsc._
import reporters.AbstractReporter
import tools.nsc.Settings
import tools.nsc.io.{VirtualDirectory, AbstractFile}
import java.net.URLClassLoader
import java.util.jar.JarFile
import org.eknet.publet.engine.convert.CodeHtmlConverter
import org.eknet.publet.vfs.{ContentType, Content, ContentResource}
import collection.mutable
import util.{Position, BatchSourceFile}

/**
 * Compiles scala snippets to ScalaScript classes.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 21:31
 */
class ScriptCompiler(target: AbstractFile, settings: Settings, imports: List[String]) {

  val targetClassLoader: ClassLoader = new AbstractFileClassLoader(target, this.getClass.getClassLoader)

  val reporter = new AbstractReporter {
    val settings = ScriptCompiler.this.settings
    val messages = new mutable.ListBuffer[List[String]]

    def display(pos: Position, message: String, severity: Severity) {
      severity.count += 1
      val severityName = severity match {
        case ERROR => "error: "
        case WARNING => "warning: "
        case _ => ""
      }
      messages += (severityName + "line " + (pos.line) + ": " + message) ::
        (if (pos.isDefined) {
          pos.inUltimateSource(pos.source).lineContent.stripLineEnd ::
            (" " * (pos.column - 1) + "^") ::
            Nil
        } else {
          Nil
        })
    }

    def displayPrompt {
      // no.
    }

    override def reset {
      super.reset
      messages.clear()
    }
  }

  private val global = new Global(settings, reporter)
  private val run = new global.Run


  def compile(script: ContentResource): Class[_] = {
    val cn = className(script)
    findClass(cn).getOrElse {
      compileSource(cn, script)
      findClass(cn).get
    }
  }

  private def compileSource(cn: String, script:ContentResource) = {
    val wrapped = wrapScript(cn, script)
    println(wrapped)
    val sfiles = List(new BatchSourceFile(script.name.fullName, wrapped))
    try {
      run.compileSources(sfiles)
    } catch {
      case e:Throwable => throw new RuntimeException("Compiler errors in script: "+
        script.path.asString+"\n"+ CodeHtmlConverter.scala.apply(script.path, Content(wrapped, ContentType.scal)).contentAsString, e)
    }
    if (global.reporter.hasErrors) {
      val ex = new RuntimeException("Errors in script: "+ script.path.asString+"\n"+ reporter.messages.map(_.mkString("\n")).mkString("\n"))
      global.reporter.reset()
      target.asInstanceOf[VirtualDirectory].clear()

      throw ex
    }
  }

  def loadScalaScriptClass(resource: ContentResource): ScalaScript = {
    val cls = compile(resource)
    cls.getConstructor().newInstance().asInstanceOf[ScalaScript]
  }

  def findClass(className: String): Option[Class[_]] = {
    try {
      val cls = targetClassLoader.loadClass(className)
      Some(cls)
    } catch {
      case e: ClassNotFoundException => None
    }
  }

  private def className(script: ContentResource): String = {
    script.path.parent.segments.mkString("", "_", "_") + script.path.name.name
  }

  private def wrapScript(className: String, script: ContentResource): String = {
    "import org.eknet.publet.engine.scala.ScalaScript\n" +
    "import org.eknet.publet.engine.scala.ScalaScript._\n\n"+
    (if (!imports.isEmpty) imports.mkString("import ", "\nimport ", "\n") else "") +
    "class "+ className+ " extends ScalaScript { \n\n" +
    " def serve() = { \n" +
    script.contentAsString + "\n" +
    " }\n" +
    "}"
  }
}

object ScriptCompiler {

  def apply(targetDir: Option[File], mp: Option[MiniProject], rp: Option[MiniProject], imports: List[String]): ScriptCompiler = {
    val settings = new Settings()

    val target = targetDir match {
      case Some(dir) => AbstractFile.getDirectory(dir)
      case None => new VirtualDirectory("(memory)", None)
    }
    settings.usejavacp.value = true
    settings.deprecation.value= true
    settings.unchecked.value = true
    settings.bootclasspath.value = (compilerPath :: libPath).mkString(File.pathSeparator)
    settings.outputDirs.setSingleOutput(target)

    val cp = (compilerPath :: libPath) ++ impliedClassPath ++ rp.map(_.libraryClassPath).getOrElse(List()) ++
      mp.map(_.libraryClassPath).getOrElse(List())
    settings.classpath.value = cp.mkString(File.separator)


    new ScriptCompiler(target, settings, imports)
  }

  private lazy val compilerPath = try {
    jarPathOfClass("scala.tools.nsc.Interpreter")
  } catch {
    case e =>
      throw new RuntimeException("Unable lo load scala interpreter from classpath (scala-compiler jar is missing?)", e)
  }

  private lazy val libPath = try {
    jarPathOfClass("scala.ScalaObject")
  } catch {
    case e =>
      throw new RuntimeException("Unable to load scala base object from classpath (scala-library jar is missing?)", e)
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
