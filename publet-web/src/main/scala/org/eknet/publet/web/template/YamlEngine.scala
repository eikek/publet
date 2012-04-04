package org.eknet.publet.web.template

import org.eknet.publet.engine.PubletEngine

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 23:03
 */
class YamlEngine(override val name: Symbol, engine: PubletEngine)
    extends HtmlTemplateEngine(name, engine)
    with YamlTemplate
    with HighlightTemplate {


  override def toString = "YamlEngine:"+ name +"["+ engine +"]"
}
