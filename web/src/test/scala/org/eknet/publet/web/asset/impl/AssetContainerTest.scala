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

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import java.nio.file.Files
import org.eknet.publet.vfs.Path
import com.google.common.eventbus.EventBus

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.09.12 15:26
 */
class AssetContainerTest extends FunSuite with ShouldMatchers with BeforeAndAfter {
  import ResourceHelper._

  val cnt = new AssetContainer(Files.createTempDirectory("assetcontainer").toFile, new EventBus())

  test ("mount and resolve") {
    cnt.mount(jqueryGroup)

    val path = Path("/groups/jquery/js/jquery-1.8.2.min.js")
    cnt.lookup(path) should not be None
  }
}
