package org.eknet.publet

import engine.{PassThrough, PubletEngine, EngineRegistry}
import impl.PubletImpl
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
  def process(uri: Uri): Either[Exception, Option[Page]]

  /**
   * Processes the source at the given URI and retursn
   * the transformed result according to the specified
   * target format.
   * <p>
   * If no source is found, $none is returned, otherwise
   * an exception is thrown if the source file could not
   * be transformed.
   * </p>
   *
   * @param uri
   * @param targetType
   * @return
   */
  def process(uri: Uri, targetType: ContentType): Either[Exception, Option[Page]]

  def mount(urlPattern: String, engine: PubletEngine)

}

object Publet {

  def apply() = {
    val p = new PubletImpl
    p.addEngine(new PassThrough)
    p
  }

}
