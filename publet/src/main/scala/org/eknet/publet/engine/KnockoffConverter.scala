package org.eknet.publet.engine

import com.tristanhunt.knockoff.DefaultDiscounter._
import org.eknet.publet.{ContentType, Content}

/**
 * Uses the knockoff library to transform markdown markup to html.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.03.12 23:16
 */
object KnockoffConverter extends ConverterEngine#Converter {

  def apply(page: Content) = {
    val xhtml = toXHTML( knockoff( page.contentAsString ) )
    Content(xhtml.toString(), ContentType.html)
  }

}