/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.eknet.publet.auth

import scala.Predef.String
import org.apache.shiro.crypto.hash.Md5Hash

/**
 * This object provides helper methods to generate the HA1 value
 * for http-digest authentication.
 *
 * This code is taken with great thanks from the same named class
 * of the milton webdav project `com.bradmcevoy.http.http11.auth.DigestGenerator`.
 *
 */
object DigestGenerator {

  /**
   * Computes the <code>response</code> portion of a Digest authentication header. Both the server and user
   * agent should compute the <code>response</code> independently. Provided as a static method to simplify the
   * coding of user agents.
   *
   * @param dr - the auth request from the client
   * @param password - plain text unencoded password
   * @return the MD5 of the digest authentication response, encoded in hex
   * @throws IllegalArgumentException if the supplied qop value is unsupported.
   */
  def generateDigest(dr: DigestResponse, password: String): String = {
    val p = if (password == null) "" else password
    val a1Md5 = encodePasswordInA1Format(dr.user, dr.realm, p)
    generateDigestWithEncryptedPassword(dr, a1Md5)
  }

  /**
   * Use this method if you are persisting a one way hash of the user name, password
   * and realm (referred to as a1md5 in the spec)
   *
   * @param dr
   * @param a1Md5
   * @return
   */
  def generateDigestWithEncryptedPassword(dr: DigestResponse, a1Md5: String): String = {
    val httpMethod: String = dr.method
    val a2Md5 = encodeMethodAndUri(httpMethod, dr.uri)
    val qop: String = dr.qop
    val nonce: String = dr.nonce
    if (qop == null) {
      md5(a1Md5, dr.nonce, a2Md5)
    }
    else if ("auth" == qop) {
      md5(a1Md5, nonce, dr.nc, dr.cnonce, dr.qop, a2Md5)
    }
    else {
      throw new IllegalArgumentException("This method does not support a qop '" + qop + "'")
    }
  }

  def encodePasswordInA1Format(username: String, realm: String, password: String): String = {
    val a1: String = username + ":" + realm + ":" + password
    md5Hex(a1)
  }

  private[auth] def encodeMethodAndUri(httpMethod: String, uri: String): String = {
    val a2: String = httpMethod + ":" + uri
    md5Hex(a2)
  }

  private[auth] def md5(ss: String*): String = md5Hex(ss.mkString(":"))

  private def md5Hex(str: String) = new Md5Hash(str).toHex
}

case class DigestResponse(method: String,
                          user: String,
                          realm: String,
                          nonce: String,
                          uri: String,
                          responseDigest: String,
                          qop: String,
                          nc: String,
                          cnonce: String)