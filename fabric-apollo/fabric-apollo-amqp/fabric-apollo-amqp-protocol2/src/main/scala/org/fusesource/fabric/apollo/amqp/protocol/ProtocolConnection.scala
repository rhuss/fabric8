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

import org.fusesource.fabric.apollo.amqp.api.Connection
import org.fusesource.fabric.apollo.amqp.api.Session
import org.fusesource.hawtbuf.Buffer

/**
 *
 */
abstract trait ProtocolConnection extends Connection {
  def send(data: Buffer): Unit

  def send(data: Buffer, channel: Int): Unit

  def release(session: ProtocolSession): Unit
}