package org.eknet.publet

import engine.EngineRegistry
import impl.PubletImpl
import java.net.URI
import source.SourceRegistry

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:06
 */
trait Publet extends SourceRegistry with EngineRegistry {

  /**
   * Processes the source at the given URI and returns
   * the transformed result. If the source is not available
   * the result is `None`.
   *
   * @param uri
   * @return
   */
  def process(uri: URI): Either[Exception, Option[Data]]

}

object Publet {

  def apply() = new PubletImpl

}
