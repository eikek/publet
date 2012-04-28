package org.eknet.publet.engine.scala

import org.eknet.publet.vfs.ContentResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.04.12 16:51
 */
class CompileException(val resource: ContentResource,
                       val messages: List[List[String]])
  extends Exception(
    "Errors compiling: "+ resource.path.asString+"\n"
    + messages.map(_.mkString("\n")).mkString("\n")
  )
