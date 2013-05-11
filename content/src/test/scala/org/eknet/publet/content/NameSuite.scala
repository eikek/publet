package org.eknet.publet.content

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.13 17:48
 */
class NameSuite extends FunSuite with ShouldMatchers {

  test ("names creation") {
    Name("a.b").ext should be ("b")
    Name("a.b").base should be ("a")

    Name("hul lah hup.mine").base should be ("hul lah hup")
    Name("hul lah hup.mine").ext should be ("mine")

    Name("hul-la.pdf.gz").base should be ("hul-la.pdf")
    Name("hul-la.pdf.gz").ext should be ("gz")

    Name("somefile").base should be ("somefile")
    Name("somefile").hasExtension should be (false)
  }

  test ("with extension") {
    val name1 = Name("movie.hprof")
    val name2 = name1.withExtension("md")
    name1.base should be (name2.base)
    name1.ext should be ("hprof")
    name2.ext should be ("md")
    name1.contentType should be (None)
    name2.contentType should be (Some(ContentType.`text/x-markdown`))
  }

  test ("content type") {
    Name("test.md").contentType should be (Some(ContentType.`text/x-markdown`))
    Name("test.scala").contentType should be (Some(ContentType.`text/x-scala`))
    Name("test.html").contentType should be (Some(ContentType.`text/html`))
    Name("test.properties").contentType should be (Some(ContentType.`text/plain`))
  }

  test ("full name") {
    Name("file.txt").fullName should be ("file.txt")
    Name("folder").fullName should be ("folder")
  }
}
