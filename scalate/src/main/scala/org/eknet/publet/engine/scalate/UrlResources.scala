package org.eknet.publet.engine.scalate

import java.util.concurrent.{ConcurrentHashMap => JMap}
import org.fusesource.scalate.util.{Resource, ResourceLoader}
import java.net.URL
import org.fusesource.scalate.TemplateSource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 14:35
 */
class UrlResources(delegate: Option[ResourceLoader]) extends ResourceLoader {

  private val urls = new JMap[String, Resource]()

  def resource(uri: String) = delegate.flatMap(_.resource(uri)) orElse Option(urls.get(uri))

  def addUrl(url: URL) {
    require(url != null, "url must not be null")
    urls.put(url.toString, TemplateSource.fromURL(url))
  }

  def put(uri: String, resource: Resource) {
    urls.put(uri, resource)
  }

  def remove(uri: String) {
    urls.remove(uri)
  }
  def removeUrl(url: URL) {
    urls.remove(url.toString)
  }
}
