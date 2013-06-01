package org.eknet.publet.webapp.scalate

import java.util.regex.Matcher
import org.fusesource.scalate.{Binding, RenderContext}
import org.fusesource.scalate.wikitext.Pygmentize
import org.fusesource.scalamd.{MacroDefinition, Markdown}
import akka.actor.{ExtensionId, Extension, Actor}
import org.eknet.publet.scalate.{ConfiguredEngine, PubletScalate}
import org.eknet.publet.webapp.{ApplicationSettings, PubletWeb}
import org.eknet.publet.actor.Publet
import org.fusesource.scalate.layout.DefaultLayoutStrategy

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.05.13 22:43
 */
trait ScalateInit {
  self: Actor =>

  PubletScalate(context.system).addInitializer("default", addDefaultBindings)
  PubletScalate(context.system).addInitializer("default", addDefaultLayout)
  PubletScalate(context.system).addInitializer("default", addFilters)

  private def addDefaultBindings(ce: ConfiguredEngine) {
    val engine = ce.engine
    engine.bindings ++= List(Binding("templateCtx", "_root_."+ classOf[TemplateContext].getName, true))
    ce.attributes = ce.attributes.updated("templateCtx" , new TemplateContext {
      def extension[T <: Extension](provider: ExtensionId[T]) = provider(context.system)
      def urlFor(path: String) = extension(PubletWeb).webSettings.urlFor(path)
      def appSettings = extension(PubletWeb).appSettings
    })
  }

  private def addDefaultLayout(ce: ConfiguredEngine) {
    val engine = ce.engine
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, "/publet/webapp/templates/default-layout.jade")
  }

  private def addFilters(ce: ConfiguredEngine) {
    val engine = ce.engine

    def filter(m: Matcher): String = {
      val filter_name = m.group(1)
      val body = m.group(2)
      engine.filter(filter_name) match {
        case Some(filter) =>
          filter.filter(RenderContext(), body)
        case None => "<div class=\"macro error\"><p>filter not found: %s</p><pre>%s</pre></div>".format(filter_name, body)
      }
    }

    def pygmentize(m: Matcher): String = Pygmentize.pygmentize(m.group(2), m.group(1))

    def fencedCode(m: Matcher): String = {
      val lang = Option(m.group(1)).filterNot(_.isEmpty)
      val body = m.group(2)
      lang match {
        case Some(l) => """<pre><code class="%s">%s</code></pre>""".format(l, xml.Utility.escape(body))
        case None => """<pre>%s</pre>""".format(xml.Utility.escape(body))
      }
    }

    // add some macros to markdown.
    Markdown.macros :::= List(
      MacroDefinition( """\{filter::(.*?)\}(.*?)\{filter\}""", "s", filter, true),
      MacroDefinition( """\{pygmentize::(.*?)\}(.*?)\{pygmentize\}""", "s", pygmentize, true),
      MacroDefinition( """\{pygmentize\_and\_compare::(.*?)\}(.*?)\{pygmentize\_and\_compare\}""", "s", pygmentize, true),
      MacroDefinition( """```(.*?)\s(.*?)```""", "s", fencedCode, true)
    )

    for (ssp <- engine.filter("ssp"); md <- engine.filter("markdown")) {
      engine.pipelines += "ssp.md" -> List(ssp, md)
      engine.pipelines += "ssp.markdown" -> List(ssp, md)
    }
  }

}

trait TemplateContext {
  def extension[T <: Extension](provider: ExtensionId[T]): T
  def urlFor(path: String): String
  def appSettings: ApplicationSettings
}