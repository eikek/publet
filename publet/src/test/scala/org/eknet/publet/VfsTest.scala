package org.eknet.publet

import vfs.fs.FilesystemPartition
import java.io.File
import vfs.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.04.12 22:55
 */
object VfsTest {


  val publet = Publet()

  def testMount() {
    val fs = new FilesystemPartition(new File("/home/"), false)
//    fs.lookup(Path("/eike")).getOrElse(sys.error("no fs /eike"))

    publet.mountManager.mount(Path("/main/a/b"), fs)
    publet.rootContainer.lookup(Path.root).get
//    println(publet.rootContainer.lookup(Path("/main/eike")).get.asInstanceOf[ContainerResource])
  }


  def main(args: Array[String]) {
    testMount()
  }
}
