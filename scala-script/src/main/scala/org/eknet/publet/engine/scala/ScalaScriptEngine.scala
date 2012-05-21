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

  def process(path: Path, data: ContentResource, target: ContentType) = {
    if (data.contentType == ContentType.scal) {
      val out = eval(path, data)
      if (out.exists(_.contentType == target)) {
        out
      } else {
        out flatMap (out => engine.process(path, out, target))
      }
    } else {
      throw new RuntimeException("no scala script content found")
    }
  }

  def eval(path: Path, resource: ContentResource): Option[ContentResource] = {
    compiler.evaluate(path, resource)
      .flatMap(_.serve()
        .map(content => new CompositeContentResource(resource, content)))
  }

}
