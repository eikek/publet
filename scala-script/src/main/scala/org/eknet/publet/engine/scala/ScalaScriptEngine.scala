package org.eknet.publet.engine.scala

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs._
import util.CompositeContentResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 18:54
 */
class ScalaScriptEngine(val name: Symbol,
                        compiler: PubletCompiler,
                        engine: PubletEngine) extends PubletEngine {

  def process(path: Path, data: Seq[ContentResource], target: ContentType) = {
    data find (_.contentType == ContentType.scal) match {
      case Some(resource) => eval(path, resource) flatMap(out => engine.process(path, Seq(out), target))
      case None => throw new RuntimeException("no scala script content found")
    }
  }

  def eval(path: Path, resource: ContentResource): Option[ContentResource] = {
    compiler.evaluate(path, resource)
      .flatMap(_.serve()
        .map(content => new CompositeContentResource(resource, content)))
  }

}
