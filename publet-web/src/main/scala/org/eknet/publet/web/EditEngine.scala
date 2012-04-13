package org.eknet.publet.web

import org.eknet.publet.resource.{NodeContent, ContentType, Content}
import org.eknet.publet.engine.PubletEngine
import template.FilebrowserTemplate
import xml._
import org.eknet.publet.impl.InstallCallback
import java.util.UUID
import org.eknet.publet.resource.Partition._
import org.eknet.publet.{Publet, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.04.12 00:17
 */
object EditEngine extends PubletEngine with InstallCallback {

  def editBody(path: Path, content: Content): NodeSeq = {
    val cancelHandler = "window.location='"+path.fileName.name+".html'"
    <h3>Edit Page</h3>
    <p>If you'd like to write markdown syntax, <a href="http://daringfireball.net/projects/markdown/syntax" target="_new">here</a>
      is the syntax definition. If you like to use some special html formatting, you can use standard yaml elements as
      <a href="http://www.yaml.de/docs/index.html#yaml-typography">defined here</a>. </p>
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

    //split into 4 sublists
    val list = ContentType.forMimeBase(path.targetType.get)
    val sz = list.length / 4
    val parts = (list take sz,
      (list drop sz) take sz,
      (list drop (2*sz)) take sz,
      list drop (3*sz))
    val max  = scala.math.max(parts._1.length,
        scala.math.max(parts._2.length,
          scala.math.max(parts._3.length, parts._4.length)))

    <div class="ym-fbox-check">
    <table>
      {
      for (i <- 0 to (max-1)) yield {
        <tr>
          { if (i<parts._1.length) { <td> {snippet(parts._1(i))} </td>} else {<td></td>} }
          { if (i<parts._2.length) { <td> {snippet(parts._2(i))} </td>} else {<td></td>} }
          { if (i<parts._3.length) { <td> {snippet(parts._3(i))} </td>} else {<td></td>} }
          { if (i<parts._4.length) { <td> {snippet(parts._4(i))} </td>} else {<td></td>} }
        </tr>
      }
      }
    </table>
    </div>
  }
  
  def name = 'edit

  def process(path: Path, data: Seq[Content], target: ContentType) = {
    if (target.mime._1 == "text")
      Right(editContent(path, data.head))
    else
      Right(UploadContent.uploadContent(path))
  }
}
