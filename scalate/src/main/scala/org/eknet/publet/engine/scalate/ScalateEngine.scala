package org.eknet.publet.engine.scalate

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.Publet
import org.eknet.publet.vfs._
import org.fusesource.scalate.{TemplateSource, TemplateEngine}
import org.fusesource.scalate.layout.{NullLayoutStrategy, DefaultLayoutStrategy}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 00:09
 */
class ScalateEngine(val name: Symbol, val engine: TemplateEngine) extends PubletEngine {

  val urlResources = new UrlResources(Some(engine.resourceLoader))
  engine.resourceLoader = urlResources

  private val defaultLayout = classOf[ScalateEngine].getResource("default.jade")
  urlResources.addUrl(defaultLayout)

  setDefaultLayoutUri(defaultLayout.toString)

  def setDefaultLayoutUri(uri: String) {
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, uri)
  }

  def disableLayout() {
    engine.layoutStrategy = NullLayoutStrategy
  }

  var attributes: Map[String, Any] = Map()

  def process(path: Path, data: Seq[ContentResource], target: ContentType) = {
    data.find(_.contentType == target) match {
      case a@Some(c) => a
      case _ => {
        if (target == ContentType.html) {
          val resource = data.find(r => engine.extensions.contains(r.name.ext))
          if (resource.isDefined) {
            val uri = path.withExt(resource.get.name.ext).asString
            Some(processUri(uri, attributes))
          } else {
            val templateSource = if (data.head.contentType.mime._1 == "text")
              new CodeTemplateSource(path, data.head)
            else
              new DownloadTemplateSource(path, data.head)

            urlResources.put(templateSource.uri, templateSource)
            Some(processSource(templateSource, attributes))
          }
        } else {
          None
        }
      }
    }
  }

  def processUri(uri: String, attributes: Map[String, Any] = Map()): Content = {
    val out = engine.layout(uri, attributes)
    Content(out, ContentType.html)
  }

  def processSource(source: TemplateSource, attributes: Map[String, Any] = Map()): Content = {
    val out = engine.layout(source, attributes)
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