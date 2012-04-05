package org.eknet.publet.web.template

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.Path
import org.eknet.publet.resource.{NodeContent, ContentType, Content}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 21:50
 */
class HtmlTemplateEngine(val name: Symbol, engine: PubletEngine) extends PubletEngine {
  this: HtmlTemplate =>
  
  def process(path: Path, data: Seq[Content], target: ContentType) = {
    engine.process(path, data, target) match {
      case Right(nc:NodeContent) => Right[Exception, Content](apply(path, nc))
      case l @ _  => l
    }
  }
}
