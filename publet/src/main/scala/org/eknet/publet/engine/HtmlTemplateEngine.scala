package org.eknet.publet.engine

import org.eknet.publet.{ContentType, Content, Path}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 10:55
 */
class HtmlTemplateEngine(val name: Symbol, engine: PubletEngine) extends PubletEngine {
  this: HtmlTemplate =>

  def process(path: Path, data: Seq[Content], target: ContentType) = {
    val content = engine.process(path, data, target)
    content.fold(a=> content, c => Right(applyTemplate(path, c)))
  }
}
