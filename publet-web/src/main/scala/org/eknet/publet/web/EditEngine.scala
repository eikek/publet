package org.eknet.publet.web

import org.eknet.publet.Path
import org.eknet.publet.resource.{NodeContent, ContentType, Content}
import org.eknet.publet.engine.PubletEngine
import xml._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.04.12 00:17
 */
object EditEngine extends PubletEngine {

  def editBody(path: Path, content: Content): NodeSeq = {
    val cancelHandler = "window.location='"+path.fileName.name+".html'"
    <h3>Edit Page</h3>
    <p>If you'd like to write markdown syntax, <a href="http://daringfireball.net/projects/markdown/syntax" target="_new">here</a>
      is the syntax definition.</p>
    <div id="filesTree"></div>
    <form action={ path.fileName.name+".html" } method="post" class="ym-form linearize-form ym-full">
      { typeSelect(path, content.contentType) }
      <div class="ym-fbox-text">
        <textarea name="page">{ content.contentAsString }</textarea>
      </div>
      <div class="ym-fbox-button">
        <input type="submit" value="Save"></input>
        <input type="button" value="Cancel" onClick={ cancelHandler }></input>
      </div>
    </form>
  }
  
  def editContent(path: Path, content:Content): NodeContent = NodeContent(editBody(path, content), ContentType.html)

  private def typeSelect(path: Path, ct: ContentType): NodeSeq = {

    def snippet(t: ContentType) = {
      val ck = <input type="radio" name="type" value={ t.typeName.name } id={t.typeName.name}></input>;
      <div>
        { if (t==ct) (ck % Attribute(None, "checked", Text("checked"), Null)) else ck }
        <label for={t.typeName.name}>{t.typeName.name}</label>
      </div>
    }

    <div class="ym-fbox-check">
      { ContentType.forMimeBase(path.targetType.get).flatMap(snippet) }
    </div>
  }
  
  def name = 'edit

  def process(path: Path, data: Seq[Content], target: ContentType) = Right(editContent(path, data.head))
}
