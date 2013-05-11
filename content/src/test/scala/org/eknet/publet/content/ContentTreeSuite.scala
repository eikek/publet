package org.eknet.publet.content

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.eknet.publet.content.Resource.EmptyContent

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.13 21:28
 */
class ContentTreeSuite extends FunSuite with ShouldMatchers {

  val root = new SimplePartition(Seq(
    EmptyContent("file1.md"),
    EmptyContent("file2.md"),
    EmptyContent("file3.md")
  ))

  val texts = new SimplePartition(Seq(
    EmptyContent("text1.txt"),
    EmptyContent("text2.txt")
  ))
  val movies = new SimplePartition(Seq(
    EmptyContent("movie1.mpg"),
    EmptyContent("movie2.avi")
  ))

  val cr = new ContentTree(root)

  test ("mount partitions") {

    cr.find("file1.md") should be (Some(EmptyContent("file1.md")))

    cr.mount("/", texts)
    cr.find("/file1.md") should be (None)
    cr.find("/text2.txt") should be (Some(EmptyContent("text2.txt")))

    cr.unmount(Path.root)
    cr.find("/file1.md") should be (Some(EmptyContent("file1.md")))

    cr.mount("/main/pages", texts)
    cr.find("/file1.md") should be (Some(EmptyContent("file1.md")))
    cr.find("/main/pages/text1.txt") should be (Some(EmptyContent("text1.txt")))

    cr.mount("/main/pages/movies", movies)
    cr.find("/file1.md") should be (Some(EmptyContent("file1.md")))
    cr.find("/main/pages/text1.txt") should be (Some(EmptyContent("text1.txt")))
    cr.find("/main/pages/movies/movie1.mpg") should be (Some(EmptyContent("movie1.mpg")))

  }

  test ("unmount partitions") {
    cr.mount("/data", texts)
    cr.find("/data/text1.txt") should be (Some(EmptyContent("text1.txt")))
    cr.mount("/data", movies)
    cr.find("/data/text1.txt") should be (None)
    cr.find("/data/movie2.avi") should be (Some(EmptyContent("movie2.avi")))
    cr.unmount("/data")
    cr.find("/data/text1.txt") should be (Some(EmptyContent("text1.txt")))
    cr.find("/data/movie2.avi") should be (None)
  }
}
