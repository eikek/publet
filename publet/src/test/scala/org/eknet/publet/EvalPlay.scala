package org.eknet.publet

import engine.scalascript.com.twitter.util.Eval

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.04.12 18:34
 */
object EvalPlay {

  def main(args: Array[String]) {
    val eval = new Eval(None)
    val mailfunction = "does not compile"
    val ok = "println(\"test\")"

    try {
      println("FIRST")
      eval.apply(mailfunction, false)
    } catch {
      case e: Throwable => e.printStackTrace()
    }

    try {
      println("SECOND")
      eval.apply(ok, false)
    }
    catch {
      case e: Throwable => e.printStackTrace()
    }

  }
}
