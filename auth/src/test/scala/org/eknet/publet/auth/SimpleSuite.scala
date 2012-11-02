package org.eknet.publet.auth

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.eknet.publet.vfs.{ResourceName, ContentType, Content}
import org.eknet.publet.vfs.util.SimpleContentResource
import xml.XmlDatabase
import org.eknet.publet.auth.user.User

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 15:48
 */
class SimpleSuite extends FunSuite with ShouldMatchers {

  private val db = {
    val content = new Content {
      def contentType = ContentType.xml
      def inputStream = getClass.getResourceAsStream("/permission.example.xml")
      override def lastModification = Some(System.currentTimeMillis())
    }
    val source = new SimpleContentResource(ResourceName("permission.example.xml"), content)
    new XmlDatabase(source, new DefaultPasswordServiceProvider, None)
  }

  test ("user list") {
    import org.eknet.publet.auth.user.UserProperty._

    val user = User("jdoe", Map(fullName -> "John Doe",
        password -> "098f6bcd4621d373cade4e832627b4f6",
        email -> "jdoe@mail.com",
        algorithm -> "md5",
        digest -> "efd"))

    db.getGroups("jdoe") should be (Set("wikiuser", "editor"))

    db.allUser should have size (1)
    db.allUser.head should be (user)

  }

  test ("get permissions") {
    db.getPermissions("jdoe") should equal (
      Set("resource:*:/devel/projectb/**",
        "git:push,pull:projectb")
    )
  }

}
