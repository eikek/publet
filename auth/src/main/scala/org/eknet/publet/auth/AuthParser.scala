package org.eknet.publet.auth

import util.parsing.combinator.JavaTokenParsers
import java.io.InputStream
import io.Source

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 22:49
 */
class AuthParser extends JavaTokenParsers {

  private def urlP: Parser[String] = //("(\"|')"+ """[^:]+"""  +"(\"|')").r
    ("(\"|')" + """([^"\p{Cntrl}\\]|\\[\\/bfnrt]|\\u[a-fA-F0-9]{4})*""" + "(\"|')").r

  private def url = urlP ^^ (u => u.substring(1, u.length - 1))


  def principal: Parser[(String, List[String], String)] = ident ~ ":" ~ roles ~ ":" ~ ident ^^ {
    case p ~ ":" ~ r ~ ":" ~ w => (p, r, w)
  }

  def rule: Parser[(String, List[String], List[String])] = url ~ ":" ~ roles ~ ":" ~ perms ^^ {
    case u ~ ":" ~ r ~ ":" ~ p => (u, r, p)
  }

  def roles: Parser[List[String]] = repsep(ident, ",")

  def perms: Parser[List[String]] = repsep(ident, ",")

  def parsePrincipal(str: String) = parseAll(principal, str)

  def parsePermission(str: String) = parseAll(rule, str)

  def principal(str: String): User = {
    val r = parsePrincipal(str).get
    User(r._1, r._2.toSet, r._3.toCharArray)
  }

  def rule(str: String): Rule = {
    val r = parsePermission(str).get
    Rule(r._1, r._2.toSet, r._3.toSet)
  }

  private def validLine(s: String) = !s.trim.isEmpty && !s.trim.startsWith("#")

  def principals(in: InputStream) = Source.fromInputStream(in)
    .getLines()
    .filter(validLine)
    .map(principal(_)).toSet

  def rules(in: InputStream): List[Rule] = Source.fromInputStream(in)
    .getLines()
    .filter(validLine)
    .map(rule(_)).toList
}

case class User(username: String, roles: Set[String], password: Array[Char], enabled: Boolean = true)

case class Rule(resource: String, roles: Set[String], permissions: Set[String])
