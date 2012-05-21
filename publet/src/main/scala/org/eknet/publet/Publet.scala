package org.eknet.publet

import engine._
import vfs._
import convert.PassThrough
import impl.PubletImpl
import java.io.InputStream

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:06
 */
trait Publet {

  /** Processes the source at the given URI and returns
   * the transformed result. If the source is not available
   * the result is `None`.
   *
   * @param path
   * @return
   */
  def process(path: Path): Option[Content]

  /** Processes the source at the given URI and returns
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
  def process(path: Path, targetType: ContentType, engine: Option[PubletEngine] = None): Option[Content]

  /** Copies the given content to the content at the specified path.
   *
   * If no content exists at the given path it is created. On
   * successful write `true` is returned.
   *
   * @param path
   * @param content
   * @return
   */
  def push(path: Path, content: InputStream, message: Option[String] = None)

  def delete(path: Path)

  /**
   * Finds resources that matches the name of the specified uri
   * but not necessarily the file extension.
   * <p>
   * For example, finds a `title.md` if a `title.html` is requested,
   * while `title.html` will be the first one on the Seq if it exists.
   * </p>
   *
   * @param path
   * @return
   */
  def findSources(path: Path): Iterable[ContentResource]


  /// MountManager
  def mountManager: MountManager

  //EngineResolver
  def engineManager: EngineMangager

  //Container
  def rootContainer: RootContainer

}

object Publet {

  def apply(): Publet = {
    val p = new PubletImpl
    p.engineManager.addEngine(PassThrough)
    p
  }

  /** The `.includes/` constant */
  val includes = ".includes/"
  /** The `.includes/` constant */
  val includesPath = Path(includes)

  /** The `.allIncludes/` constant */
  val allIncludes = ".allIncludes/"
  /** The `.allIncludes/` constant */
  val allIncludesPath = Path(allIncludes)
}
