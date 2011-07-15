/**
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */
package org.fusesource.fabric.apollo.amqp.protocol

import org.fusesource.fabric.apollo.amqp.api.Session

object ProtocolSession {
  def createSession(connection:ProtocolConnection):ProtocolSession = {
    new AMQPSession(connection)
  }
}

/**
 *
 */
abstract trait ProtocolSession extends Session {

  def setLocalChannel(channel:Int)

  def setRemoteChannel(channel:Int)

  def getLocalChannel:Option[Int]

  def getRemoteChannel:Option[Int]

}