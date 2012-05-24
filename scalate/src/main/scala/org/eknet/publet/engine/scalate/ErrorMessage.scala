package org.eknet.publet.engine.scalate

import util.parsing.input.Position

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.05.12 16:24
 */
case class ErrorMessage(pos: Position, message: String)
