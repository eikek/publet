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

package org.eknet.publet.web.asset.impl

import java.io.{ByteArrayInputStream, SequenceInputStream, InputStream}
import java.util

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.09.12 19:13
 */
class ConcatInputStream(ins: Iterable[InputStream])
  extends SequenceInputStream(ConcatInputStream.inputEnumeration(ins))

object ConcatInputStream {

  def apply(ins: Iterable[InputStream]): ConcatInputStream = new ConcatInputStream(ins)

  private def inputEnumeration(ins: Iterable[InputStream]): util.Enumeration[InputStream] = {
    def newline = new ByteArrayInputStream("\n\n\n".getBytes("UTF-8"))
    val mixed = ins.flatMap(is => List(is, newline))
    toEnumeration(mixed)
  }

  class ListEnumeration[A](e: Iterable[A]) extends util.Enumeration[A] {
    val iter = e.iterator
    def hasMoreElements = iter.hasNext
    def nextElement() = iter.next()
  }
  implicit def toEnumeration[A](e: Iterable[A]) = new ListEnumeration[A](e)
}
