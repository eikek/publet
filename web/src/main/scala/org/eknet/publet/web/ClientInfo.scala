package org.eknet.publet.web

import javax.servlet.http.HttpServletRequest
import util.{Request, Key}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.06.12 18:11
 */
case class ClientInfo(ip: String, agent: Option[String], referer: Option[String]) {

}

object ClientInfo {

  private val xForwardFor = "x-forwarded-for"

  private def getIpAddress(req: HttpServletRequest) = {
    Option(req.getHeader(xForwardFor)) match {
      case Some(h) => {
        h.indexOf(',') match {
          case i if (i > -1) => h.substring(0, i)
          case _ => h
        }
      }
      case None => req.getRemoteAddr
    }
  }

  def apply(req: HttpServletRequest):ClientInfo = {
    val ip = getIpAddress(req)
    val ref = Option(req.getHeader("Referer"))
    val ag = Option(req.getHeader("user-agent"))
    new ClientInfo(ip, ag, ref)
  }
}
