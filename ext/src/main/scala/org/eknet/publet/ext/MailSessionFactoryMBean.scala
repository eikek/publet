/*
 * Copyright 2013 Eike Kettner
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

package org.eknet.publet.ext

import javax.management.MXBean

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.02.13 22:00
 */
@MXBean
trait MailSessionFactoryMBean {

  def isUseSsl: Boolean
  def setUseSsl(value: Boolean)

  def isSmtpAuth: Boolean
  def setSmtpAuth(value: Boolean)

  def isStartTLS: Boolean
  def setStartTLS(value: Boolean)

  def getSocketFactoryClass: String
  def setSocketFactoryClass(className: String)

  def getSmtpHost: String
  def setSmtpHost(hostname: String)

  def getSmtpUsername: String
  def setSmtpUsername(user: String)

  def getSmtpPassword: Array[Char]

  def getProtocol: String
  def setProtocol(protocol: String)

  def getSmtpPort: Int
  def setSmtpPort(port: Int)

}
