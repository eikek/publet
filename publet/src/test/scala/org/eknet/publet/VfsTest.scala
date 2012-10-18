package org.eknet.publet

import _root_.com.google.common.eventbus.EventBus
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
    val fs = new FilesystemPartition(new File("/home/"), new EventBus(), false)
//    fs.lookup(Path("/eike")).getOrElse(sys.error("no fs /eike"))

    publet.mountManager.mount(Path("/main"), fs)

    val p = publet.rootContainer.lookup(Path("/main")).get
    println(p)
  }


  def main(args: Array[String]) {
    testMount()
  }
}
