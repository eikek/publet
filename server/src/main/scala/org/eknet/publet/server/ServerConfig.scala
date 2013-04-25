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

  /**
   * The maximum number of threads for the servers thread pool.
   * Default is 200.
   *
   * @return
   */
  def serverThreads: Int

  /**
   * Milliseconds to wait for graceful shutdown.
   * Default is 5000.
   * @return
   */
  def gracefulShutdownTimeout: Int
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
  val propertyConnectorThreads = "publet.server.connectorThreads"
  val propertyGracefulShutdownTimeout = "publet.server.gracefulShutdownTimeout"
}

class DefaultConfig extends ServerConfig {

  private var _port: Option[Int] = None
  private var _securePort: Option[Int] = None
  private var _keystorePath = ""
  private var _keystorePassword = ""
  private var _shutdownPort = 8099
  private var _bindAddress: Option[String] = None
  private var _sslBindAddress: Option[String] = None
  private var _contextPath = "/"
  private var _workdingDirectory = ""
  private var _connectorThreads = 20
  private var _gracefulShutdownTimeout = 5000

  def port:Option[Int] = _port
  def securePort:Option[Int] = _securePort
  def keystorePath:String = _keystorePath
  def keystorePassword:String = _keystorePassword
  def shutdownPort:Int = _shutdownPort
  def sslBindAddress:Option[String] = _sslBindAddress
  def bindAddress:Option[String] = _bindAddress
  def contextPath = _contextPath
  def workingDirectory = _workdingDirectory
  def serverThreads = _connectorThreads
  def gracefulShutdownTimeout = _gracefulShutdownTimeout

  def setPort(p: Option[Int]) { this._port = p }
  def setSecurePort(p: Option[Int]) { this._securePort = p }
  def setKeystorePath(p: String) { this._keystorePath = p }
  def setKeystorePassword(p: String) { this._keystorePassword = p }
  def setShutdownPort(p: Int) { this._shutdownPort = p }
  def setBindAddress(a: Option[String]) {this._bindAddress = a}
  def setSslBindAddress(a: Option[String]) { this._sslBindAddress = a }
  def setWorkingDirectory(wd: String) { this._workdingDirectory = wd }
  def setContextPath(cp: String) {this._contextPath = cp}
  def setConnectorThreads(tc: Int) {this._connectorThreads = tc}
  def setGracefulShutdownTimeout(t: Int) { this._gracefulShutdownTimeout = t }
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
  abstract override def sslBindAddress = sysprop(propertySslBindAddress) orElse (super.sslBindAddress)
  abstract override def bindAddress = sysprop(propertyBindAddress) orElse (super.bindAddress)
  abstract override def contextPath = sysprop(propertyContextPath) getOrElse(super.contextPath)
  abstract override def workingDirectory = sysprop(propertyWorkingDirectory) getOrElse(super.workingDirectory)
  abstract override def serverThreads = syspropInt(propertyConnectorThreads) getOrElse(super.serverThreads)
  abstract override def gracefulShutdownTimeout = syspropInt(propertyGracefulShutdownTimeout) getOrElse(super.gracefulShutdownTimeout)

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
  abstract override def sslBindAddress = property(propertySslBindAddress) orElse (super.sslBindAddress)
  abstract override def bindAddress = property(propertyBindAddress) orElse (super.bindAddress)
  abstract override def contextPath = property(propertyContextPath) getOrElse(super.contextPath)
  abstract override def workingDirectory = property(propertyWorkingDirectory) getOrElse(super.workingDirectory)
  abstract override def serverThreads = intProperty(propertyConnectorThreads) getOrElse(super.serverThreads)
  abstract override def gracefulShutdownTimeout = intProperty(propertyGracefulShutdownTimeout) getOrElse(super.gracefulShutdownTimeout)
}