package org.eknet.publet.web.filter

import org.eknet.publet.web.{RepositoryNameResolver, RequestParams, RequestUrl}
import javax.servlet.{ServletResponse, ServletRequest}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 23:08
 */
trait HttpFilter {

  class ReqUtils(val req: HttpServletRequest) extends RequestUrl with RequestParams with RepositoryNameResolver

  def getRequestUtils(req:HttpServletRequest) = new ReqUtils(req)

  implicit def toHttpReq(request: ServletRequest) = request.asInstanceOf[HttpServletRequest]
  implicit def toHttpRes(res: ServletResponse) = res.asInstanceOf[HttpServletResponse]
}
