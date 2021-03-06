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

package org.eknet.guice.squire

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.google.inject.TypeLiteral
import com.google.inject.internal.MoreTypes
import java.lang.reflect.Type

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.11.12 22:12
 */
class TypeLiteralTest extends FunSuite with ShouldMatchers {

  test ("test types of types") {
    val rtl = new TypeLiteral[List[Set[String]]](){}
    val tl = SquireBinder.typeLiteral[List[Set[String]]]
    tl should be (rtl)
  }


}
