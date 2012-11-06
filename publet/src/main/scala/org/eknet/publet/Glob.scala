/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet

import org.eknet.publet.Glob.{Token, Lexer}

/**
 * Simple ant-style glob impl for matching paths. It only supports the following three
 *
 * * `*` matches any chars up to the next path separator or string
 * * `?` matches excatly one char
 * * `**` like `*` but crosses path boundaries
 *
 * It implements [[scala.Ordered]] by comparing the pattern length.
 *
 * @param pattern
 */
final case class Glob(pattern: String) extends Ordered[Glob] {

  private val separatorString = "/"
  private val lexed = new Lexer(separatorString).split(pattern)

  /**
   * Matches the string against the pattern of this glob.
   *
   * Returns `false` if it doesn't match and `true` if it does.
   *
   * @param str
   * @return
   */
  def matches(str: String): Boolean = {
    val result = consume(str, lexed)
    result.fold(part => false, str => true)
  }

  /**
   * Matches the given string against the pattern of this glob.
   *
   * Never returns `false`, but throws an exception with additional
   * error information instead. On success, it returns `true`.
   *
   * @param str
   * @throws org.eknet.publet.GlobParseException
   * @return
   */
  @throws(classOf[GlobParseException])
  def consume(str: String): Boolean = {
    val result = consume(str, lexed)
    result.fold(p => throw new GlobParseException(str, str.length-p.length), s=>true)
  }

  def compare(that: Glob) = pattern.length.compare(that.pattern.length) match {
    case 0 => pattern.compare(that.pattern)
    case x => x
  }

  private def consume(str: String, tokens: List[Token]): Either[String, String] = {
    tokens match {
      case Nil => if (str.isEmpty) Right("") else Left(str)
      case c::cs => {
        val rem = c.consume(str, cs)
        rem.fold(fa=>Left(fa), t => consume(t._1, t._2))
      }
    }
  }

}

object Glob {

  implicit def stringToGlob(str: String) = Glob(str)

  private abstract class Token(val name: String) {
    def :: (tok: Token): List[Token]
    def reverse: Token

    /**
     * Consumes the string by matching characters
     * from the beginning of the string against this token.
     *
     * On error, it returns the remaining part of the
     * string, on success it returns the next string and token
     * list.
     *
     * @param str
     * @param next
     * @return
     */
    def consume(str: String, next:List[Token]): Either[String, (String, List[Token])] = {
      if (str.length < name.length)
        return Left(str)
      else {
        for (i <- 0 to name.length-1) {
          if (str(i) != name(i)) return Left(str.substring(i))
        }
      }
      Right(str.substring(name.length), next)
    }

  }

  private def createToken(name: String, sep: String): Token = name match {
    case "**" => KleeneStar2(sep)
    case "*" => KleeneStar(sep)
    case "?" => OneChar
    case `sep` => Separator(sep)
    case _ => Word(name)
  }

  /** A plain string without wildcards */
  private case class Word(override val name:String) extends Token(name) {
    def :: (tok: Token): List[Token] = tok match {
      case Word(_) => List(Word(this.name+tok.name))
      case _ => List(tok, this)
    }
    def reverse:Token = Word(name.reverse)
  }

  /** The `?` wildcard */
  private object OneChar extends Token("?") {
    def :: (tok: Token): List[Token] = List(tok, this)
    def reverse = this

    override def consume(str: String, next: List[Token]) = {
      if (str.length > 0) Right(str.substring(1), next)
      else Left(str)
    }

    override def toString = "OneChar(?)"
  }

  /** The `*` wildcard. */
  private case class KleeneStar(sep: String) extends Token("*") {
    override def :: (tok:Token):List[Token] = tok match {
      case KleeneStar(`sep`) => List(KleeneStar2(sep))
      case _ => List(tok, this)
    }

    def reverse = this
    override def consume(str: String, next: List[Token]) = {
      val nextString = { //either up to the next path separator if avail, or next token string
        if (str.indexOf(sep) > 0) sep
        else next.headOption.map(_.name).getOrElse(sep)
      }
      val n = str.indexOf(nextString)
      if (n > 0) Right(str.substring(n), next)
      else Right("", next)
    }
    override def toString = "KleeneStar(*)"
  }

  /** The `**` wildcard */
  private case class KleeneStar2(sep: String) extends Token("**") {
    def :: (tok: Token): List[Token] = List(tok, this)
    def reverse = this

    override def consume(str: String, next: List[Token]) = {
      next match {
        case Nil => Right("", next)
        case c::cs => {
          if (c == Separator(sep)) consume(str, cs)
          else {
            val ind = str.indexOf(c.name)
            if (ind > 0) Right(str.substring(ind), next)
            else Right("", next)
          }
        }
      }
    }
    override def toString = "KleeneStar2(**)"
  }

  private case class Separator(separatorString: String) extends Token(separatorString) {
    def :: (tok: Token): List[Token] = List(tok, this)
    def reverse = this
  }

  /** Splits the pattern into a list of tokens. */
  private class Lexer(separatorString: String) {

    def split(pattern: String) = {
      pattern.reverse.map(c => token(c.toString))
        .foldLeft(List[Token]())((list, t) => prepend(list, t))
        .map(_.reverse)
    }

    private def token(name: String) = createToken(name, separatorString)

    private def prepend(list: List[Token], token: Token): List[Token] = {
      list match {
        case c::cs => (token :: c) ::: cs
        case Nil => List(token)
      }
    }
  }
}

class GlobParseException(val str: String, val offset: Int) extends Exception("\n"+str+"\n"+((" "*offset))+ "^")