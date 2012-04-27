package org.eknet.publet.webeditor

import xml.NodeSeq
import org.eknet.publet.vfs.{PathContentResource, Path, ContentType, NodeContent}

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
      </form> ++ imageView(path)
  }

  private def imageView(path: Path): NodeSeq = {
    if (path.name.targetType.mime._1 == "image")
      <p>Current image:</p> <img src={path.segments.last}/>
    else
      <p>Current File:
        <a href={path.segments.last}>
          {path.segments.last}
        </a>
      </p>
  }

  def uploadContent(path: Path) = new PathContentResource(path, NodeContent(uploadBody(path), ContentType.html))
}
