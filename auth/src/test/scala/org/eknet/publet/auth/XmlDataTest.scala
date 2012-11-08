package org.eknet.publet.auth

import org.scalatest.{BeforeAndAfter, OneInstancePerTest, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import java.io.{FileOutputStream, File}
import org.eknet.publet.vfs.{ContentResource, Path, Content}
import org.eknet.publet.vfs.fs.FileResource
import org.eknet.publet.auth.xml.{XmlData, XmlDatabase}
import com.google.common.eventbus.EventBus
import org.eknet.publet.auth.store.{User, UserProperty, ResourcePatternDef}
import org.eknet.publet.Glob
import org.apache.shiro.mgt.DefaultSecurityManager

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 18:14
 */
class XmlDataTest extends FunSuite with ShouldMatchers with OneInstancePerTest with BeforeAndAfter with SecurityManagerMock {

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

  test ("update user") {
    db.modify { data =>
      data.users = data.users + ("jack" -> new User("jack",
        Map(UserProperty.password -> "hallo",
            UserProperty.fullName -> "Jack White")))
    }

    db.users should have size (2)

    val xmldb2 = new XmlData(db.source)
    xmldb2.users should have size (2)
    val jack = xmldb2.users.get("jack").get
    jack.get(UserProperty.fullName) should be (Some("Jack White"))
    jack.get(UserProperty.password) should be (Some("hallo"))
    jack.get(UserProperty.algorithm) should be (None)
    jack.get(UserProperty.digest) should be (None)
    jack.get(UserProperty.email) should be (None)

  }

  test ("update group") {
    db.modify { data =>
      val g = data.groups.get("jdoe").getOrElse(Set[String]())
      data.groups = data.groups + ("jdoe" -> (g + "manager"))
    }

    db.groups should have size (1)
    db.groups.get("jdoe") should be (Some(Set("wikiuser", "editor", "manager")))

    val xmldb2 = new XmlData(db.source)
    xmldb2.groups should have size (1)
    xmldb2.groups.get("jdoe") should be (Some(Set("wikiuser", "editor", "manager")))
  }

  test ("update permission") {
    db.modify { data =>
      val set = data.permissions.get("editor").get
      data.permissions = data.permissions + ("editor" -> (set + "git:pull:projectx"))
    }

    val expected = Set("resource:*:/devel/projectb/**", "git:push,pull:projectb", "git:pull:projectx")
    db.permissions.get("editor") should be (Some(expected))

    val xmldb2 = new XmlData(db.source)
    xmldb2.permissions.get("editor") should be (Some(expected))
  }

  test ("update patterns") {
    db.modify { data =>
      data.anonPatterns = "/sic/public/**" :: data.anonPatterns
      data.restricted = ResourcePatternDef("/dev/mods/**", "modperm", ResourceAction.write) :: data.restricted
    }

    db.anonPatterns should have size (2)
    db.restrictedPatterns should have size (3)

    val xmldb2 = new XmlData(db.source)
    xmldb2.anonPatterns should contain ("/sic/public/**")
    xmldb2.restrictedPatterns should contain (ResourcePatternDef("/dev/mods/**", "modperm", ResourceAction.write))
  }
}