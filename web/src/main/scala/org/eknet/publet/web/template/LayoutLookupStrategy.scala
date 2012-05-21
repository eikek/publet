package org.eknet.publet.web.template

import org.fusesource.scalate.{RenderContext, Template, TemplateEngine}
import org.fusesource.scalate.layout.{DefaultLayoutStrategy, LayoutStrategy}
import org.eknet.publet.web.{PubletWebContext, IncludeLoader}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.05.12 23:19
 */
class LayoutLookupStrategy(val engine: TemplateEngine, defaultLayouts: String*) extends LayoutStrategy {

  private val delegate = new DefaultLayoutStrategy(engine, defaultLayouts: _*)
  private val loader = new IncludeLoader()

  val layoutName = "pageLayout"
  val layoutCandidates = engine.extensions.map(layoutName +"."+ _)

  def layout(template: Template, context: RenderContext) {
    PubletWebContext.param("noLayout") match {
      case Some(_) => context.attributes.update("layout", "")
      case _ =>
    }
    context.attributes.get("layout") getOrElse {
      findLayout(layoutCandidates.toList) foreach { layout =>
        context.attributes.update("layout", layout)
      }
    }
    delegate.layout(template, context)
  }

  private def findLayout(cand: List[String]): Option[String] = {
    cand match {
      case c::cs => loader.findInclude(c) orElse {
        findLayout(cs)
      }
      case Nil => None
    }
  }
}
