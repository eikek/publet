package org.eknet.publet.auth

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.eknet.publet.vfs.{ResourceName, ContentType, Content}
import org.eknet.publet.vfs.util.SimpleContentResource
import xml.XmlDatabase

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
    new XmlDatabase(source)
  }

  private val policy = db.getPolicy("jdoe")

  test ("user list") {
    db.getAllUser should equal(Seq(User("jdoe",
      "098f6bcd4621d373cade4e832627b4f6".toCharArray,
      Some("md5"),
      "efd".toCharArray,
      Set("wikiuser", "editor"),
      Map("fullName" -> "John Doe",
          "email" -> "jdoe@mail.com")))
    )
  }

  test ("repository owner permission") {
    policy.getPermissions should equal (
      Set("pull:wikis/mywiki",
        "pull:contentroot",
        "push:wikis/mywiki",
        "push:jdoe/dotfiles",
        "gitadmin:wikis/mywiki",
        "gitadmin:jdoe/dotfiles")
    )
  }

}
