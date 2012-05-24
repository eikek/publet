package org.eknet.publet.engine.scalate

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.Publet
import org.eknet.publet.vfs._
import org.fusesource.scalate.layout.{NullLayoutStrategy, DefaultLayoutStrategy}
import grizzled.slf4j.Logging
import org.fusesource.scalate.{CompilerException, InvalidSyntaxException, TemplateSource, TemplateEngine}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 00:09
 */
class ScalateEngine(val name: Symbol, val engine: TemplateEngine) extends PubletEngine with Logging {

  val urlResources = new UrlResources(Some(engine.resourceLoader))
  engine.resourceLoader = urlResources

  private val defaultLayout = classOf[ScalateEngine].getResource("default.jade")
  urlResources.addUrl(defaultLayout)

  private val errorViewLayout = classOf[ScalateEngine].getResource("_errorView.page")
  urlResources.addUrl(errorViewLayout)

  setDefaultLayoutUri(defaultLayout.toString)

  def setDefaultLayoutUri(uri: String) {
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, uri)
  }

  def disableLayout() {
    engine.layoutStrategy = NullLayoutStrategy
  }

  var attributes: Map[String, Any] = Map()

  def process(path: Path, data: ContentResource, target: ContentType) = {
    if (data.contentType == target) {
      Some(data)
    } else {
      if (target == ContentType.html) {
        if (engine.extensions.contains(data.name.ext)) {
          val uri = path.withExt(data.name.ext).asString
          Some(processUri(uri, Some(data), attributes))
        } else {
          val templateSource = if (data.contentType.mime._1 == "text")
            new CodeTemplateSource(path, data)
          else if (data.contentType.mime._1 == "image")
            new ImageTemplateSource(path, data)
          else
            new DownloadTemplateSource(path, data)

          urlResources.put(templateSource.uri, templateSource)
          Some(processSource(templateSource, Some(data), attributes))
        }
      } else {
        None
      }
    }
  }

  def processUri(uri: String, data: Option[ContentResource], attributes: Map[String, Any] = Map()): Content = {
    try {
      val out = engine.layout(uri, attributes)
      Content(out, ContentType.html)
    }
    catch {
      case e:InvalidSyntaxException => {
        error("Invalid template syntax: " + e.getMessage)
        renderErrorView(e, data)
      }
      case e: CompilerException => {
        error("Compiler error in template: "+ e.getMessage)
        renderErrorView(e, data)
      }
    }
  }

  def processSource(source: TemplateSource, data: Option[ContentResource], attributes: Map[String, Any] = Map()): Content = {
    try {
      val out = engine.layout(source, attributes)
      Content(out, ContentType.html)
    }
    catch {
      case e:InvalidSyntaxException => {
        error("Invalid template syntax: "+ e.getMessage)
        renderErrorView(e, data)
      }
      case e: CompilerException => {
        error("Compiler error in template: "+ e.getMessage)
        renderErrorView(e, data)
      }
    }
  }

  def renderErrorView(e: InvalidSyntaxException, data: Option[ContentResource]): Content = {
    val info = Map(
      "content" -> data.map(_.contentAsString.split('\n').toList).getOrElse(List()),
      "templateUri" -> e.template,
      "errors" -> Map(e.pos.line -> ErrorMessage(e.pos, e.brief))
    )
    val out = engine.layout(errorViewLayout.toString, (attributes ++ info))
    Content(out, ContentType.html)
  }

  def renderErrorView(e: CompilerException, data: Option[ContentResource]): Content = {
    val errors = e.errors.map(error => error.pos.line -> ErrorMessage(error.pos, error.message))
    val info = Map(
      "content" -> data.map(_.contentAsString.split('\n').toList).getOrElse(List()),
      "templateUri" -> data.map(_.name.fullName).getOrElse(""),
      "errors" -> errors.toMap
    )
    val out = engine.layout(errorViewLayout.toString, (attributes ++ info))
    Content(out, ContentType.html)
  }
}

object ScalateEngine {

  def apply(name: Symbol, publet: Publet): ScalateEngine = {
    val engine = new TemplateEngine
    VfsResourceLoader.install(engine, publet)
    new ScalateEngine(name, engine)
  }
}

private class DownloadTemplateSource(path: Path, c: ContentResource) extends TemplateSource {
  private val created = System.currentTimeMillis()

  def uri = path.withExt("ssp").asString

  def inputStream = {
    val p = path.withExt(c.contentType.extensions.head).segments.last
    NodeContent(<p>Download: <a href={p}>{p}</a></p>, ContentType.ssp).inputStream
  }

  def lastModified = c.lastModification.getOrElse(created)
  override def toString = "DownloadTemplateSource["+c.toString+": "+ lastModified+"]"
}

private class CodeTemplateSource(path: Path, c: ContentResource) extends TemplateSource {
  private val created = System.currentTimeMillis()

  def uri = path.withExt("ssp").asString

  def inputStream = {
    NodeContent(<pre><code>{c.contentAsString}</code></pre>, ContentType.ssp).inputStream
  }

  def lastModified = c.lastModification.getOrElse(created)
  override def toString = "CodeTemplateSource["+c.toString+": "+ lastModified+"]"
}

private class ImageTemplateSource(path: Path, c: ContentResource) extends TemplateSource {
  private val created = System.currentTimeMillis()

  def uri = path.withExt("ssp").asString

  def inputStream = {
    NodeContent(<img src={ path.sibling(c.name.fullName).asString } alt={c.name.fullName}></img>, ContentType.ssp).inputStream
  }

  def lastModified = c.lastModification.getOrElse(created)
  override def toString = "ImageTemplateSource["+c.toString+": "+ lastModified+"]"
}