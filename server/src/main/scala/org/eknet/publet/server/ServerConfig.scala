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
   * The port for the AJP connector. If not specified, no
   * ajp connector is started.
   *
   * @return
   */
  def ajpPort: Option[Int]

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

  /**
   * The address to bind the ssl connector to. Default is 0.0.0.0
   * @return
   */
  def sslBindAddress: Option[String]

  /**
   * The address to bind the http connector to. Default is 0.0.0.0
   *
   * @return
   */
  def bindAddress: Option[String]

  /**
   * The context path to deploy the publet webapp. Default is the root
   * path `/`
   *
   * @return
   */
  def contextPath: String

  /**
   * The working directory for the server instance. Defaults to
   * the current working directory. This directory is expected
   * to contain the `etc` and `webapp` directory and is also
   * used to save log files and other data.
   *
   * @return
   */
  def workingDirectory: String

}

object ServerConfig {
  val propertyPort = "publet.server.port"
  val propertySecurePort = "publet.server.securePort"
  val propertyKeystorePath = "publet.server.keystorePath"
  val propertyKeystorePassword = "publet.server.keystorePassword"
  val propertyShutdownPort = "publet.server.shutdownPort"
  val propertyAjpPort = "publet.server.ajpPort"
  val propertyBindAddress = "publet.server.bindAddress"
  val propertySslBindAddress = "publet.server.secureBindAddress"
  val propertyContextPath = "publet.server.contextPath"
  val propertyWorkingDirectory = "publet.server.workingDirectory"
}

class DefaultConfig extends ServerConfig {
  def port:Option[Int] = None
  def securePort:Option[Int] = None
  def keystorePath:String = ""
  def keystorePassword:String = ""
  def shutdownPort:Int = 8099
  def ajpPort:Option[Int] = None
  def sslBindAddress:Option[String] = None
  def bindAddress:Option[String] = None
  def contextPath = "/"
  def workingDirectory = ""
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
  abstract override def ajpPort = syspropInt(propertyAjpPort) orElse (super.ajpPort)
  abstract override def sslBindAddress = sysprop(propertySslBindAddress) orElse (super.sslBindAddress)
  abstract override def bindAddress = sysprop(propertyBindAddress) orElse (super.bindAddress)
  abstract override def contextPath = sysprop(propertyContextPath) getOrElse(super.contextPath)
  abstract override def workingDirectory = sysprop(propertyWorkingDirectory) getOrElse(super.workingDirectory)

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
  abstract override def ajpPort = intProperty(propertyAjpPort) orElse (super.ajpPort)
  abstract override def sslBindAddress = property(propertySslBindAddress) orElse (super.sslBindAddress)
  abstract override def bindAddress = property(propertyBindAddress) orElse (super.bindAddress)
  abstract override def contextPath = property(propertyContextPath) getOrElse(super.contextPath)
  abstract override def workingDirectory = property(propertyWorkingDirectory) getOrElse(super.workingDirectory)
}