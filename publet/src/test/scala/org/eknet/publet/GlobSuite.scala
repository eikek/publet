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

package org.eknet.publet

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 29.05.12 20:59
 */
class GlobSuite extends FunSuite with ShouldMatchers {

  test ("plain strings with no wildcards") {
    Glob("/ab/dd/e").consume("/ab/dd/e") should be (true)
    Glob("asda/sss/02").consume("asda/sss/02") should be (true)
    Glob("/ab/qq/d") matches ("/ax/qq/d") should be (false)
    Glob("ab/d") matches ("ab/de") should be (false)
  }

  test ("one-char placeholders") {
    Glob("/?b/d?/e").consume("/ab/dd/e") should be (true)
    Glob("/?b/d?/e").consume("/vb/d2/e") should be (true)
    Glob("/?b/d?/e").consume("/1b/dx/e") should be (true)
    Glob("/?b/d?/e").matches("/1b/xd/e") should be (false)

    Glob("asda/s??s/02").consume("asda/s2Ks/02") should be (true)
    Glob("asda/s??s/02").matches("asda/s2Kxs/02") should be (false)

    Glob("??ab/qq/d?") consume ("siab/qq/d_") should be (true)
    Glob("??ab/qq/d?") matches ("xsiab/qq/d_") should be (false)
  }

  test ("single star placeholders") {
    Glob("/dev/*/name.pdf") matches("/dev/hallo/name.pdf") should be (true)
    Glob("/dev/*/name.pdf") matches("/dev/hallo/priv/name.pdf") should be (false)
    Glob("/dev/*/*/name.pdf") matches("/dev/hallo/priv/name.pdf") should be (true)

    Glob("/dev/*/*.pdf") consume ("/dev/hallo/name.pdf") should be (true)
    Glob("/dev/*/*.pdf") matches ("/dev/hallo/new/name.pdf") should be (false)
    Glob("/dev/*/*/*.pdf") matches ("/dev/hallo/new/name.pdf") should be (true)

    Glob("*/hallo/test") matches ("/test/hallo/test") should be (false)
    Glob("*/hallo/test") matches ("test/hallo/test") should be (true)

    Glob("/hallo/test/*") matches ("/hallo/test/") should be (true)
    Glob("/hallo/test/*") matches ("/hallo/test/sdf") should be (true)
    Glob("/hallo/test/*") matches ("/hallo/test/s22") should be (true)
    Glob("/hallo/test/*") matches ("/hallo/test/s22.pdf") should be (true)
    Glob("/hallo/test/*") matches ("/hallo/test/s22/h.pdf") should be (false)
  }

  test ("double char placeholders") {
    Glob("/hallo/**") matches ("/hallo/test/s22/h.pdf") should be (true)
    Glob("/hallo/**/*.txt") matches ("/hallo/test/s22/h.pdf") should be (false)
    Glob("/**") matches ("/hallo/test/s22/h.pdf") should be (true)
    Glob("/**") matches ("hallo/test/s22/h.pdf") should be (false)
    Glob("**/name/*.pdf") matches ("/hallo/test/s22/name/h.pdf") should be (true)
    Glob("**/name/*.pdf") matches ("/hallo/test/s22/name/h.txt") should be (false)
    Glob("**/name/*") matches ("/hallo/test/s22/name/h.txt") should be (true)
    Glob("**/name/*") matches ("/hallo/test/s22/name/dev/h.txt") should be (false)
  }

  test ("simple implies") {
    Glob("/dev/**").implies(Glob("/dev/ab/**")) should be (true)
    Glob("/dev/**").implies(Glob("/ab/**")) should be (false)

    Glob("/?b/home/**").implies(Glob("/ab/home/tests/**")) should be (true)
    Glob("/?b/home/**").implies(Glob("/ab/home/**")) should be (true)

    Glob("/ab/*/c").implies(Glob("/?b/*/c")) should be (false)
    Glob("/a?/*/c").implies(Glob("/ab/*/c")) should be (true)

    Glob("**/name/*.pdf").implies(Glob("/one/two/name/**")) should be (false)
    Glob("/*/*/name/*.pdf").implies(Glob("/one/two/name/*.pdf")) should be (true)
    Glob("**/name/*.pdf").implies(Glob("/one/name/*.pdf")) should be (true)

    Glob("/aa/bb/cc").implies(Glob("/aa/bb/cc")) should be (true)
    Glob("/aa/bb/cc").implies(Glob("/aa/abb/cc")) should be (false)

    Glob("/**").implies(Glob("**")) should be (false)
    Glob("**").implies(Glob("**")) should be (true)
  }
}
