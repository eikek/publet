package org.eknet.publet.auth

import scala.xml.PrettyPrinter
import org.eknet.publet.vfs.util.SimpleContentResource
import org.eknet.publet.vfs.{ContentType, Content, ResourceName}
import xml.XmlDatabase


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 12:31
 */
object XmlParserTest {

  def main(args: Array[String]) {
    val content = new Content {
      def contentType = ContentType.xml
      def inputStream = getClass.getResourceAsStream("/permission.example.xml")
      override def lastModification = Some(System.currentTimeMillis())
    }
    val source = new SimpleContentResource(ResourceName("permission.example.xml"), content)
    val pa = new XmlDatabase(source)
    val pp = new PrettyPrinter(90, 2)
    val policy = pa.getPolicy("jdoe")
    println(policy.getPermissions)
    println(policy.getRoles)
    println(pa.getAllRepositories)
    println(pa.getPolicy("jdoe"))
    println("---------------")
    println(pp.format(pa.findUser("jdoe").get.toXml))
  }
}
