package org.eknet.publet.web.extensions

import org.eknet.publet.resource.{ContentType, Content}
import xml.{NodeBuffer, Elem, NodeSeq}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 21:32
 */
object WebDsl {

  abstract class WebElem {
    def render: String
    def ~ (elem: WebElem): WebElem = new PairElem(this, elem)
  }
  class PairElem(e1: WebElem, e2: WebElem) extends WebElem {
    def render = e1.render + e2.render
  }
  class StringElem(str: String) extends WebElem {
    def render = str
  }
  case class Head(str:String, level: Int = 1) extends WebElem {
    lazy val render = "<h"+ level+ ">"+str+"</h"+ level +">"
  }

  implicit def stringToWebElem(str: String): WebElem = new StringElem(str)


  // forms
  class FormElem(nodes: => NodeSeq, onSubmit: => Content, validate: PartialFunction[Param, Option[String]] = { case _ => None }) extends WebElem {
    nodes.head.label.ensuring(_ == "form", "only applicable to form elements")

    //TODO
    // FormElem must check the nodes and search for any input tag with "name" attribute
    // if the request does not contain at least one of these parameter, then render the nodes. make
    // sure to replace the "action=" attribute with the current request url (or better with $this, which is replaced by javascript
    // to make it also work when included.
    //
    // if parameters from the form are present, then feed them in the validate() partial function
    // any results must be rendered next to the form (think about how.. maybe add additional labels with error css class)
    // if validate does not fail, call onSubmit (which can return "this" or another page)

    def render = null
  }
  case class Param(name:String, value:String)

  def form(render: => NodeSeq, onSubmit: => Content, validate: PartialFunction[Param, Option[String]] = { case _ => None }): WebElem = {
    new FormElem(render, onSubmit, validate)
  }

  class NodeSeqElem(ns: NodeSeq) extends WebElem {
    def action(onSubmit: => Content, validate: PartialFunction[Param, Option[String]] = { case _ => None }): WebElem = {
      form(ns, onSubmit, validate)
    }

    def render = ns.toString()
  }

  implicit def elemToWebElem(xml: Elem): WebElem = new NodeSeqElem(xml)
  implicit def nodeseqToElem(ns: NodeSeq) = new NodeSeqElem(ns)
  implicit def nodeBufferToElem(ns: NodeBuffer) = new NodeSeqElem(ns)


  // create content from an webelem
  def page(elem: WebElem): Content = Content(elem.render, ContentType.html)

  // want to build pages like this

  import org.eknet.publet.engine.scalascript.ScalaScript._
  import MailSupport._

  page {
    <h1>This is my headline</h1> ~
    form(
      // gives the xhtml content.
      render = <form>
          <input type="text" name="username"></input>
          <input type="button" value="Send"></input>
        </form>,

      //is called after form submit, that is a request to the current url with parameters
      onSubmit = {
        newMail("me@you.com").to("you@me.come").subject("my mail").text("this is text").send()
        makeHtml("<p>yes</p>")
      },

      //optional partial function that is applied to the params before onsubmit
      validate = {
        case Param("username", un)  => if (un.isEmpty) Some("empty username") else None
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
        newMail("me@you.com").to("you@me.come").subject("my mail").text("this is text").send()
        makeHtml("<p>yes</p>")
      },

      //optional partial function that is applied to the params before onsubmit
      validate = {
        case Param("username", un)  => if (un.isEmpty) Some("empty username") else None
      }
    ) ~
    <p>Pleas fill the form</p>
  }
}

