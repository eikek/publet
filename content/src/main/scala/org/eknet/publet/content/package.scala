package org.eknet.publet

import java.net.URI

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 04.05.13 16:07
 */
package object content {


  /**
   * A converter function that converts a source content into
   * data of a specified target type, So its
   * `(soure data, target type) => target data`
   *
   */
  type Engine = PartialFunction[(Source, ContentType), Source]

  /**
   * Factory that creates a [[org.eknet.publet.content.Partition]]
   * for a given uri.
   */
  type PartitionSupplier = URI => Partition

  def uri(str: String) = new java.net.URI(str)
}
