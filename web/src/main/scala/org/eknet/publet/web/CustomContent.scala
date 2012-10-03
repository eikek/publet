package org.eknet.publet.web

import org.eknet.publet.vfs.{ContentType, Content}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import java.io.InputStream

/**
 * In case you want to take care of response handling yourself, implement
 * the `send` method.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 03.10.12 13:55
 * 
 */
trait CustomContent extends Content {

  /**
   * Writes the response data. This method is called last and is expected
   * to write data into the response' output stream.
   *
   * @param req
   * @param resp
   */
  def send(req: HttpServletRequest, resp: HttpServletResponse)

  def contentType = ContentType.unknown
  def inputStream: InputStream = sys.error("No input stream available")
}
