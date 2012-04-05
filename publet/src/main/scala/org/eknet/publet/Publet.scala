package org.eknet.publet

import engine._
import resource._
import impl.PubletImpl

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:06
 */
trait Publet extends MountManager[Partition] with EngineResolver {

  /** Processes the source at the given URI and returns
   * the transformed result. If the source is not available
   * the result is `None`.
   *
   * @param path
   * @return
   */
  def process(path: Path): Either[Exception, Option[Content]]

  /** Processes the source at the given URI and retursn
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

  def process(path: Path, targetType: ContentType, engine: PubletEngine): Either[Exception, Option[Content]]

  /** Copies the given content to the content at the specified path.
   *
   * If no content exists at the given path it is created. On
   * successful write `true` is returned.
   *
   * @param path
   * @param content
   * @return
   */
  def push(path: Path, content: Content): Either[Exception, Boolean]

  /** Returns the children of the container at the specified
   * path. It returns an empty list, if this resource is not
   * available or if it is not a container.
   *
   * @param path
   * @return
   */
  def children(path: Path): Iterable[_ <: Resource]

}

object Publet {

  def apply() = {
    val p = new PubletImpl
    p.addEngine(PassThrough)
    p
  }
  
}
