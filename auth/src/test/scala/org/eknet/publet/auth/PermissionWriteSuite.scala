package org.eknet.publet.auth

import org.scalatest.{OneInstancePerTest, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import java.io.{FileOutputStream, File}
import org.eknet.publet.vfs.{Path, Content}
import org.eknet.publet.vfs.fs.FileResource
import xml.XmlDatabase
import com.google.common.eventbus.EventBus

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.06.12 18:14
 */
class PermissionWriteSuite extends FunSuite with ShouldMatchers with OneInstancePerTest {

  val db = {
    val temp = File.createTempFile("perm.db", ".xml")
    temp.deleteOnExit()
    val org = getClass.getResourceAsStream("/permission.example.xml")
    Content.copy(org, new FileOutputStream(temp), true, true)
    val source = new FileResource(temp, Path.root, new EventBus())
    new XmlDatabase(source, new DefaultPasswordServiceProvider, None)
  }

  test ("Update RepositoryModel") {
    val rm = db.getRepository("jdoe/dotfiles")
    rm.owner should equal ("jdoe")
    rm.tag should equal (RepositoryTag.closed)

    val nrm = RepositoryModel("jdoe/dotfiles", RepositoryTag.open, "jdoe")
    db.updateRepository(nrm)

    val load = db.getRepository("jdoe/dotfiles")
    load.owner should equal ("jdoe")
    load.tag should equal (RepositoryTag.open)
  }

  test ("Remove Permission") {
    db.getPolicy("jdoe").getPermissions should contain ("pull:contentroot")
    db.removePermission("editor", Permission("pull", Some("contentroot")))
    db.getPolicy("jdoe").getPermissions should not contain ("pull:contentroot")
  }
}
