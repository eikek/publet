package org.eknet.publet.content

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.eknet.publet.content
import java.nio.file.{Path => JPath, Paths}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.13 20:50
 */
class PathSuite extends FunSuite with ShouldMatchers {


  test ("unapply path") {
    Path("/folder1/folder2/file.txt") match {
      case a / as => {
        a should be ("folder1")
        as should be (Path("/folder2/file.txt"))
      }
      case EmptyPath => sys.error("no match")
    }

    Path("/") match {
      case EmptyPath =>
      case _ => sys.error("no match")
    }
  }

  test ("create paths") {

    Path("/") should be (Path.root)
    Path("") should be (Path.root)
    Path(Paths.get("/")) should be (Path.root)

    Path("/a/b/c") should have size (3)

    Path(List("a", "b", "c")) should be (Path("/a/b/c"))

    Path("/a/b/d/../c") should be (Path("/a/b/c"))

  }

  test ("path properties") {
    val ap = Path("/folder1/folder2/test.avi")
    val rp = Path("tmp/folder1/folder2/test.avi")

    ap.size should be (3)
    ap.head should be ("folder1")
    ap.tail should be (Path("/folder2/test.avi"))
    ap.fileName should be (Name("test.avi"))

    rp.size should be (4)
    rp.head should be ("tmp")
    rp.tail should be (ap)
    rp.fileName should be (Name("test.avi"))

    ap / rp should be (Path("/folder1/folder2/test.avi/tmp/folder1/folder2/test.avi"))
    rp / ap should be (Path("tmp/folder1/folder2/test.avi/folder1/folder2/test.avi"))

    ap.drop(2) should be (Path("/test.avi"))
    rp.drop(2) should be (Path("folder2/test.avi"))
  }

  test ("path concat") {
    val jp = Paths.get("/a/b/d/../c")
    val next = Path("z/u")

    (next / jp) should be (Path("z/u/a/b/c"))

    (Path.root / "a/b") should be (Path("/a/b"))
    (Path.root / "ab" / "cd") should be (Path("/ab/cd"))

    (EmptyPath / EmptyPath) should be (EmptyPath)
    (Path("/a/b") / EmptyPath) should be (Path("/a/b"))
  }

  test ("take path") {
    val p = Path("/a/b/c/d/e")
    val p2 = p.take(3)
    p2 should be (Path("/a/b/c"))
  }

  test ("path to string") {
    Path("/a/b/c/d").toString should be ("a/b/c/d")
    Path("/a/b/c/d").absoluteString should be ("/a/b/c/d")
  }

  test ("prepend path") {
    val p1 = Path("/a/b")
    val p2 = Path("/x/y")
    (p1 /:: p2) should be (p2 / p1)
    p2.prepend(p1) should be (p2 / p1)

    ("u" /: p1) should be (Path("/u/a/b"))

    ("a" /: "b" /: "c" /: EmptyPath) should be (Path("/a/b/c"))
  }
}
