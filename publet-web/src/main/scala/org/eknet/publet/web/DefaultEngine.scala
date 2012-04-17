package org.eknet.publet.web

import org.eknet.publet.resource.ContentType._
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.{Path, Publet}
import org.eknet.publet.engine.convert._
import template._
import org.eknet.publet.resource.NodeContent._
import org.eknet.publet.resource.{NodeContent, ContentType, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 09.04.12 12:56
 */

class DefaultEngine(publet: Publet, val name: Symbol = 'main) extends PubletEngine {
  self =>

  object ImageHtmlConverter extends ConverterEngine#Converter {

    def apply(path: Path, cn: Content) = {
      val imgpath = path.withExtension(cn.contentType.extensions.head).segments.last
      path.targetType match {
        case Some(c) if (c == ContentType.scal) => NodeContent(<img src={ imgpath+"?a=eval" } alt=""/>, ContentType.html)
        case _ => NodeContent(<img src={ imgpath } alt=""/>, ContentType.html)
      }

    }
  }
  
  val convEngine = new DefaultConverterEngine('include)
  convEngine.addConverter(markdown -> html, KnockoffConverter)
  convEngine.addConverter(scal -> html, CodeHtmlConverter.scala)
  convEngine.addConverter(css -> html, CodeHtmlConverter.css)
  convEngine.addConverter(javascript -> html, CodeHtmlConverter.json)
  convEngine.addConverter(text -> html, CodeHtmlConverter.json)
  convEngine.addConverter(xml -> html, CodeHtmlConverter.json)
  convEngine.addConverter(png -> html, ImageHtmlConverter)
  convEngine.addConverter(jpg -> html, ImageHtmlConverter)
  convEngine.addConverter(gif -> html, ImageHtmlConverter)
  convEngine.addConverter(icon -> html, ImageHtmlConverter)
  convEngine.addConverter(pdf -> html, DownloadLinkConverter)
  convEngine.addConverter(zip -> html, DownloadLinkConverter)
  convEngine.addConverter(unknown -> html, DownloadLinkConverter)

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
    def yamlColumn(path: Path, content: Content, source: Seq[Content]) = {
      val sidebarPath = path.parent / Path(".includes/sidebar.html").withExtension(path.targetType.get.extensions.head)
      publet.process(sidebarPath, path.targetType.get, convEngine) match {
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
      case _ => defaultEngine.process(path, data, target)
    }
  }

}
