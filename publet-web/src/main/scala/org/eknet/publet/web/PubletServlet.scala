package org.eknet.publet.web

import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import org.eknet.publet.source.FilesystemPartition
import org.eknet.publet.{Content, ContentType, Path, Publet}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 27.03.12 22:42
 */
class PubletServlet extends HttpServlet {

  private val publet = Publet.default(Path("/"), new FilesystemPartition("/tmp/publet"))

  def action(req: HttpServletRequest, resp: HttpServletResponse) {
    
    val path = Path(req.getRequestURI).strip
    val html = publet.process(path, path.targetType.getOrElse(ContentType.html))

    path.segments.head match {
      case "css" => html.fold(writeError(_, path, resp), writePage(_, path, resp))
      case _ => {
        val out = resp.getOutputStream
        out.println("<html>")
        out.println("<head>")
        out.println("<title>"+ path.segments.last +"</title>")
        out.println("<link href=\"/publet-web/css/base.css\" rel=\"stylesheet\" type=\"text/css\"/>")
        out.println("<link href=\"/publet-web/css/gray-theme.css\" rel=\"stylesheet\" type=\"text/css\"/>")
        out.println("<link href=\"/publet-web/css/typography.css\" rel=\"stylesheet\" type=\"text/css\"/>")
        out.println("<link href=\"/publet-web/css/screen-FULLPAGE-layout.css\" rel=\"stylesheet\" type=\"text/css\"/>")
        out.println("</head>")
        
        out.println("""
        <body>
        <header>
	<div class="ym-wrapper">
		<div class="ym-wbox">
			<h1>PROJECT</h1>
		</div>
	</div>
</header>
        <div id="main">
        <div class="ym-wrapper">
		<div class="ym-wbox">
		<section class="ym-grid linearize-level-1">
				<article class="ym-g66 ym-gl content">

        """)
        html.fold(writeError(_, path, resp), writePage(_, path, resp))

        out.println("""
        </article></section>
        </div></div>
        </div>
        </body>
        </html>""")
        out.flush()
      }
    }
  }

  def writeError(ex: Exception, path: Path, resp: HttpServletResponse) {
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
//    resp.getOutputStream.println("<h3>PATH: "+ path.asString+ "</h3>")
  }

  def writePage(page: Option[Content], path: Path, resp: HttpServletResponse) {
    val out = resp.getOutputStream
    page match {
      case None => resp.sendError(HttpServletResponse.SC_NOT_FOUND)
      case Some(p) => out.println(p.contentAsString)
    }
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) {
    action(req, resp)
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse) {
    action(req, resp)
  }
}
