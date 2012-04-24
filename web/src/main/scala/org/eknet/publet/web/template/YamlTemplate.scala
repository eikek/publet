package org.eknet.publet.web.template

import java.util.UUID
import org.eknet.publet.{Path, Publet}
import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.resource.Partition._
import xml.transform.{RuleTransformer, RewriteRule}
import org.eknet.publet.resource.{ContentType, Content, NodeContent}
import xml.{Null, NodeSeq, Node}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 22:39
 */
trait YamlTemplate extends InstallCallback with HtmlTemplate {

  private val randPath = UUID.randomUUID().toString
  private val footerRegex = "<hr/?>(</hr>)?<hr/?>(</hr>)?".r
  
  val yamlPartition = classpath(Path("../themes/yaml"), classOf[YamlTemplate])

  override def onInstall(publ: Publet) {
    super.onInstall(publ)
    publ.mount(Path("/"+randPath+"/yaml"), yamlPartition)
  }

  override def headerHtml(path: Path, content: Content, source: Seq[Content]) = {
    val base = path.relativeRoot + randPath + "/"
    super.headerHtml(path, content, source) +
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>.toString() +
    <link href={ base+"yaml/single-page.css" } rel="stylesheet" type="text/css"/>.toString()
  }

  def yamlHead(path: Path, content: Content, source: Seq[Content]): String = {
    val evalLink = if (source.find(_.contentType == ContentType.scal).isDefined)
      { <span> :: </span><a href="?a=eval">Eval</a>}
    else
      NodeSeq.Empty
      
    NodeSeq.fromSeq(<span style="float:right" class="ym-clearfix">
    <a href="?a=edit">Edit</a> { evalLink }
    </span>).toString()
  }

  def stripHeadAndFoot(content:Content): Option[String] = {
    val cnt = content.contentAsString.lastIndexOf("<hr></hr><hr></hr>") match {
      case -1 => content.contentAsString
      case in => content.contentAsString.substring(0, in)
    }

    findHeadRegex.findFirstMatchIn(cnt) match {
      case Some(m) => Some(String.valueOf(m.before) + String.valueOf(m.after))
      case None => None
    }
  }

  def yamlMain(path: Path, content: Content, source: Seq[Content]): String = {
    val body = stripHeadAndFoot(content)
    val xml = <div class="ym-wrapper ym-wrapper2">
      <div class="ym-wbox">
        %s
      </div>
    </div>
    String.format(xml.toString(), body.getOrElse(content.contentAsString))
  }
  
  def yamlFooter(path: Path, content: Content, source: Seq[Content]): String = {
    (footerRegex.findFirstMatchIn(content.contentAsString) match {
      case Some(m) => String.valueOf(m.after) + "<br/><br/><hr/>"
      case None => ""
    }) +
      """<p>© 2012 - Layout based on <a href="http://www.yaml.de">YAML</a></p>"""
  }
  
  override final def bodyHtml(path: Path, content: Content, source: Seq[Content]) = {
    val base = path.relativeRoot + randPath + "/"

    """<ul class="ym-skiplinks">
      <li><a class="ym-skip" href="#nav">Skip to navigation (Press Enter)</a></li>
      <li><a class="ym-skip" href="#main">Skip to main content (Press Enter)</a></li>
    </ul>
    <header>
      <div class="ym-wrapper">
        <div class="ym-wbox">
          <h1> """+ title(path, content, source)+ """ </h1>
          """+ yamlHead(path, content, source) + """
        </div>
      </div>
    </header>
    <div id="main">
      """+ yamlMain(path, content, source) + """
    </div>
    <footer>
      <div class="ym-wrapper">
        <div class="ym-wbox">
          """+ yamlFooter(path,content, source) + """
        </div>
      </div>
    </footer>
    <script src=""""+ base + "yaml/core/js/yaml-focusfix.js" + """"></script>"""
    
  }

}
