package org.eknet.publet.web

import org.eknet.publet.Path
import xml.NodeSeq
import org.eknet.publet.resource.{ContentType, NodeContent}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 23:44
 */
object UploadContent {

  def uploadBody(path: Path): NodeSeq = {
    <h3>Upload</h3>
    <form action={path.segments.last} method="post" class="ym-form linearize-form ym-full" enctype="multipart/form-data">
      <input name="file" type="file" size="70" maxlength="100000"></input>
      <div class="ym-fbox-button">
        <input type="submit" value="Save"></input>
      </div>
    </form>
  }

  def uploadContent(path: Path) = NodeContent(uploadBody(path), ContentType.html)
}
