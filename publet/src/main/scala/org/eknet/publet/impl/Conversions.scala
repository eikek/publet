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

package org.eknet.publet.impl

import org.eknet.publet.vfs.Content

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:10
 */
protected[publet] object Conversions {


  implicit def toOptionalEither(v: Either[Exception, Content]): Either[Exception, Option[Content]] = {
    v match {
      case Right(data) => Right(Option(data))
      case Left(x) => Left(x)
    }
  }

  def throwException(msg: String): Nothing = {
    throw new RuntimeException(msg)
  }
}
