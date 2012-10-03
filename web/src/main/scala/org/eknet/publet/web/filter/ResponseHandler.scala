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

import org.eknet.publet.vfs.ContentResource
import javax.servlet.http.{HttpServletResponse => Response, HttpServletRequest => Request}
import org.eknet.publet.web.Method

/**
 * Chain for handling http response. The idea is to create a [[org.eknet.publet.web.filter.WriteResponseHandler]]
 * initially and hand functions into the `respond` method. Those functions do something with the response and
 * may either return `Result.stop` or `Result.continue` depending on whether the next function should get the
 * chance to also write into the response.
 *
 * The object [[org.eknet.publet.web.filter.ServeContentResource]] defines some methods that can be passed
 * into `respond`.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.10.12 20:37
 */
sealed abstract class ResponseHandler {
  def respond(f: Token => Result.Value): ResponseHandler
}

object NullResponseHandler extends ResponseHandler {
  def respond(f: Token => Result.Value) = NullResponseHandler
}

final case class WriteResponseHandler(token: Token) extends ResponseHandler {
  def respond(f: Token => Result.Value) = {
    f(token) match {
      case Result.continue => this
      case Result.stop => NullResponseHandler
    }
  }
}

object WriteResponseHandler {
  def apply(r: ContentResource, req: Request, res: Response): ResponseHandler = WriteResponseHandler(Token(r, req, res))
}

object Result extends Enumeration {
  val continue, stop = Value
}

case class Token(resource: ContentResource, request: Request, response: Response) {

  def dateHeader(name: String) =
    Option(request.getDateHeader(name)).collect({case m if (m != -1) => m})

  /**
   * Returns the last-modified property of the resource if available
   * and the http method is not `HEAD`. This information is taken from
   * the `org.eclipse.jetty.servlet.DefaultServlet` when processing the
   * `If-(Un)modified-Since` headers.
   *
   * @return
   */
  def getLastModified =
    resource.lastModification.collect({ case lm if (request.getMethod != Method.head.toString) => lm})

}
