package org.eknet.publet.web

import org.eknet.publet.vfs.{ContentType, Content}
import xml.{NodeBuffer, Elem, NodeSeq}
import org.eknet.publet.web.util.Key

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 21:32
 */
object WebDsl {

  abstract class WebElem {
    def render: String

    def ~(elem: WebElem): WebElem = new PairElem(this, elem)

    def action(onSubmit: => Option[Content], validate: PartialFunction[Param, Option[String]] = {
      case _ => None
    }): WebElem = {
      form(this, onSubmit, validate)
    }

    def when(cond: => Boolean) = new ConditionElem(this, cond)
  }

  class ConditionElem(el: WebElem, cond: => Boolean) extends WebElem {
    def render = if (cond) el.render else ""
  }

  class PairElem(e1: WebElem, e2: WebElem) extends WebElem {
    def render = e1.render + e2.render
  }

  class StringElem(str: String) extends WebElem {
    def render = str
  }

  implicit def stringToWebElem(str: String): WebElem = new StringElem(str)

  def when(cond: => Boolean)(elem: WebElem) = new ConditionElem(elem, cond)

  // forms
  class FormElem(elm: WebElem, onSubmit: => Option[Content], validate: PartialFunction[Param, Option[String]] = {
    case _ => None
  }) extends WebElem {

    //TODO
    //
    // if parameters from the form are present, then feed them in the validate() partial function
    // any results must be put into the request's attribute map. the render method must react on
    // those information accordingly. if validate does not fail, call onSubmit (which can return
    // "this" or another page)

    def render = null
  }

  case class Param(name: String, value: String)

  def form(markup: WebElem, onSubmit: => Option[Content], validate: PartialFunction[Param, Option[String]] = {
    case _ => None
  }): WebElem = {
    new FormElem(markup, onSubmit, validate)
  }

  class NodeSeqElem(ns: NodeSeq) extends WebElem {

    def render = ns.toString()
  }

  implicit def elemToWebElem(xml: Elem): WebElem = new NodeSeqElem(xml)

  implicit def nodeseqToElem(ns: NodeSeq) = new NodeSeqElem(ns)

  implicit def nodeBufferToElem(ns: NodeBuffer) = new NodeSeqElem(ns)


  // create content from an webelem
  def page(elem: WebElem): Content = Content(elem.render, ContentType.html)

  // want to build pages like this

  import org.eknet.publet.engine.scala.ScalaScript._

  val successKey = Key[Boolean]("success")
  val ctx = WebContext()

  page {
    <h1>This is my headline</h1> ~
      form(
        // gives the xhtml content.
        markup = <h2>Kontakt</h2> ~
          <p class="box success"></p>.when(ctx(successKey).exists(_ == true)) ~
          when(ctx(successKey).isEmpty) {
            """<form class="ym-form linearize-form" action={ctx(WebContext.requestUrl)}>
              <input type="hidden" name="a" value="eval"/>
              <div class="ym-fbox-text">
                <label for="from">Von (Email)<sup class="ym-required">*</sup></label>
                  <input type="text" name="from" id="from" size="20" required="required"/>
              </div>
              <div class="ym-fbox-text">
                <label for="message">Nachricht<sup class="ym-required">*</sup></label>
                <textarea name="message" rows="10" required="required"></textarea>
              </div>
              <div class="ym-fbox-text">
                <label for="captcha">Captcha<sup class="ym-required">*</sup></label>
                  <input type="text" name="captcha" id="captcha" size="20" required="required"/>
              </div>
              <div class="ym-fbox-select">
                <img alt="captcha" src="/.publets/scripts/captcha.png" />
              </div>
              <button class="ym-button ym-email publetAjaxSubmit">Senden</button>
            </form>"""
          },

        //is called after form submit, that is a request to the current url with parameters
        onSubmit = {
//          newMail("me@you.com").to("you@me.come").subject("my mail").text("this is text").send()
          makeHtml("<p>yes</p>")
        },

        //optional partial function that is applied to the params before onsubmit
        validate = {
          case Param("username", un) => if (un.isEmpty) Some("empty username") else None
        }
      ) ~
      <p>Please fill this form</p>
  }

  // or this way
  page {
    <h1>This is the headline</h1>
      <form>
        <input type="text" name="username"></input>
        <input type="button" value="Send"></input>
      </form>.action(
      //is called after form submit, that is a request to the current url with parameters
      onSubmit = {
//        newMail("me@you.com").to("you@me.come").subject("my mail").text("this is text").send()
        makeHtml("<p>yes</p>")
      },

      //optional partial function that is applied to the params before onsubmit
      validate = {
        case Param("username", un) => if (un.isEmpty) Some("empty username") else None
      }
    )
  }
}

