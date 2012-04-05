package org.eknet.publet.web.filter

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 14:00
 *
 */
trait Filter {

  def handle(req: HttpServletRequest, resp: HttpServletResponse): Boolean

}
