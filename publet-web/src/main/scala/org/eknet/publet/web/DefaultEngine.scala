package org.eknet.publet.web

import org.eknet.publet.resource.ContentType._
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.engine.convert.{ConverterEngine, KnockoffConverter}
import org.eknet.publet.{Path, Publet}
import org.eknet.publet.resource.{NodeContent, ContentType, Content}
import scala.xml.NodeSeq
import org.eknet.publet.engine.scalascript.ScalaHtmlConverter
import template._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 09.04.12 12:56
 */

class DefaultEngine(publet: Publet, val name: Symbol = 'main) extends PubletEngine {
  self =>

  private val convEngine = ConverterEngine()
  convEngine.addConverter(markdown -> html, KnockoffConverter)
  convEngine.addConverter(scal -> html, ScalaHtmlConverter)

  private val defaultEngine = new HtmlTemplateEngine('main, convEngine)
      with YamlTemplate
      with HighlightTemplate
      with CustomCssTemplate {

    def publet = self.publet
  }
  defaultEngine.onInstall(publet)

  object SidebarEngine extends HtmlTemplateEngine('sidebar, convEngine) with Yaml2ColTemplate with HighlightTemplate {
    def yamlColumn(path: Path, content: NodeContent) = {
      val sidebarPath = path.sibling("_sidebar.html").withExtension(path.targetType.get.extensions.head)
      publet.process(sidebarPath, path.targetType.get, convEngine) match {
        case Right(Some(c: NodeContent)) => c.node
        case _ => NodeSeq.Empty
      }
    }
  }
  SidebarEngine.onInstall(publet)

  def process(path: Path, data: Seq[Content], target: ContentType) = {
    //check for a _sidebar and use it if present
    val sidebarPath = path.sibling("_sidebar.html")
    publet.findSources(sidebarPath) match {
      case Seq(a, _*) => SidebarEngine.process(path, data, target)
      case _ => defaultEngine.process(path, data, target)
    }
  }
  
}
