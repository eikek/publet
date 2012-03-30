package org.eknet.publet.engine

import org.eknet.publet.{ContentType, Page}
import collection.mutable.ListBuffer
import io.Source

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.03.12 22:41
 */
object TextToHtml extends ConverterEngine#Converter {
  
  def apply(page: Page) = {
    val buffer = ListBuffer[String]()
    buffer + "<html>"
    for (line <- Source.fromInputStream(page.content).getLines()) {
      if (buffer.length == 1) {
        buffer + "<head><title>"+ line +"</title></head>"
        buffer + "<body>"
        buffer + "<p>"
      } else {
        buffer + line
      }
    }
    buffer + "</p></body>"
    buffer + "</html>"
    val str = buffer.mkString("\n") .replaceAll("\\n\\n", "</p><p>")
    Page(str, ContentType.html)
  }
  
}
