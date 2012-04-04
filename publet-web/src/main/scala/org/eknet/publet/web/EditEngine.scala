package org.eknet.publet.web

import org.eknet.publet.Path
import org.eknet.publet.resource.{NodeContent, ContentType, Content}
import xml.NodeSeq
import org.eknet.publet.engine.PubletEngine

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.04.12 00:17
 */
object EditEngine extends PubletEngine {

  def editBody(path: Path, content: Content): NodeSeq = {
    <h3>Edit Page</h3>
    <p>If you'd like to write markdown syntax, <a href="http://daringfireball.net/projects/markdown/syntax" target="_new">here</a>
      is the syntax definition.</p>
    <form action={ path.segments.last } method="post" class="ym-form linearize-form ym-full">
      { typeSelect(path, content.contentType) }
      <div class="ym-fbox-text">
        <textarea name="page">{ content.contentAsString }</textarea>
      </div>
      <div class="ym-fbox-button">
        <input type="submit" value="Save"></input>
        <input type="button" value="Cancel" onClick="window.location={ path.segments.last }"></input>
      </div>
    </form>
  }
  
  def editContent(path: Path, content:Content): NodeContent = NodeContent(editBody(path, content), ContentType.html)


//  private def template(path: Path, content: Content): String = {
//    """
//    <h3>Edit Page</h3>
//    <p>If you'd like to write markdown syntax, <a href="http://daringfireball.net/projects/markdown/syntax" target="_new">here</a>
//    is the syntax definition.</p>
//    <form action="""" + path.segments.last + """" method="post" class="ym-form linearize-form ym-full" >
//    """ + typeSelect(path, content) + """
//      <div class="ym-fbox-text">
//        <textarea name="page">
//""" + content.contentAsString + """</textarea>
//     </div>
//     <div class="ym-fbox-button">
//        <input type="submit" value="Save">
//        <input type="button" value="Cancel" onClick="window.location='"""+ path.segments.last +"""'">
//     </div>
//    </form>
//    """
//  }
//
//  private def typeSelect(path: Path, content: Content): String = {
//    val templ = "<div><input type=\"radio\" name=\"type\" value=\"$n\" id=\"$n\" $s> <label for=\"$n\">$n</label></div>"
//    val list = for (t<-ContentType.forMimeBase(path.targetType.get))
//    yield
//      if (t == content.contentType) templ.replace("$n", t.typeName.name).replace("$s", "checked")
//      else templ.replace("$n", t.typeName.name).replace("$s", "")
//
//    "<div class=\"ym-fbox-check\">\n" + list.mkString("\n") + "</div>"
//  }
  
  private def typeSelect(path: Path, ct: ContentType): NodeSeq = {
    def snippet(t: ContentType) = {
      <div>
        <input type="radio" name="type" value={ t.typeName.name } id={t.typeName.name} checked={ if (t==ct) "checked" else "unchecked" }></input>
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
