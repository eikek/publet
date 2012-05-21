package org.eknet.publet.engine.convert

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{Path, ContentResource, ContentType, Content}
import grizzled.slf4j.Logging

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:46
 */
class DefaultConverterEngine(val name: Symbol) extends PubletEngine with ConverterEngine with ConverterRegistry with Logging {

  def this() = this('convert)

  def process(path: Path, data: ContentResource, target: ContentType): Option[Content] = {
    //if target type is available return it, otherwise try to process
    if (data.contentType == target) {
      Some(data)
    } else {
      converterFor(data.contentType, target) match {
        case None => {
          error("no converter found: "+ data.contentType+" -> "+ target)
          None
        };
        case Some(c) => Option(c(path, data))
      }
    }
  }

}
