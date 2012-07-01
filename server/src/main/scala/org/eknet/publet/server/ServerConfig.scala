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

package org.eknet.publet.server

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.07.12 16:35
 */
trait ServerConfig {

  /**
   * The port for the standard http connector. If not there, no connector
   * is created.
   *
   * @return
   */
  def port: Option[Int]

  /**
   * The port for the secure connector.
   *
   * @return
   */
  def securePort: Option[Int]

  /**
   * The path to the keystore.
   *
   * @return
   */
  def keystorePath: String

  /**
   * The password for the keystore.
   *
   * @return
   */
  def keystorePassword: String

  /**
   * The port for receiving shutdown request
   *
   * Default is `8099`
   *
   *@return
   */
  def shutdownPort: Int
}

object ServerConfig {
  val propertyPort = "publet.server.port"
  val propertySecurePort = "publet.server.securePort"
  val propertyKeystorePath = "publet.server.keystorePath"
  val propertyKeystorePassword = "publet.server.keystorePassword"
  val propertyShutdownPort = "publet.server.shutdownPort"
}

class DefaultConfig extends ServerConfig {
  def port:Option[Int] = None
  def securePort:Option[Int] = None
  def keystorePath:String = ""
  def keystorePassword:String = ""
  def shutdownPort:Int = 8099
}

/**
 * Gets the config properties from the system properties.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.07.12 16:37
 */
trait SyspropConfig extends ServerConfig {

  import ServerConfig._

  abstract override def port = syspropInt(propertyPort) orElse (super.port)
  abstract override def securePort = syspropInt(propertySecurePort) orElse (super.securePort)
  abstract override def keystorePath = sysprop(propertyKeystorePath) getOrElse (super.keystorePath)
  abstract override def keystorePassword = sysprop(propertyKeystorePassword) getOrElse (super.keystorePassword)
  abstract override def shutdownPort = syspropInt(propertyShutdownPort) getOrElse (super.shutdownPort)

  private def sysprop(name: String): Option[String] = Option(System.getProperty(name))
  private def syspropInt(name: String): Option[Int] = sysprop(name).map(_.toInt)

}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.07.12 17:41
 */
trait PropertiesConfig extends ServerConfig {
  import java.util
  def props: util.Properties

  private def property(name: String) = Option(props.getProperty(name))
  private def intProperty(name: String) = property(name) map (_.toInt)

  import ServerConfig._

  abstract override def port = intProperty(propertyPort) orElse (super.port)
  abstract override def securePort = intProperty(propertySecurePort) orElse (super.securePort)
  abstract override def keystorePath = property(propertyKeystorePath) getOrElse(super.keystorePath)
  abstract override def keystorePassword = property(propertyKeystorePassword) getOrElse(super.keystorePassword)
  abstract override def shutdownPort:Int = intProperty(propertyShutdownPort) getOrElse(super.shutdownPort)
}