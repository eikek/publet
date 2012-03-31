package org.eknet.publet.postproc

import org.eknet.publet.source.Partitions
import org.eknet.publet.{ContentType, Path, Publet, Content}
import xml.XML


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 12:26
 */
object YamlTemplate extends PostProcessor {

  def process(path: Path, content: Content) = {
    if (content.contentType == ContentType.html) {
      val c = content.contentAsString
      val title = "project"
      val css = ("../" * path.parent.segments.length) + "yaml/simple-page.css"
      Content(template(c, title, css), ContentType.html)
    } else {
      content
    }
  }
  
  override def onInstall(publ: Publet) {
    publ.mount(Path("/yaml"), Partitions.yamlPartition)
  }

  private def template(content: String, title: String, css: String) = {
    (<html>
      <head>
        <title> { title } </title>
        <link href={ css } rel="stylesheet" type="text/css"/>
      </head>
      <body>
        <header>
          <div class="ym-wrapper">
            <div class="ym-wbox">
              <h1> { title } </h1>
            </div>
          </div>
        </header>
        <div id="main">
          <div class="ym-wrapper">
            <div class="ym-wbox">
              <section class="ym-grid linearize-level-1">
                <article class="ym-g66 ym-gl content">

                   $$$content$$$

                </article></section>
            </div>
          </div>
        </div>
        <footer>
          <div class="ym-wrapper">
            <div class="ym-wbox">
              <p>© 2012 - Layout based on <a href="http://www.yaml.de">YAML</a></p>
            </div>
          </div>
        </footer>
      </body>
    </html>).toString().replace("$$$content$$$", content)
  }
}
