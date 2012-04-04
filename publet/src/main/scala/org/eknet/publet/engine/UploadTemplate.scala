package org.eknet.publet.engine

import org.eknet.publet.resource.{ContentType, Content}
import org.eknet.publet.Path


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 19:13
 */
trait UploadTemplate extends HtmlTemplate {

  def applyTemplate(path: Path, content: Content) =  Content(template(path, content), ContentType.html)

  private def template(path: Path, content: Content): String = {
    """
    <h3>Upload</h3>
    <form action="""" + actionString(path) + """" method="post" class="ym-form linearize-form ym-full" enctype="multipart/form-data">
    <input name="file" type="file" size="50" maxlength="100000">
     <div class="ym-fbox-button">
        <input type="submit" value="Save">
     </div>
    </form>
    """
  }
}
