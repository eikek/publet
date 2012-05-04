package org.eknet.publet.webeditor

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs._
import util.CompositeContentResource
import xml._
import org.eknet.publet.web.shiro.Security
import org.eknet.publet.web.{WebPublet, WebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.04.12 00:17
 */
class EditEngine(del: PubletEngine) extends PubletEngine {

  def editBody(content: ContentResource): NodeSeq = {
    val path = WebContext().requestPath
    val cancelHandler = path.withExt("html").asString
    val pushPath = Path(path.relativeRoot) / EditorWebExtension.scriptPath / "push.html"
    val delHandler = pushPath.asString+"?delete="+path.asString
    <h3>Edit Page</h3>
      <p>If you'd like to write markdown syntax,
        <a href="http://daringfireball.net/projects/markdown/syntax" target="_new">here</a>
        is the syntax definition. If you like to use some special html formatting, you can use standard yaml elements as
        <a href="http://www.yaml.de/docs/index.html#yaml-typography">defined here</a>
      </p>
      <div class="formSubmitResponse"></div>
      <form action={ pushPath.asString } method="post" class="ym-form linearize-form ym-full">
        <div class="ym-fbox-button">
          <button class="publetAjaxSubmit">Save</button>
          <a class="ym-button" href={cancelHandler}>View</a>
          <a class="ym-button" onClick="return confirm('Really delete this file?');" href={delHandler}>Delete</a>
        </div>
          {typeSelect(path, content.contentType)}
        <div class="ym-fbox-text">
          <textarea name="page" id="editPage">{content.contentAsString}</textarea>
        </div>
        <div class="ym-fbox-text">
          <label for="commitMessage">Message</label>
          <input type="text" name="commitMessage" id="commitMessage" size="20"></input>
        </div>
        <input type="hidden" name="path" value={ path.asString } />
        <input type="hidden" name="a" value="include"/>
      </form>
  }

  def editContent(content: ContentResource): ContentResource = {
    val c = NodeContent(editBody(content), ContentType.html)
    new CompositeContentResource(content, c)
  }

  private def typeSelect(path: Path, ct: ContentType): NodeSeq = {
    val publet = WebPublet().publet
    val source = publet.findSources(path).headOption

    val list = ContentType.forMimeBase(path.name.targetType)
    val extensions = list.flatMap(_.extensions).sortWith((t1, t2) => t1 < t2)

    def optionSnippet(ext: String) = {
      val o = <option>{ext}</option>
      if (source.exists(_.name.ext == ext)) {
        o % Attribute("selected", Text("selected"), Null)
      } else {
        o
      }
    }

    <div class="ym-fbox-select">
      <label for="extentionOptions">Extension<sup class="ym-required">*</sup></label>
      <select id="extentionOptions" name="extension" required="required">
      { for (ext<-extensions) yield { optionSnippet(ext) }}
      </select>
    </div>
  }

  def name = 'edit

  def process(path: Path, data: Seq[ContentResource], target: ContentType) = {
    Security.checkPerm(name, path)
    if (target.mime._1 == "text")
      del.process(path, Seq(editContent(data.head)), ContentType.html)
    else
      del.process(path, Seq(UploadContent.uploadContent(path)), ContentType.html)
  }
}
