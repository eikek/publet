package org.eknet.publet.engine

import org.eknet.publet.{Content, Path}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 22:02
 */
trait HtmlTemplate {

  /**
   * Override to add header lines.
   *
   * @param path
   * @param content
   * @return
   */
  protected def headerContent(path: Path, content: Content): String = ""

  /**
   * Override this to supply title for the page
   *
   * @param path
   * @param content
   * @return
   */
  protected def title(path: Path, content: Content): String = path.fileName.name.replace("_", " ")

  /**
   * Applies a template to the given content. Title and header data are obtained
   * by the two corresponding methods.
   * 
   * @param path
   * @param content
   * @return
   */
  def applyTemplate(path: Path, content: Content): Content

  def actionString(path: Path): String = path.segments.last
  
}
