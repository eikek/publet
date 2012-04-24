package org.eknet.publet.web

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.{Path, Publet}
import org.eknet.publet.engine.convert._
import template._
import org.eknet.publet.resource.{ContentType, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 09.04.12 12:56
 */

class DefaultEngine(publet: Publet, val name: Symbol = 'mainWiki) extends PubletEngine {
  self =>

  val includeEngine = ConverterEngine('include)

  private val mainWikiEngine = new HtmlTemplateEngine('singlepage, includeEngine)
      with YamlTemplate
      with HighlightTemplate
      with PubletTemplate
      with IncludesTemplate
  mainWikiEngine.onInstall(publet)

  private object SidebarEngine extends HtmlTemplateEngine('sidebar, includeEngine)
      with Yaml2ColTemplate
      with HighlightTemplate
      with PubletTemplate
      with IncludesTemplate
  {
    def yamlColumn(path: Path, content: Content, source: Seq[Content]) = {
      val sidebarPath = path.parent / Path(".includes/sidebar.html").withExtension(path.targetType.get.extensions.head)
      publet.process(sidebarPath, path.targetType.get, includeEngine) match {
        case Right(Some(c)) => c.contentAsString
        case _ => ""
      }
    }
  }
  SidebarEngine.onInstall(publet)

  def process(path: Path, data: Seq[Content], target: ContentType) = {
    //check for a .includes/sidebar and use it if present
    val sidebarPath = path.parent / Path(".includes/sidebar.html")
    publet.findSources(sidebarPath) match {
      case Seq(a, _*) => SidebarEngine.process(path, data, target)
      case _ => mainWikiEngine.process(path, data, target)
    }
  }

}
