package org.eknet.publet.engine

import org.eknet.publet.resource.{ContentType, Content}
import org.eknet.publet.Path


/**
 * A "meta" engine that wraps the output of its delegate in a YAML
 * page and adds the highligh.js syntax highlighter.
 *
 * This is only applied if the delegating engine returns a content
 * of type 'html. Others are passed through as is. This is required
 * to be able to serve css and js contents properly.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 16:17
 */
class YamlEngine(override val name: Symbol, engine: PubletEngine) extends HtmlTemplateEngine(name, engine)
    with YamlTemplate
    with HighlightTemplate {

  def this(engine: PubletEngine) = this('yaml, engine)

  override def process(path:Path, data: Seq[Content], target: ContentType) = {
    val content = engine.process(path, data, target)
    content.fold(a=> content, c => {
      if (c.contentType == ContentType.html) {
        Right(applyTemplate(path, c))
      } else {
        content
      }
    })
  }

}
