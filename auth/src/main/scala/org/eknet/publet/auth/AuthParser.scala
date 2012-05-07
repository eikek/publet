package org.eknet.publet.auth

import util.parsing.combinator.JavaTokenParsers
import java.io.InputStream
import io.Source

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.04.12 22:49
 */
class AuthParser extends JavaTokenParsers {

  private def url: Parser[String] = """[^=]+""".r
  private def filter: Parser[String] = "auth"|"anon"

  private def perm: Parser[String] =
    """[a-zA-Z0-9_\-:\*\.]+""".r

  private def role: Parser[String] = ident

  private def fullName: Parser[String] =
    """[a-zA-Z\s\.]*""".r

  private def email: Parser[String] = """[^:]*""".r

  private def password: Parser[String] = """[^:]*""".r

  private def roles: Parser[List[String]] = repsep(role , ",")
  private def perms: Parser[List[String]] = repsep(perm, ",")


  private def principalParser: Parser[User] = ident~":"~fullName~":"~email~":"~roles~":"~password ^^ {
    case lo~":"~fn~":"~mail~":"~rs~":"~pw => User(lo, fn, mail, rs.toSet, pw.toCharArray)
    case _ => sys.error("Wrong user entry")
  }

  private def permRuleParser: Parser[PermissionRule] = roles~"="~perms ^^ {
    case rs~"="~ps => PermissionRule(rs.toSet, ps.toSet)
    case _ => sys.error("Wrong permission input")
  }

  private def urlMappingParser = url~"="~filter ^^ {
    case u~"="~f => (u.trim, f.trim)
    case _ => sys.error("Wrong url mapping input")
  }

  def parsePrincipal(str: String) = parseAll(principalParser, str)
  def parsePermission(str: String) = parseAll(permRuleParser, str)
  def parseUrlMapping(str: String) = parseAll(urlMappingParser, str)

  private def validLine(s: String) = !s.trim.isEmpty && !s.trim.startsWith("#")

  def principals(in: InputStream) = Source.fromInputStream(in)
    .getLines()
    .filter(validLine)
    .map(parsePrincipal(_).get).toSet

  def permissionRules(in: InputStream) = Source.fromInputStream(in)
    .getLines()
    .filter(validLine)
    .map(parsePermission(_).get).toList

  def urlMappings(in: InputStream) = Source.fromInputStream(in)
    .getLines()
    .filter(validLine)
    .map(parseUrlMapping(_).get).toList

}

case class User(login: String, fullname: String, email: String, roles: Set[String], password: Array[Char], enabled: Boolean = true) {

  login.ensuring(!_.isEmpty, "login is mandatory")
  password.ensuring(!_.isEmpty, "password is mandatory")

  val algorithmPassword = getAlgorithmAndPassword

  def hasRole(role:String): Boolean = {
    if (roles.contains("*")) true
    else roles.contains(role)
  }

  private def getAlgorithmAndPassword: (Option[String], Array[Char]) = {
    val algoPassword = """^\{([A-Z0-9a-z]*)\}(.*)""".r
    new String(password) match {
      case algoPassword("", p) => (None, p.toCharArray)
      case algoPassword(a, p) => (Some(a), p.toCharArray)
      case _ => sys.error("Wrong password input!")
    }
  }
}

case class PermissionRule(roles: Set[String], perms: Set[String])

