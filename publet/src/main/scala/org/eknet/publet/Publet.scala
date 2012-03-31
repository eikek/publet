package org.eknet.publet

import engine._
import impl.PubletImpl
import postproc.PostProcessor
import source.{Partition, MountManager}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:06
 */
trait Publet extends MountManager[Partition] with EngineResolver {

  /**
   * Processes the source at the given URI and returns
   * the transformed result. If the source is not available
   * the result is `None`.
   *
   * @param path
   * @return
   */
  def process(path: Path): Either[Exception, Option[Content]]

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
   * @param path
   * @param targetType
   * @return
   */
  def process(path: Path, targetType: ContentType): Either[Exception, Option[Content]]


  def install(path: Path, proc:PostProcessor)
  
}

object Publet {

  def apply() = {
    val p = new PubletImpl
    p.addEngine(PassThrough)
    p
  }
  
  def default(path: Path, part: Partition): Publet = {
    val publ = Publet()
    val conv = DefaultConverterEngine
    publ.register("/*", conv)
    conv.addConverter(ContentType.markdown, ContentType.html, KnockoffConverter)
    publ.mount(path, part)
    publ
  }

}
