/*
 * Copyright 2013 Eike Kettner
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

package org.eknet.publet.gitr.webui.scripts

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.13 12:57
 */
class DurationSuite extends FunSuite with ShouldMatchers {

  import Duration._

  test ("conversions") {
    14.days in Minute should be (14*24*60)
    1.day in Day should be (1)
    1.day in Hour should be (24)
    2.days in Hour should be (48)
    (-2).days in Hour should be (-48)
    1.hours in Minute should be (60)
    2.hours in Minute should be (120)
    2.hours in Second should be (7200)
    1.minute in Second should be (60)
    2.minutes in Second should be (120)
    1.second in Millis should be (1000)

    2.hours in Second should be (7200)
    1000.millis in Second should be (1)
    2000.millis in Second should be (2)
    120.minutes in Hour should be (2)
    150.minutes in Hour should be (2)
    28.hours in Day should be (1)
    18.hours in Day should be (0)
    56.hours in Day should be (2)

    960.hours in Day should be (40)
    960.hours in Month should be (1)
    960.days in Year should be (2)

    val now = 1366456822174L.millis
    1336156665090L.millis.until(now) in Month should be (11)
  }

  test ("age") {
    1.minute.age should be (Minute -> 1)
    7200.seconds.age should be (Hour -> 2)

    960.hours.age should be (Month -> 1)
    960.hours.ageString should be ("1 month")

    val now = 1366456822174L.millis
    1266456822174L.millis.until(now).age should be (Year -> 3)
    1266456822174L.millis.until(now).ageString should be ("3 years")
  }
}
