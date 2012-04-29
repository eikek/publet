package org.eknet.publet.engine.scala

import org.eknet.publet.vfs.Path


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.04.12 16:51
 */
class CompileException(val path: Path,
                       val messages: List[List[String]])
  extends Exception(
    "Errors compiling: "+ path.asString+"\n"
    + messages.map(_.mkString("\n")).mkString("\n")
  )
