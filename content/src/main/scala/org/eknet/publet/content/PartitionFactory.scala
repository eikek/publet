package org.eknet.publet.content

import java.net.URI

/**
 * Manages partition factories that create [[org.eknet.publet.content.Partition]] instances
 * from a provided URI. Concrete factories are registered for a URI scheme. When creating
 * a partition for an uri, the concrete factory is looked up using the uri scheme and the
 * uri is passed on.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.13 10:00
 */
class PartitionFactory extends PartitionSupplier with Registry {

  type Key = String
  type Value = PartitionSupplier

  def getFactory(scheme: String): Option[PartitionSupplier] = headOption(scheme)

  def get(uri: URI): Option[Partition] = getFactory(uri.getScheme).map(f => f(uri))

  def apply(uri: URI): Partition = get(uri).getOrElse(sys.error(s"No partition factory registered for uri scheme ${uri.getScheme}"))

  def apply(uriString: String): Partition = apply(uri(uriString))
}

class ClasspathPartitonFactory(loader: ClassLoader) extends PartitionSupplier {
  def apply(uri: URI) = new ClasspathPartition(loader, base = Path(uri.getSchemeSpecificPart).absoluteString)
  override def toString() = getClass.getSimpleName+"["+loader+"]"
}