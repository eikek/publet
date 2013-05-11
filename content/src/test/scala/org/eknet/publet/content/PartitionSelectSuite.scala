package org.eknet.publet.content

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import java.nio.file.Files
import org.eknet.publet.content.Resource.EmptyContent

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.13 23:24
 */
class PartitionSelectSuite extends FunSuite with ShouldMatchers {

  val p = new FsPartition(Files.createTempDirectory("partition"))
  p.createContent("/main/pages", EmptyContent("file1.md"), ModifyInfo.none)
  p.createContent("/main/pages", EmptyContent("file2.md"), ModifyInfo.none)
  p.createContent("/main/pages", EmptyContent("file3.pdf"), ModifyInfo.none)
  p.createContent("/main/pages", EmptyContent("file4.html"), ModifyInfo.none)
  p.createContent("/second/pages", EmptyContent("file5.html"), ModifyInfo.none)
  p.createContent("/second/pages", EmptyContent("file6.md"), ModifyInfo.none)
  p.createContent("/second/pages", EmptyContent("file7.pdf"), ModifyInfo.none)
  p.createContent("/main/assets", EmptyContent("file8.css"), ModifyInfo.none)
  p.createContent("/main/assets", EmptyContent("file9.css"), ModifyInfo.none)
  p.createContent("/main/assets", EmptyContent("file10.js"), ModifyInfo.none)
  p.createContent("/main/assets", EmptyContent("file11.pdf"), ModifyInfo.none)
  p.createContent("/main/assets", EmptyContent("file12.pdf"), ModifyInfo.none)

  test ("select on FsPartition") {
    val r = p.select("/main/pages/*.md")
    r should have size (2)
    r.map(_._1) should be (Set("/main/pages/file1.md", "/main/pages/file2.md").map(Path(_)))

    val r2 = p.select("/main/*/*.pdf")
    r2 should have size (3)
    r2.map(_._1) should be (Set(
      "/main/pages/file3.pdf",
      "/main/assets/file12.pdf",
      "/main/assets/file11.pdf").map(Path(_)))

    val r3 = p.select("*/pages/file?.html")
    r3 should have size (2)
    r3.map(_._1) should be (Set(
      "/main/pages/file4.html",
      "/second/pages/file5.html").map(Path(_)))

    import FsPath._
    p.directory.deleteDirectory()
  }

  test ("select on SimplePartition") {
    val part = new SimplePartition(Seq(
      EmptyContent("test.md"),
      EmptyContent("test.ssp"),
      EmptyContent("test.txt"),
      EmptyContent("file1.pdf"),
      EmptyContent("file2.png")
    ))

    part.select("test.*") should have size (3)
    part.select("test.*").map(_._2.name.fullName).toSet should be (Set("test.md", "test.ssp", "test.txt"))
  }
}
