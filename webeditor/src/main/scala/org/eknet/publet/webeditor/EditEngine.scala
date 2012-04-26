package org.eknet.publet.webeditor

import org.eknet.publet.engine.PubletEngine
import xml._
import org.eknet.publet.vfs.{Path, NodeContent, ContentType, Content}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.04.12 00:17
 */
class EditEngine(del: PubletEngine) extends PubletEngine {

  def editBody(path: Path, content: Content): NodeSeq = {
    val cancelHandler = "window.location='" + path.name.withExtension("html") +"'"
    val pushPath = Path(path.relativeRoot) / EditorWebExtension.scriptPath / "push.json"
    <h3>Edit Page</h3>
      <p>If you'd like to write markdown syntax,
        <a href="http://daringfireball.net/projects/markdown/syntax" target="_new">here</a>
        is the syntax definition. If you like to use some special html formatting, you can use standard yaml elements as
        <a href="http://www.yaml.de/docs/index.html#yaml-typography">defined here</a>
      </p>
      <form action={ pushPath.asString } method="post" class="ym-form linearize-form ym-full">
        {typeSelect(path, content.contentType)}<div class="ym-fbox-text">
        <textarea name="page" id="editPage">{content.contentAsString}</textarea>
      </div>
        <div class="ym-fbox-button">
          <input type="submit" value="Save"></input>
          <input type="button" value="Cancel" onClick={cancelHandler}></input>
        </div>
        <input type="hidden" name="path" value={ path.asString } />
      </form>
  }

  def editContent(path: Path, content: Content): NodeContent = NodeContent(editBody(path, content), ContentType.html)

  private def typeSelect(path: Path, ct: ContentType): NodeSeq = {

    def snippet(t: ContentType) = {
      val ck = <input type="radio" name="type" value={t.typeName.name} id={t.typeName.name}></input>;
      <div>
        {if (t == ct) (ck % Attribute(None, "checked", Text("checked"), Null)) else ck}<label for={t.typeName.name}>
        {t.typeName.name}
      </label>
      </div>
    }

    val cols = 4
    //split into $cols sublists
    val list = ContentType.forMimeBase(path.name.targetType)
    val parts = list.grouped(list.length / cols).toList
    val max = parts.foldLeft(0)((i, list) => scala.math.max(i, list.length))

    <div class="ym-fbox-check">
      <table>
        {for (r <- 0 to (max - 1)) yield {
        <tr>
          {for (c <- 0 to (cols - 1)) yield {
          if (r < parts(c).length) {
            <td>
              {snippet(parts(c)(r))}
            </td>
          } else {
            <td></td>
          }
        }}
        </tr>
      }}
      </table>
    </div>
  }

  def name = 'edit

  def process(path: Path, data: Seq[Content], target: ContentType) = {
    if (target.mime._1 == "text")
      del.process(path, Seq(editContent(path, data.head)), ContentType.html)
    else
      del.process(path, Seq(UploadContent.uploadContent(path)), ContentType.html)
  }
}
