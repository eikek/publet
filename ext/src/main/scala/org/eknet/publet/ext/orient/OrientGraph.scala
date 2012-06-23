package org.eknet.publet.ext.orient

import com.tinkerpop.blueprints.impls.orient
import com.orientechnologies.orient.core.metadata.schema.{OType, OClass}
import com.orientechnologies.orient.core.index.{OIndexUnique, OPropertyIndexDefinition}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.06.12 17:00
 */
class OrientGraph(url: String) extends orient.OrientGraph(url) {

  /**
   * Create an automatic indexing structure for indexing provided key for element class.
   *
   * Unlike `createKeyIndex` this creates a unique index, where a property value may
   * only exists once!
   *
   * @param key
   * @param elementClass
   * @tparam T
   * @return
   */
  def createUniqueKeyIndex[T](key: String, elementClass: Class[T])  = {
    // this is copy&paste from createKeyIndex and the index type is substituted
    val className: String = getClassName(elementClass)
    val cls: OClass = getRawGraph.getMetadata.getSchema.getClass(className)
    val idxType = OIndexUnique.TYPE_ID
    getRawGraph.getMetadata.getIndexManager.createIndex(className + "." + key, idxType, new OPropertyIndexDefinition(className, key, OType.STRING), cls.getClusterIds, null)
  }

}
