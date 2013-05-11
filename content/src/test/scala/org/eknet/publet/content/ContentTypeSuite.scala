package org.eknet.publet.content

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import org.eknet.publet.content.ContentType.CustomContentType

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.13 19:03
 */
class ContentTypeSuite extends FunSuite with ShouldMatchers {

  test ("register custom type") {
    val mpeg = CustomContentType("video", "mpeg", Set("mpg", "mpeg", "mpe"), true)
    ContentType.all should have size(18)

    ContentType.register(mpeg)
    ContentType.all should have size(19)
    ContentType.get("video" -> "mpeg") should be (Some(mpeg))
    ContentType.findByExt("mpg") should be (Some(mpeg))

    intercept[RuntimeException] {
      val other = CustomContentType("video", "avi", Set("avi", "mpeg"), true)
      ContentType.register(other)
    }
  }

  test ("get listed types") {
    ContentType.get("text" -> "plain") should be (Some(ContentType.`text/plain`))
    ContentType.get("text" -> "html") should be (Some(ContentType.`text/html`))
    ContentType.get("text" -> "x-scala") should be (Some(ContentType.`text/x-scala`))
    ContentType.get("text" -> "x-markdown") should be (Some(ContentType.`text/x-markdown`))
    ContentType.get("application" -> "pdf") should be (Some(ContentType.`application/pdf`))
  }

  test ("get for extension") {
    ContentType.findByExt("gz") should be (None)

    ContentType.findByExt("pdf") should be (Some(ContentType.`application/pdf`))
    ContentType.findByExt("css") should be (Some(ContentType.`text/css`))
    ContentType.findByExt("scala") should be (Some(ContentType.`text/x-scala`))
    ContentType.findByExt("markdown") should be (Some(ContentType.`text/x-markdown`))
    ContentType.findByExt("md") should be (Some(ContentType.`text/x-markdown`))
    ContentType.findByExt("mdown") should be (Some(ContentType.`text/x-markdown`))
  }

  test ("pattern match") {
    ContentType.findByExt("pdf").get match {
      case ContentType(mainType, subType, binary) => {
        mainType should be ("application")
        subType should be ("pdf")
        binary should be (true)
      }
      case _ => sys.error("no match")
    }
  }
}
