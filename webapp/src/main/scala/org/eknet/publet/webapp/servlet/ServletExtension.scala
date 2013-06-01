package org.eknet.publet.webapp.servlet

import javax.servlet.http.HttpServlet
import spray.routing.{StandardRoute, RequestContext}
import javax.servlet.{Filter, Servlet}
import spray.routing.directives.CompletionMagnet
import org.eknet.publet.webapp.{PubletWeb, WebExtension}

/**
 * Extension for delegating request processing to a [[javax.servlet.http.HttpServlet]].
 *
 * Use this extension by adding it as a self type:
 * {{{
 *   trait MyServletExtension extends WebExtension {
 *     self: ServletExtension =>
 *
 *     private lazy val myServlet = (new MyServlet)
 *       .withConfig("base-path" -> "/tmp", "export-all" -> "true")
 *       .withServletPath("some/path")
 *
 *     withRoute {
 *       pathPrefix("some/path") {
 *         detachTo(singleRequestServiceActor) {
 *           complete(myServlet)
 *         }
 *       }
 *     }
 *   }
 * }}}
 *
 * It is important to specify the servlet as `lazy val` to defer the creation of the servlet context
 * to the latest possible point (when the first request hits a servlet).
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.05.13 13:02
 */
trait ServletExtension {
  self: WebExtension =>

  private val contextInitParams = collection.mutable.Map.empty[String, String]

  final def setServletContextInitParam(name: String, value: String) {
    contextInitParams.put(name, value)
  }

  private lazy val servletContext = new MiniServletContext(system, contextInitParams.toMap)

  implicit def fromFilter(filter: Filter) = fromServlet(new FilterWrapper(filter))
  implicit def fromServlet(servlet: Servlet) = new ConfiguredServlet(servlet, () => servletContext, PubletWeb(system).webSettings)
  implicit def toSettingsServlet(cs: ConfiguredServlet) = new ServletWithSettings(cs, cs.initRequestSettings)

  private final class ServletCompletion(ss: ServletWithSettings) extends CompletionMagnet {
    def route = new StandardRoute {
      def apply(ctx: RequestContext) {
        val req = new MiniServletRequest(servletContext, ss.settings.forRequest(ctx.request))
        val resp = new MiniServletResponse
        ss.cs.servlet.service(req, resp)
        ctx.complete(resp.toHttpResponse)
      }
    }
  }

  implicit def fromServlet(servlet: ServletWithSettings): CompletionMagnet = new ServletCompletion(servlet)

}
