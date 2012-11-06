package org.eknet.publet.auth

import org.scalatest.{OneInstancePerTest, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import java.io.{FileOutputStream, File}
import org.eknet.publet.vfs.{ContentResource, Path, Content}
import org.eknet.publet.vfs.fs.FileResource
import org.eknet.publet.auth.xml.{XmlData, XmlDatabase}
import com.google.common.eventbus.EventBus
import org.eknet.publet.auth.store.{UserProperty, ResourcePatternDef}
import org.eknet.publet.Glob

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 18:14
 */
class XmlDataTest extends FunSuite with ShouldMatchers with OneInstancePerTest {

  val db = {
    val temp = File.createTempFile("perm.db", ".xml")
    temp.deleteOnExit()
    val org = getClass.getResourceAsStream("/permission.example.xml")
    Content.copy(org, new FileOutputStream(temp), true, true)
    val source: ContentResource = new FileResource(temp, Path.root, new EventBus())
    new XmlData(source)
  }

  test ("read anon patterns") {
    db.anonPatterns should have size (1)
    db.anonPatterns.head should be ("/public/**")
    println(db.toXml)
  }

  test ("read restricted patterns") {
    val restricted = db.restrictedPatterns
    restricted should have size (2)

    restricted.head should be (ResourcePatternDef("/dev/aa/project/**"))
    restricted(1) should be (ResourcePatternDef("/dev/bb/**", "secperm", ResourceAction.write))
  }

  test ("read permissions") {
    val perms = db.permissions
    val expected = Set("resource:*:/devel/projectb/**", "git:push,pull:projectb")

    perms should have size (3)
    perms.get("manager") should be (Some(expected))
    perms.get("editor") should be (Some(expected))
    perms.get("coders") should be (Some(expected))
  }

  test ("read groups") {
    val groups = db.groups

    groups should have size (1)
    groups.get("jdoe") should be (Some(Set("wikiuser", "editor")))
  }

  test ("read user") {
    val users = db.users
    users should have size (1)

    val jdoe = users.get("jdoe").get
    jdoe.get(UserProperty.fullName) should be (Some("John Doe"))
    jdoe.get(UserProperty.email) should be (Some("jdoe@mail.com"))
    jdoe.get(UserProperty.algorithm) should be (Some("md5"))
    jdoe.get(UserProperty.digest) should be (Some("efd"))
    jdoe.get(UserProperty.enabled) should be (None)
    jdoe.isEnabled should be (true)
    jdoe.get(UserProperty.password) should be (Some("098f6bcd4621d373cade4e832627b4f6"))
  }
}
