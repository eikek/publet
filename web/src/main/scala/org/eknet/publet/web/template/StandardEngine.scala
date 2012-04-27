package org.eknet.publet.web.template

import org.eknet.publet.Publet
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.virtual.{UrlResource, MutableContainer, ClasspathContainer}
import org.eknet.publet.engine.convert.ConverterEngine
import collection.mutable
import xml.NodeSeq
import org.eknet.publet.vfs._
import org.eknet.publet.web.WebContext

/** Uses yaml to create a html page layout.
 *
 * There are two modes: one column and two column layout. The two column
 * layout is chosen, if a `sidebar.html` is found either in `.includes/`
 * of the current requested path or in `/.allIncludes`. If there is no
 * such file, the one column layout is used.
 *
 * Besides the columns, the page is divided into header and footer part.
 * Each is again searched for in `.includes/` or `/.allIncludes` by looking
 * at `header.html` and `footer.html`. The yaml copyright notice will
 * always be visible.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 12:15
 */
class StandardEngine(val publet:Publet) extends PubletEngine
    with WebIncludes with HtmlTemplate {

  val yamlPath = Path("/publet/yaml/")
  val jsPath = Path("/publet/std/js/")

  val includeEngine = ConverterEngine('include)

  val htmlHeadContribs = mutable.ListBuffer[Function2[Path, Content, String]]()

  /**
   * Mounts all shared resources into the publet instance.
   *
   * This should only be invoked once per server startup.
   * TODO (above)

   * @return
   */
  def init(): this.type  = {
    val cp = new ClasspathContainer(yamlPath, classOf[StandardEngine], Some(Path("../themes")))
    publet.mountManager.mount(yamlPath, cp)

    def urlOf(name: String) = Option(classOf[StandardEngine].getResource("../themes/"+ name))

    val jsCont = new MutableContainer(jsPath)
    val jquery = "jquery-1.7.2.min.js"
    jsCont.addResource(new UrlResource(urlOf(jquery), Some(jsCont), jsPath/jquery))
    val publJs = "publet.js"
    jsCont.addResource(new UrlResource(urlOf(publJs), Some(jsCont), jsPath/publJs))
    publet.mountManager.mount(jsPath, jsCont)

    publet.engineManager.addEngine(includeEngine)
    this
  }

  def name = 'mainWiki


  def process(data: Seq[ContentResource], target: ContentType) = {
    includeEngine.process(data, target) match {
      case Some(nc) if (nc.contentType == ContentType.html) => {
        val resource = new CompositeContentResource(data.head, nc)
        apply(resource, data)
      }
      case l @ _  => l
    }
  }

  /** Relocates ref underneath the yaml path */
  private def yamlAt(path: Path, ref: Path) = Path(path.relativeRoot) / yamlPath / ref
  private def jsAt(path: Path, ref: Path) = Path(path.relativeRoot) / jsPath / ref

  def standardHtmlHead(path: Path) = {
    val yamlcss = yamlAt(path, Path("yaml/single-page.css"))
    NodeSeq.fromSeq(<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
      <link href={ yamlcss.asString } rel="stylesheet" type="text/css"/>
      <script src={ jsAt(path, Path("jquery-1.7.2.min.js")).asString } />
      <script src={ jsAt(path, Path("publet.js")).asString } />
    ).toString()
  }

  override def htmlHead(content: ContentResource, source: Seq[ContentResource]) = {
    val path = WebContext().requestPath
    standardHtmlHead(path) +
    processInclude(path.parent/"head.html").getOrElse(Content.empty(ContentType.html)).contentAsString +
    htmlHeadContribs.toList.map(_.apply(path, content)).mkString("\n")
  }

  override def htmlBody(content: ContentResource, source: Seq[ContentResource]) = {
    val path = WebContext().requestPath
    val header = yamlHeader(path, content)
    val footer = yamlFooter(path)
    val main = stripTitle(content.contentAsString).getOrElse(content.contentAsString)
    val base = Path(path.relativeRoot) / yamlPath
    getSidebar(path) match {
      case Some(sb) => yamlHeaderMainFooter(header, yamlTwoColMain(sb.contentAsString, main), footer, base.asString)
      case None => yamlHeaderMainFooter(header, yamlOneColMain(main), footer, base.asString)
    }
  }

  def getSidebar(path: Path): Option[Content] = {
    val sidebar = path.parent / "sidebar.html"
    processInclude(sidebar)
  }

  def stripTitle(cnt: String) = {
    findHeadRegex.findFirstMatchIn(cnt) match {
      case Some(m) => Some(String.valueOf(m.before) + String.valueOf(m.after))
      case None => None
    }
  }

  def yamlHeaderMainFooter(header: String, main: String, footer: String, base:String) = {
    """<ul class="ym-skiplinks">
      <li><a class="ym-skip" href="#nav">Skip to navigation (Press Enter)</a></li>
      <li><a class="ym-skip" href="#main">Skip to main content (Press Enter)</a></li>
    </ul>
    <header>
      <div class="ym-wrapper">
        <div class="ym-wbox">
          """+ header + """
        </div>
      </div>
    </header>
    <div id="main">
      """+ main + """
    </div>
    <footer>
      <div class="ym-wrapper">
        <div class="ym-wbox">
          """+ footer + """
        </div>
      </div>
    </footer>
    <script src=""""+ base + "yaml/core/js/yaml-focusfix.js" + """"></script>"""
  }

  def yamlOneColMain(body: String) = {
    val xml = <div class="ym-wrapper ym-wrapper2">
      <div class="ym-wbox">
        %s
      </div>
    </div>
    String.format(xml.toString(), body)
  }

  def yamlTwoColMain(yamlColumn: String, body: String) = {
    val str = """<div class="ym-wrapper">
      <div class="ym-wbox">
        <section class="ym-grid linearize-level-1">
          <article class="ym-g66 ym-gl content">
            <div class="ym-gbox-left ym-clearfix">
              %s
            </div>
          </article>
          <aside class="ym-g33 ym-gr">
            <div class="ym-gbox-right ym-clearfix">
              %s
            </div>
          </aside>
        </section>
      </div>
    </div>"""

    String.format(str, body, yamlColumn)
  }

  def yamlHeader(path: Path, content: ContentResource) = {
    val custom = processInclude(path.parent/"header.html") match {
      case Some(c) => c.contentAsString
      case None => ""
    }
    <h1>{title(content, Seq[ContentResource]())}</h1>.toString() +"\n"+ custom
  }

  def yamlFooter(path: Path) = {
    val custom = processInclude(path.parent/"footer.html") match {
      case Some(c) => c.contentAsString + "\n"
      case None => ""
    }
    custom + """<p>© 2012 - Layout based on <a href="http://www.yaml.de">YAML</a></p>"""
  }
}
