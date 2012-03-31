package org.eknet.publet.engine

import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.source.Partitions
import org.eknet.publet._

/**
 * A "meta" engine that wraps the output of its delegate in a YAML
 * page.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 16:17
 */
class YamlEngine(engine: PubletEngine) extends PubletEngine with InstallCallback {
  
  def name = 'yaml

  def onInstall(publ: Publet) {
    publ.mount(Path("/yaml"), Partitions.yamlPartition)
  }

  def process(path:Path, data: Seq[Content], target: ContentType) = {
    val content = engine.process(path, data, target)
    content.fold(a=> content, c => {
      if (c.contentType == ContentType.html) {
        val cstr = c.contentAsString
        val title = path.fileName.name
        val css = ("../" * path.parent.size) + "yaml/simple-page.css"
        Right(Content(template(cstr, title, css), ContentType.html))
      } else {
        content
      }
    })
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
