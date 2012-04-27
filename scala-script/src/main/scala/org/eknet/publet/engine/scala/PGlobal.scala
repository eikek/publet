package org.eknet.publet.engine.scala

import scala.tools.nsc._
import scala.tools.nsc.reporters._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 19:24
 */
class PGlobal(settings: Settings, reporter: Reporter) extends Global(settings, reporter) {

  def this(settings:Settings) = this(settings, new ConsoleReporter(settings))

  override def classPath =  super.classPath
  //add the compiler classpath of the root and mini project

}

