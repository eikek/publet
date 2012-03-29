package org.eknet.publet.engine

import org.eknet.publet.Data

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 10:11
 */
trait ConverterEngine extends PubletEngine {

  type Converter = Data => Data

}

object ConverterEngine {

  def apply(): ConverterEngine = new DefaultConverterEngine

}