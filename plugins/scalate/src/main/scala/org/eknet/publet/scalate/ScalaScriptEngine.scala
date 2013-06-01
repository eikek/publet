package org.eknet.publet.scalate

import akka.actor._
import org.eknet.publet.actor.{utils, Publet, Logging}
import org.eknet.publet.content._
import scala.util.Try
import org.fusesource.scalate.support.ScalaCompiler
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.reflect.io.AbstractFile
import org.eknet.publet.actor.messages.Conversion
import org.eknet.publet.content.Resource.SimpleContent
import org.eknet.publet.actor.messages.FindContent
import akka.util.Timeout
import scala.concurrent.Future
import org.eknet.publet.actor.messages.Conversion
import org.eknet.publet.content.Resource.SimpleContent
import scala.Some
import org.eknet.publet.content.FsContent
import org.eknet.publet.content./
import org.eknet.publet.actor.messages.FindContent

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.05.13 12:02
 */
class ScalaScriptEngine extends Actor with Logging {

  private val scriptSources = new FsPartition(Publet(context.system).settings.workdir.resolve("script-sources"))
  private val compiler = new ScalaCompiler(scriptSources.directory.toFile, "")
  private val scriptLoader = new AbstractFileClassLoader(AbstractFile.getDirectory(scriptSources.directory.toFile), this.getClass.getClassLoader)

  import akka.pattern.pipe
  private implicit val timeout = Timeout(5000L)
  import context.dispatcher

  def receive = {
    case Conversion(FindContent(path, params), sources, targetType) if (!sources.isEmpty) => {
      val scala = sources.find(_.contentType == ContentType.`text/x-scala`)
      val conv = scala.map { c =>
        val sp = sourcePath(path, c)
        if (needsRecompile(sp, c)) {
          log.info(s"Resource ${sp.absoluteString} needs to be compiled")
          val stopButton = utils.Stopwatch.start()
          val code = scriptWrapper(sp, c)
          val fcontent = scriptSources.createContent(path.parent,
            SimpleContent(path.fileName, Source.scala(code)), ModifyInfo.none).get
          compiler.compile(fcontent.asInstanceOf[FsContent].file.toFile)
          log.info(s"Resource ${sp.absoluteString} compiled in ${stopButton()}ms")
        }
        findClass(sp).get.newInstance()
      }
      val scriptCtx = new ScalaScriptEngine.Context(params, path, targetType, context.system)
      Future { conv.flatMap(_.apply(scriptCtx)) } pipeTo sender
    }
  }

  private def sourcePath(path: Path, c: Content) = path.sibling(c.name)

  private def needsRecompile(path: Path, c: Content) = {
    val clazzFile = path.sibling(c.name.withExtension("class"))
    scriptSources.find(clazzFile).collect({ case c:Content => c}).map { cf =>
      (c.lastModification, cf.lastModification) match {
        case (Some(sourceMod), Some(classMod)) => sourceMod > classMod
        case _ => true
      }
    } getOrElse(true)
  }
  private def scriptWrapper(sp: Path, c: Content) = {
    val pack = sp.parent.segments.mkString(".")
    val name = c.name.base.replace('.', '_')
    val settings = PubletScalate(context.system).scalateSettings.scriptCompilerConfig
    val imports = settings.importStatements.mkString("\n")
    s"""
      |package $pack
      |
      |import org.eknet.publet.content._
      |import org.eknet.publet.scalate.ScriptContext
      |$imports
      |
      |class $name extends org.eknet.publet.scalate.ScriptSource {
      |  def apply(context: ScriptContext): Option[Source] = {
      |    ${contentToString(c)}
      |  }
      |}
    """.stripMargin
  }

  private def contentToString(c: Content) = io.Source.fromInputStream(c.inputStream).getLines().mkString("\n")

  private def findClass(sp: Path) = Try {
    val name = sp.parent.mkString(".") + "."+ sp.fileName.base.replace('.', '_')
    scriptLoader.loadClass(name).asInstanceOf[Class[ScriptSource]]
  }
}

object ScalaScriptEngine {
  private final class Context(val params: Map[String, String], val path: Path, val targetType: ContentType, system: ActorSystem) extends ScriptContext {
    def extension[T <: Extension](provider: ExtensionId[T]) = provider(system)
  }
}

trait ScriptSource {
  def apply(context: ScriptContext): Option[Source]
}

trait ScriptContext {
  def params: Map[String, String]
  def targetType: ContentType
  def path: Path
  def extension[T <: Extension](provider: ExtensionId[T]): T
}