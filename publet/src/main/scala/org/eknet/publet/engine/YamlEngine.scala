package org.eknet.publet.engine

import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.source.Partitions
import org.eknet.publet._
import java.util.UUID

/**
 * A "meta" engine that wraps the output of its delegate in a YAML
 * page.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 16:17
 */
class YamlEngine(engine: PubletEngine) extends PubletEngine with YamlTemplate with HighlightTemplate {

  def name = 'yaml

  def process(path:Path, data: Seq[Content], target: ContentType) = {
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
