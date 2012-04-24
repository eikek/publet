package org.eknet.publet.web.template

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.Path
import org.eknet.publet.resource.{ContentType, Content}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 21:50
 */
class HtmlTemplateEngine(val name: Symbol, engine: PubletEngine) extends PubletEngine {
  this: HtmlTemplate =>
  
  def process(path: Path, data: Seq[Content], target: ContentType) = {
    engine.process(path, data, target) match {
      case Right(nc) if (nc.contentType == ContentType.html) => Right[Exception, Content](apply(path, nc, data))
      case l @ _  => l
    }
  }
}
