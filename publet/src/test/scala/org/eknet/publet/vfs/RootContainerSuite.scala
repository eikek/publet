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

package org.eknet.publet.vfs

import fs.FilesystemPartition
import org.eknet.publet.impl.PubletImpl
import java.io.File
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import scala.None
import com.google.common.eventbus.EventBus

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.07.12 19:55
 */
class RootContainerSuite extends FunSuite with ShouldMatchers {

  test ("lookup root") {

    val publet = new PubletImpl
    val temp = new File("/tmp")
    val pdir = new File(temp, "artifacts/maven2")
    val fs = new FilesystemPartition(pdir, new EventBus(), true)
    publet.mountManager.mount(Path("/maven2"), fs)

    val r = publet.rootContainer.lookup(Path("/maven2"))
    r should not be (None)

  }
}
