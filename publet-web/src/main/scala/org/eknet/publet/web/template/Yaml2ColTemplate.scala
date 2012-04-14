package org.eknet.publet.web.template

import org.eknet.publet.Path
import xml.NodeSeq
import org.eknet.publet.resource.{Content, NodeContent}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 08.04.12 14:58
 */

trait Yaml2ColTemplate extends YamlTemplate {
  override def yamlMain(path: Path, content: NodeContent, source: Seq[Content]) = {
    val body = removeHeadline(content)
    <div class="ym-wrapper">
      <div class="ym-wbox">
        <section class="ym-grid linearize-level-1">
          <article class="ym-g66 ym-gl content">
            <div class="ym-gbox-left ym-clearfix">
              { body.getOrElse(content.node) }
            </div>
          </article>
          <aside class="ym-g33 ym-gr">
            <div class="ym-gbox-right ym-clearfix">
              { yamlColumn(path, content, source) }
            </div>
          </aside>
        </section>
      </div>
    </div>
  }

  def yamlColumn(path: Path, content: NodeContent, source: Seq[Content]): NodeSeq

}
