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

package org.eknet.publet.web

import javax.servlet.http.HttpServletRequest
import util.{Key, AttributeMap}
import java.util.Locale

/**
 * Helper trait for accessing attributes.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 12.08.12 16:01
 */
trait RequestAttr {

  protected def req: HttpServletRequest

  def sessionMap = AttributeMap(req.getSession)
  def requestMap = AttributeMap(req)

  /**
   * Gets the value of the specified key from one of the attribute maps.
   *
   * It first tries the request, than the session.
   *
   * @param key
   * @tparam T
   * @return
   */
  def attr[T: Manifest](key: Key[T]) = {
    requestMap.get(key).orElse {
      key.synchronized {
        sessionMap.get(key)
      }
    }
  }

  /**
   * Returns the http request method.
   * @return
   */
  def getMethod: Method.Value = Method.withName(req.getMethod.toUpperCase(Locale.ROOT))

  def getLocale = req.getLocale
}
