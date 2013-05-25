package org.eknet.publet.webapp.extensions

import scala.reflect.io.{AbstractFile, VirtualDirectory}
import scala.tools.nsc.{Global, Settings}
import akka.actor._
import scala.reflect.internal.util.BatchSourceFile
import scala.tools.nsc.interpreter.AbstractFileClassLoader
import scala.util.{Success, Try}
import com.typesafe.scalalogging.slf4j.Logging
import scala.util.Success
import org.eknet.publet.webapp.{PubletExtension, PubletWeb}
import org.eknet.publet.actor.utils
import java.nio.file.{Files, Path}
import java.security.MessageDigest
import java.math.BigInteger

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.05.13 18:34
 */
private[webapp] class ExtensionOven(target: Path) extends Logging {
  Files.createDirectories(target)
  val targetDir = AbstractFile.getDirectory(target.toFile)

  private val settings = new Settings()
  settings.deprecation.value = true
  settings.unchecked.value = true
  settings.outputDirs.setSingleOutput(targetDir)
  settings.usejavacp.value = true

  private val global = new Global(settings)
  private lazy val run = new global.Run
  val classLoader = new AbstractFileClassLoader(targetDir, this.getClass.getClassLoader)

  private val dynamicAccess = new ReflectiveDynamicAccess(classLoader)

  def compileExtensions(exts: List[String]) = {
    val base = classOf[WebExtensionImpl]
    if (exts.isEmpty) Success(base.asInstanceOf[Class[Actor]])
    else {
      val className = classNameForCode(exts)
      findClass(className) orElse {
        logger.info("Building webapp with extensions: "+ exts.mkString(", "))
        val stopButton = utils.Stopwatch.start()
        val source = new BatchSourceFile("inline", makeClassDef(className, exts))
        run.compileSources(List(source))
        logger.info(s"Webapp build in ${stopButton()}ms")
        findClass(className)
      }
    }
  }

  def bakeExtensionActor(webapp: ActorRefFactory, system: ActorSystem) = Try {
    val exts = PubletWeb(system).webSettings.extensions
    if (exts.isEmpty) {
      new WebExtensionImpl(webapp, system) with PubletExtension
    } else {
      val actorClass = compileExtensions(exts ::: List(classOf[PubletExtension].getName)).get
      lazy val systemCtor = dynamicAccess.createInstanceFor[Actor](actorClass,
        Seq(classOf[ActorRefFactory] -> webapp, classOf[ActorSystem] -> system))
      systemCtor.get
    }
  }

  private def makeClassDef(className: String, ext: List[String]) =
  {
    val base = classOf[WebExtensionImpl]
    """
      |package org.eknet.publet.webapp.extensions
      |
      |import akka.actor.{ActorSystem, ActorRefFactory, Actor}
      |
      |final class %s(webapp: ActorRefFactory, system: ActorSystem) extends %s(webapp, system) with %s
    """.stripMargin format (className, base.getName, ext.mkString(" with "))
  }

  private def findClass(simpleName: String) = Try {
    classLoader.loadClass("org.eknet.publet.webapp.extensions."+ simpleName).asInstanceOf[Class[Actor]]
  }

  private def classNameForCode(exts: List[String]): String = {
    val digest = MessageDigest.getInstance("SHA-1").digest(exts.mkString(":").getBytes)
    "Cake"+new BigInteger(1, digest).toString(16)
  }
}
