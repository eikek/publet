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

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.eknet.publet.vfs.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 11.11.12 15:18
 */
class PartitionTypeMountTest extends FunSuite with ShouldMatchers {

  test ("partial test") {

    object FsType extends PartitionTypeMount("fs") {
      def apply(v1: PartitionConfig) = 1
    }

    val cfg = PartitionConfig("git", "files", List(Path("/dav/files")))

    FsType.isDefinedAt(cfg) should be (false)
  }

}
