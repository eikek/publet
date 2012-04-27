package org.eknet.publet.engine.scala

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 18:54
 */
class ScalaScriptEngine(val name: Symbol,
                        compiler: PubletCompiler,
                        engine: PubletEngine) extends PubletEngine {

  def process(data: Seq[ContentResource], target: ContentType) = {
    data find (_.contentType == ContentType.scal) match {
      case Some(resource) => eval(resource) match {
        case a@Some(content) => engine.process(Seq(new CompositeContentResource(resource, content)), target)
        case None => None
      }
      case None => throw new RuntimeException("no scala script content found")
    }
  }

  def eval(resource: ContentResource): Option[Content] = {
    compiler.evaluate(resource)
  }
}
