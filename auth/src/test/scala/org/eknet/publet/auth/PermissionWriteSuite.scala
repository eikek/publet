package org.eknet.publet.auth

import org.scalatest.{OneInstancePerTest, FunSuite}
import org.scalatest.matchers.ShouldMatchers
import java.io.{FileOutputStream, File}
import org.eknet.publet.vfs.{ContentResource, Path, Content}
import org.eknet.publet.vfs.fs.FileResource
import xml.XmlDatabase
import com.google.common.eventbus.EventBus
import org.eknet.publet.auth.repository.{RepositoryModel, RepositoryTag}

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
    val source: ContentResource = new FileResource(temp, Path.root, new EventBus())
    new XmlDatabase(source, new DefaultPasswordServiceProvider, None)
  }


  ignore ("Update RepositoryModel") {
    val rm = db.findRepository("jdoe/dotfiles").get
    rm.owner should equal ("jdoe")
    rm.tag should equal (RepositoryTag.closed)

    val nrm = RepositoryModel("jdoe/dotfiles", RepositoryTag.open, "jdoe")
    db.updateRepository(nrm)

    val load = db.findRepository("jdoe/dotfiles").get
    load.owner should equal ("jdoe")
    load.tag should equal (RepositoryTag.open)
  }

  ignore ("Remove Permission") {
    db.getPermissions("jdoe") should contain ("pull:contentroot")
    db.dropPermission("jdoe", Permission.forGit(Set("pull"), Set("contentroot")).toString)
    db.getPermissions("jdoe") should not contain ("pull:contentroot")
  }
}
