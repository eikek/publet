package org.eknet.publet.engine

import org.eknet.publet.Page

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 10:11
 */
trait ConverterEngine extends PubletEngine {

  type Converter = Page => Page

}

object ConverterEngine {

  def apply(): ConverterEngine = new DefaultConverterEngine

}