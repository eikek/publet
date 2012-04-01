package org.eknet.publet.engine

import org.eknet.publet.{ContentType, Content, Path}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 10:47
 */
trait  EditTemplate extends HtmlTemplate {

  def applyTemplate(path: Path, content: Content) = Content(template(path, content), ContentType.html)

  def actionString(path: Path): String = path.segments.last

  private def template(path: Path, content: Content): String = {
    """
    <form action="""" + actionString(path) + """" method="post" class="ym-form linearize-form ym-full" >
      <div class="ym-fbox-text">
        <textarea name="page">
""" + content.contentAsString + """</textarea>
     </div>
     <div class="ym-fbox-button">
        <input type="submit" value="Save">
     </div>
    </form>
    """
  }
}
