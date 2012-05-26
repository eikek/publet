package scalate

import grizzled.slf4j.Logging
import org.fusesource.scalamd.{Markdown, MacroDefinition}
import java.util.regex.Matcher
import org.fusesource.scalate.{RenderContext, TemplateEngine}
import java.util.concurrent.atomic.AtomicBoolean
import org.fusesource.scalate.wikitext.Pygmentize

/**
 * Borrowed from here
 * https://github.com/scalate/scalate/blob/master/scalate-website/ext/scalate/Boot.scala
 *
 * see also this
 * http://www.assembla.com/spaces/scalate/tickets/256-should-we-add-the-pygmentize-style-macros-by-default-to-markdown-
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.12 16:32
 */
class Boot(engine: TemplateEngine) extends Logging {

  private val _initialised = new AtomicBoolean(false)

  def run() {
    if (_initialised.compareAndSet(false, true)) {

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

      // add some macros to markdown.
      Markdown.macros :::= List(
        MacroDefinition( """\{filter::(.*?)\}(.*?)\{filter\}""", "s", filter, true),
        MacroDefinition( """\{pygmentize::(.*?)\}(.*?)\{pygmentize\}""", "s", pygmentize, true),
        MacroDefinition( """\{pygmentize\_and\_compare::(.*?)\}(.*?)\{pygmentize\_and\_compare\}""", "s", pygmentize, true)
        //        MacroDefinition("""\$\{project_version\}""", "", _ => project_version.toString, true),
        //        MacroDefinition("""\$\{project_name\}""", "", _ => project_name.toString, true),
        //        MacroDefinition("""\$\{project_id\}""", "", _ => project_id.toString, true)
      )

      for (ssp <- engine.filter("ssp"); md <- engine.filter("markdown")) {
        engine.pipelines += "ssp.md" -> List(ssp, md)
        engine.pipelines += "ssp.markdown" -> List(ssp, md)
      }

      // lets add the confluence macros...
      //      ConfluenceLanguageExtensions.extensions ++= List(ExpressionTag("project_version", () => project_version))

      info("Bootstrapped website gen")
    }

  }
}
