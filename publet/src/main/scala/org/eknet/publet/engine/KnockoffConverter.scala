package org.eknet.publet.engine

import com.tristanhunt.knockoff.DefaultDiscounter._
import com.tristanhunt.knockoff._
import io.Source
import org.eknet.publet.{ContentType, Page}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.03.12 23:16
 */
object KnockoffConverter extends ConverterEngine#Converter {

  def apply(page: Page) = {
    val xhtml = toXHTML( knockoff( page.contentAsString ) )
    Page(xhtml.toString(), ContentType.html)
  }

}
