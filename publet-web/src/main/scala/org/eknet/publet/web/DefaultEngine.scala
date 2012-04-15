package org.eknet.publet.web

import org.eknet.publet.resource.ContentType._
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.{Path, Publet}
import org.eknet.publet.resource.{NodeContent, ContentType, Content}
import scala.xml.NodeSeq
import org.eknet.publet.engine.scalascript.CodeHtmlConverter
import template._
import org.eknet.publet.engine.convert.{ConverterEngine, DefaultConverterEngine, KnockoffConverter}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 09.04.12 12:56
 */

class DefaultEngine(publet: Publet, val name: Symbol = 'main) extends PubletEngine {
  self =>

  val convEngine = new DefaultConverterEngine('include)
  convEngine.addConverter(markdown -> html, KnockoffConverter)
  convEngine.addConverter(scal -> html, CodeHtmlConverter.scala)
  convEngine.addConverter(css -> html, CodeHtmlConverter.css)
  convEngine.addConverter(javascript -> html, CodeHtmlConverter.json)

  private val defaultEngine = new HtmlTemplateEngine('main, convEngine)
      with YamlTemplate
      with HighlightTemplate
      with PubletTemplate
      with IncludesTemplate
  defaultEngine.onInstall(publet)

  object SidebarEngine extends HtmlTemplateEngine('sidebar, convEngine)
      with Yaml2ColTemplate
      with HighlightTemplate
      with PubletTemplate
      with IncludesTemplate
  {
    def yamlColumn(path: Path, content: NodeContent, source: Seq[Content]) = {
      val sidebarPath = path.parent / Path(".includes/sidebar.html").withExtension(path.targetType.get.extensions.head)
      publet.process(sidebarPath, path.targetType.get, convEngine) match {
        case Right(Some(c: NodeContent)) => c.node
        case _ => NodeSeq.Empty
      }
    }
  }
  SidebarEngine.onInstall(publet)

  def process(path: Path, data: Seq[Content], target: ContentType) = {
    //check for a .includes/sidebar and use it if present
    val sidebarPath = path.parent / Path(".includes/sidebar.html")
    publet.findSources(sidebarPath) match {
      case Seq(a, _*) => SidebarEngine.process(path, data, target)
      case _ => defaultEngine.process(path, data, target)
    }
  }

}
