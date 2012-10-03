package org.eknet.publet.web.filter

import org.eknet.publet.vfs.ContentResource
import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request}
import org.eknet.publet.web.Method
import com.google.common.net.HttpHeaders

/**
 * Writes the content into the HTTP response.
 *
 * The method `serveResource` does the appropriate steps to copy the
 * resource contents into the http response. All steps are given
 * in the `doXyz` methods.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 03.10.12 13:33
 * 
 */
object ServeContentResource {

  def serveResource(resource: ContentResource, req: Request, res: Response) {
    WriteResponseHandler(resource, req, res)
      .respond(doAddHeaders)
      .respond(doProcessIfModifiedSince)
      .respond(doProcessIfUnmodifiedSince)
      .respond(doWriteContent)

    res.flushBuffer()
  }

  def doAddHeaders(token: Token) = {
    token.resource.lastModification.map {lm =>
      token.response.setDateHeader(HttpHeaders.LAST_MODIFIED, lm)
    }
    token.response.setContentType(token.resource.contentType.mimeString)
    token.resource.length.map { len =>
      token.response.setContentLength(len.toInt)
    }
    Result.continue
  }

  def doProcessIfUnmodifiedSince(token: Token) = {
    token.getLastModified flatMap { lm =>
      token.dateHeader(HttpHeaders.IF_UNMODIFIED_SINCE) collect {
        case date if (lm > date) => {
          token.response.sendError(Response.SC_PRECONDITION_FAILED)
          Result.stop
        }
      }
    } getOrElse (Result.continue)
  }

  def doProcessIfModifiedSince(token: Token) = {
    token.getLastModified flatMap { lm =>
      token.dateHeader(HttpHeaders.IF_MODIFIED_SINCE) collect {
        case ifms if (lm <= ifms) => {
          token.response.reset()
          token.response.setStatus(Response.SC_NOT_MODIFIED)
          Result.stop
        }
      }
    } getOrElse (Result.continue)
  }

  def doWriteContent(token: Token) = {
    val out = token.response.getOutputStream
    token.resource.copyTo(out, close = false)
    Result.stop
  }
}