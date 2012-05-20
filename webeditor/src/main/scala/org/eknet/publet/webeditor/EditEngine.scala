package org.eknet.publet.webeditor

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs._
import util.CompositeContentResource
import xml._
import grizzled.slf4j.Logging
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.{GitAction, PubletWeb, PubletWebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.04.12 00:17
 */
class EditEngine(del: PubletEngine) extends PubletEngine with Logging {
/*
  textarea#editPage {
    height: 400px;
    font-family: monospace;
    line-height: 1.2;
    font-size: medium;
    font-style: oblique
  }
*/
  def pushPath(path: Path, content: ContentResource) = Path(path.relativeRoot + EditorWebExtension.scriptPath.asString) / "push.html"

  def deleteButton(path: Path, content: ContentResource) = {
    val delHandler = pushPath(path, content).asString+"?delete="+path.asString
    if (Security.hasGitAction(GitAction.push)) {
      <a class="ym-button ym-delete" onClick="return confirm('Really delete this file?');" href={delHandler}>Delete</a>
    } else {
      NodeSeq.Empty
    }
  }

  def createForm(path: Path, content: ContentResource, body: NodeSeq, upload:Boolean): NodeSeq = {
    val cancelHandler = path.withExt("html").asString
    val pushPath = Path(path.relativeRoot) / EditorWebExtension.scriptPath / "push.html"
    val delButton = deleteButton(path, content)
    val lastmod = content.lastModification.map(_.toString).getOrElse("")
    val enctype = if (upload) "multipart/form-data" else ""
    val saveButton = if (upload) {
//      <button type="submit" class="ym-button ym-save">Save</button>
      <button class="ym-button ym-save" onClick="return formAjaxSubmit('editPageForm', 'editFormResponse');">Save</button>
    } else {
      <button class="ym-button ym-save" onClick="return formAjaxSubmit('editPageForm', 'editFormResponse');">Save</button>
    }
    <h3>Edit Page</h3>
      <div id="editFormResponse"></div>
      <form id="editPageForm" action={ pushPath.asString } method="post" class="ym-form linearize-form ym-full" enctype={enctype}>
        <div class="ym-fbox-button">
          { saveButton }
          <a class="ym-button ym-play" href={cancelHandler}>View</a>
          { delButton }
        </div>
        { body }
        <div class="ym-fbox-text">
          <label for="commitMessage">Message</label>
          <input type="text" name="commitMessage" id="commitMessage" size="20"></input>
        </div>
        <input type="hidden" name="path" value={ path.asString } />
        <input type="hidden" name="a" value="include"/>
        <input id="lastHead" type="hidden" name="head" value={lastmod}/>
      </form>
      <p>If you'd like to write markdown syntax,
        <a href="http://daringfireball.net/projects/markdown/syntax" target="_new">here</a>
        is the syntax definition. If you like to use some special html formatting, you can use standard yaml elements as
        <a href="http://www.yaml.de/docs/index.html#yaml-typography">defined here</a>
      </p>
  }

  def editContent(path: Path, content: ContentResource): ContentResource = {
    def typeSelect(): NodeSeq = {
      val publet = PubletWeb.publet


      <div class="ym-fbox-select">
        <label for="extentionOptions">Extension<sup class="ym-required">*</sup></label>
        <select id="extentionOptions" name="extension" required="required">
        </select>
      </div>
    }

    val body = typeSelect() ++ <div class="ym-fbox-text">
      <textarea name="page" id="editPage">{content.contentAsString}</textarea>
    </div>

    new CompositeContentResource(content,
      NodeContent(createForm(path, content, body, false), ContentType.html))
  }

  def name = 'edit


  def uploadBody(path: Path) = {
    def imageView(): NodeSeq = {
      if (path.name.targetType.mime._1 == "image")
        <p>Current image:</p> <img src={path.segments.last} style="max-width:400px;"/>
      else
        <p>Current File:
          <a href={path.segments.last}>
            {path.segments.last}
          </a>
        </p>
    }

    <div class="ym-fbox-text">
      <label for="fileInput">File</label>
      <input id="fileInput" name="file" type="file" maxlength="100000" required="required"></input>
      <div>{imageView()}</div>
    </div>
  }

  def uploadContent(path: Path, content: ContentResource) = {
    val formbody = uploadBody(path)
    new CompositeContentResource(content,
      NodeContent(createForm(path, content, formbody, true), ContentType.html))
  }

  def process(path: Path, data: Seq[ContentResource], target: ContentType) = {
    Security.checkGitAction(GitAction.push)
    if (!data.head.exists || data.head.isInstanceOf[Writeable]) {
      if (data.head.contentType.mime._1 == "text")
        del.process(path, Seq(editContent(path, data.head)), ContentType.html)
      else
        del.process(path, Seq(uploadContent(path, data.head)), ContentType.html)
    } else {
//      val c = new CompositeContentResource(data.head, jsFunction(message("Resource is not writeable", Some("error"))).get)
//      del.process(path, Seq(c), ContentType.html)
    }
    null
  }
}
