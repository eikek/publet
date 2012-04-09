package org.eknet.publet.web.template

import java.util.UUID
import org.eknet.publet.{Path, Publet}
import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.resource.NodeContent
import org.eknet.publet.resource.Partition._
import xml.{NodeSeq, Node}
import xml.transform.{RuleTransformer, RewriteRule}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.04.12 22:39
 */
trait YamlTemplate extends InstallCallback with HtmlTemplate {

  private val randPath = UUID.randomUUID().toString

  val yamlPartition = classpath(Path("../themes/yaml"), classOf[YamlTemplate])

  override def onInstall(publ: Publet) {
    super.onInstall(publ)
    publ.mount(Path("/"+randPath+"/yaml"), yamlPartition)
  }

  override def headerHtml(path: Path, content: NodeContent) = {
    val base = path.relativeRoot + randPath + "/"
    super.headerHtml(path, content) ++
    {
      <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
      <link href={ base+"yaml/single-page.css" } rel="stylesheet" type="text/css"/>
    }
  }

  def yamlHead(path: Path, content: NodeContent): NodeSeq = {
    <a href="?a=edit" style="display:inline; float:right" class="ym-clearfix">Edit</a>
  }

  def removeHeadline(content:NodeContent): Option[NodeSeq] = {
    findHead(content, 1, 4) map { titleNode => {
        val removeTitle = new RewriteRule {
          override def transform(n: Node) = {
            if (n == titleNode) NodeSeq.Empty else n
          }
        }
        new RuleTransformer(removeTitle).transform(content.node)
      }
    }
  }

  def yamlMain(path: Path, content: NodeContent): NodeSeq = {
    //remove page title from body if present
    val body = removeHeadline(content)
    
    <div class="ym-wrapper ym-wrapper2">
      <div class="ym-wbox">
        { body.getOrElse(content.node) }
      </div>
    </div>
  }
  
  override final def bodyHtml(path: Path, content: NodeContent) = {
    val base = path.relativeRoot + randPath + "/"
    <ul class="ym-skiplinks">
      <li><a class="ym-skip" href="#nav">Skip to navigation (Press Enter)</a></li>
      <li><a class="ym-skip" href="#main">Skip to main content (Press Enter)</a></li>
    </ul>
    <header>
      <div class="ym-wrapper">
        <div class="ym-wbox">
          <h1> { title(path, content) } </h1>
          { yamlHead(path, content) }
        </div>
      </div>
    </header>
    <div id="main">
      { yamlMain(path, content) }
    </div>
    <footer>
      <div class="ym-wrapper">
        <div class="ym-wbox">
          <p>© 2012 - Layout based on <a href="http://www.yaml.de">YAML</a></p>
        </div>
      </div>
    </footer>
    <script src={ base + "yaml/core/js/yaml-focusfix.js" }></script>
  }

}
