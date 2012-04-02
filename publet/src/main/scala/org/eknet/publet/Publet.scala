package org.eknet.publet

import engine._
import ContentType._
import impl.PubletImpl
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

  def process(path: Path, targetType: ContentType, engine: PubletEngine): Either[Exception, Option[Content]]

  /**
   * Copies the given content to the content at the specified path.
   *
   * If no content exists at the given path it is created. On
   * successful write `true` is returned.
   *
   * @param path
   * @param content
   * @return
   */
  def push(path: Path, content: Content): Either[Exception, Boolean]

  def create(path: Path, contentType: ContentType): Either[Exception, Content]

}

object Publet {

  def apply() = {
    val p = new PubletImpl
    p.addEngine(PassThrough)
    p
  }
  
  def default(path: Path, part: Partition): Publet = {
    val publ = Publet()
    publ.mount(path, part)

    val conv = ConverterEngine()
    conv.addConverter(markdown -> html, KnockoffConverter)
    publ.register("/*", new YamlEngine('default, conv))
    
    val editEngine = new HtmlTemplateEngine('editor, PassThrough) with EditTemplate
    publ.addEngine(new YamlEngine('edit, editEngine))

    val uploadEngine = new HtmlTemplateEngine('uploader, PassThrough) with UploadTemplate
    publ.addEngine(new YamlEngine('upload, uploadEngine))
    publ
  }

}
