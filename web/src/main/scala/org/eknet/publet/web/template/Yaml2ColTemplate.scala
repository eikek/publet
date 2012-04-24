package org.eknet.publet.web.template

import org.eknet.publet.Path
import xml.NodeSeq
import org.eknet.publet.resource.{ContentType, Content, NodeContent}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 08.04.12 14:58
 */

trait Yaml2ColTemplate extends YamlTemplate {
  override def yamlMain(path: Path, content: Content, source: Seq[Content]) = {
    val body = stripHeadAndFoot(content)

    val str = """<div class="ym-wrapper">
      <div class="ym-wbox">
        <section class="ym-grid linearize-level-1">
          <article class="ym-g66 ym-gl content">
            <div class="ym-gbox-left ym-clearfix">
              %s
            </div>
          </article>
          <aside class="ym-g33 ym-gr">
            <div class="ym-gbox-right ym-clearfix">
              %s
            </div>
          </aside>
        </section>
      </div>
    </div>"""

    String.format(str,
      body.getOrElse(content.contentAsString),
      yamlColumn(path, content, source))
  }

  def yamlColumn(path: Path, content: Content, source: Seq[Content]): String

}
