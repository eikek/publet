package org.eknet.publet.engine

import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.source.Partitions
import org.eknet.publet._
import java.util.UUID

/**
 * A "meta" engine that wraps the output of its delegate in a YAML
 * page.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 16:17
 */
class YamlEngine(engine: PubletEngine) extends PubletEngine with InstallCallback {

  private val randPath = UUID.randomUUID().toString

  def name = 'yaml

  def onInstall(publ: Publet) {
    publ.mount(Path("/"+randPath+"/yaml"), Partitions.yamlPartition)
    publ.mount(Path("/"+randPath+"/highlight"), Partitions.highlightPartition)
  }

  def process(path:Path, data: Seq[Content], target: ContentType) = {
    val content = engine.process(path, data, target)
    content.fold(a=> content, c => {
      if (c.contentType == ContentType.html) {
        Right(Content(template(path, c), ContentType.html))
      } else {
        content
      }
    })
  }

  private def template(path: Path, content: Content) = {
    val base = ("../" * path.parent.size) + randPath+ "/"
    val title = path.fileName.name

    """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
         "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
     <meta charset="utf-8"/>
     <title> """ + title + """ </title>

     <!-- Mobile viewport optimisation -->
	   <meta name="viewport" content="width=device-width, initial-scale=1.0">

     <link href="""" + base+"yaml/simple-page.css" + """" rel="stylesheet" type="text/css"/>
     <link rel="stylesheet" href=""""+ base + "highlight/styles/zenburn.css" + """">
     <script src="""" + base + "highlight/highlight.pack.js"+ """"></script>
     <script>hljs.initHighlightingOnLoad();</script>
  </head>
  <body>
    <ul class="ym-skiplinks">
      <li><a class="ym-skip" href="#nav">Skip to navigation (Press Enter)</a></li>
      <li><a class="ym-skip" href="#main">Skip to main content (Press Enter)</a></li>
    </ul>
    <header>
      <div class="ym-wrapper">
        <div class="ym-wbox">
          <h1> """ + title + """ </h1>
        </div>
      </div>
    </header>
    <div id="main">
      <div class="ym-wrapper">
        <div class="ym-wbox">
          <section class="ym-grid linearize-level-1">
            <article class="ym-g66 ym-gl content">
            """ +
              content.contentAsString +
            """
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

    <!-- full skip link functionality in webkit browsers -->
    <script src="""" +  base + "core/js/yaml-focusfix.js" + """"></script>
  </body>
</html>
    """
  }
}
