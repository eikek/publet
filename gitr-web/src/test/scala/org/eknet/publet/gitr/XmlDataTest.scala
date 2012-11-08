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

package org.eknet.publet.gitr

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.io.{FileOutputStream, File}
import org.eknet.publet.vfs.{Path, ContentResource, Content}
import org.eknet.publet.vfs.fs.FileResource
import com.google.common.eventbus.EventBus
import org.eknet.publet.gitr.auth.{RepositoryTag, RepositoryModel, XmlData}
import org.eknet.publet.auth.store.{UserProperty, User, UserStoreAdapter}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 08.11.12 23:32
 */
class XmlDataTest extends FunSuite with ShouldMatchers {

  val db = {
    val temp = File.createTempFile("repo.db", ".xml")
    temp.deleteOnExit()
    val org = getClass.getResourceAsStream("/repositories.example.xml")
    Content.copy(org, new FileOutputStream(temp), true, true)
    val source: ContentResource = new FileResource(temp, Path.root, new EventBus())
    new XmlData(source, new MockUserStore)
  }

  test ("read repos") {
    db.repositories should have size (1)
    db.repositories.get("contentroot") should be (Some(RepositoryModel("contentroot", RepositoryTag.closed)))
  }


  class MockUserStore extends UserStoreAdapter {
    override def findUser(login: String) = Some(new User("test", Map(UserProperty.email->"test@mail.com")))
  }
}
