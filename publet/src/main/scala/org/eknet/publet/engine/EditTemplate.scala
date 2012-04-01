package org.eknet.publet.engine

import org.eknet.publet.{ContentType, Content, Path}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 10:47
 */
trait  EditTemplate extends HtmlTemplate {

  def applyTemplate(path: Path, content: Content) = Content(template(path, content), ContentType.html)

  private def template(path: Path, content: Content): String = {
    """
    <h3>Edit Page</h3>
    <p>If you'd like to write markdown syntax, <a href="http://daringfireball.net/projects/markdown/syntax" target="_new">here</a>
    is the syntax definition.</p>
    <form action="""" + actionString(path) + """" method="post" class="ym-form linearize-form ym-full" >
    """ + typeSelect(path, content) + """
      <div class="ym-fbox-text">
        <textarea name="page">
""" + content.contentAsString + """</textarea>
     </div>
     <div class="ym-fbox-button">
        <input type="submit" value="Save">
        <input type="button" value="Cancel" onClick="window.location='"""+actionString(path)+"""'">
     </div>
    </form>
    """
  }
  
  private def typeSelect(path: Path, content: Content): String = {
    val templ = "<div><input type=\"radio\" name=\"type\" value=\"$n\" id=\"$n\" $s> <label for=\"$n\">$n</label></div>"
    val list = for (t<-ContentType.forMimeBase(path.targetType.get))
                yield
                if (t == content.contentType) templ.replace("$n", t.typeName.name).replace("$s", "checked")
                else templ.replace("$n", t.typeName.name).replace("$s", "")

    "<div class=\"ym-fbox-check\">\n" + list.mkString("\n") + "</div>"
  }
}
