package org.eknet.publet.engine

import java.util.UUID
import org.eknet.publet.impl.InstallCallback
import org.eknet.publet.{Publet, ContentType, Content, Path}
import org.eknet.publet.source.Partitions


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 22:06
 */
trait YamlTemplate extends HtmlTemplate with InstallCallback {

  private val randPath = UUID.randomUUID().toString

  override def onInstall(publ: Publet) {
    super.onInstall(publ)
    publ.mount(Path("/"+randPath+"/yaml"), Partitions.yamlPartition)
  }

  override def headerContent(path: Path, content: Content) = {
    val base = path.relativeRoot + randPath + "/"
    """<link href="""" + base+"yaml/single-page.css" + """" rel="stylesheet" type="text/css"/>
    """
  }

  def applyTemplate(path: Path, content: Content) = {
    val base = path.relativeRoot + randPath + "/"
    val pageTitle = title(path, content)
    val body = """<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
         "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
  <head>
     <meta charset="utf-8"/>
     <title> """ + pageTitle + """ </title>

     <!-- Mobile viewport optimisation -->
	   <meta name="viewport" content="width=device-width, initial-scale=1.0">
     """ +
    headerContent (path, content) +
     """
  </head>
  <body>
    <ul class="ym-skiplinks">
      <li><a class="ym-skip" href="#nav">Skip to navigation (Press Enter)</a></li>
      <li><a class="ym-skip" href="#main">Skip to main content (Press Enter)</a></li>
    </ul>
    <header>
      <div class="ym-wrapper">
        <div class="ym-wbox">
          <h1> """ + pageTitle + """ </h1>
        </div>
      </div>
    </header>
    <div id="main">
      <div class="ym-wrapper">
        <div class="ym-wbox">
          <section class="ym-grid linearize-level-1">

            """ + // <article class="ym-gl content">
      content.contentAsString +
      // </article>
      """
           </section>
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
     <script src="""" + base + "yaml/core/js/yaml-focusfix.js" + """"></script>
  </body>
</html>
    """
    Content(body, ContentType.html)
  }
}
