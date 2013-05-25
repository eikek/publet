package org.eknet.publet.content

import java.net.URI

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.13 10:00
 */
class ClasspathPartitonFactory(loader: ClassLoader) extends PartitionSupplier {
  def apply(uri: URI) = new ClasspathPartition(loader, base = Path(uri.getSchemeSpecificPart).absoluteString)
  override def toString() = getClass.getSimpleName+"["+loader+"]"
}