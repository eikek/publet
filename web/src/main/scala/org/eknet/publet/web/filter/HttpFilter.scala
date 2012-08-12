/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.web.filter

import org.eknet.publet.web.{RequestAttr, RepositoryNameResolver, RequestParams, RequestUrl}
import javax.servlet.{ServletResponse, ServletRequest}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.12 23:08
 */
trait HttpFilter {

  def getRequestUtils(req:HttpServletRequest) = new ReqUtils(req)

  implicit def toHttpReq(request: ServletRequest) = request.asInstanceOf[HttpServletRequest]
  implicit def toHttpRes(res: ServletResponse) = res.asInstanceOf[HttpServletResponse]
}

class ReqUtils(val req: HttpServletRequest) extends RequestAttr with RequestUrl with RequestParams with RepositoryNameResolver
