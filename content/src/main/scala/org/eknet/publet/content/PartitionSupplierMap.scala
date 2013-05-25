package org.eknet.publet.content

import java.net.URI

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.05.13 18:32
 */
class PartitionSupplierMap(map: Map[String, PartitionSupplier] = Map.empty) {

  def get(scheme: String) = map.get(scheme)
  def apply(scheme: String) = map(scheme)
  def update(scheme: String, supplier: PartitionSupplier) = new PartitionSupplierMap(map.updated(scheme, supplier))
  def remove(scheme: String) = new PartitionSupplierMap(map - scheme)

  def create(uri: URI): Partition = apply(uri.getScheme)(uri)
  def create(uriString: String): Partition = create(uri(uriString))

}
