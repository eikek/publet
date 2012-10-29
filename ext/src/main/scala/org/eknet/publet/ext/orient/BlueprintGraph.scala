package org.eknet.publet.ext.orient

import com.tinkerpop.blueprints.TransactionalGraph
import com.tinkerpop.blueprints.IndexableGraph
import com.tinkerpop.blueprints.KeyIndexableGraph

/**
 * Super trait of features implemented by [[com.tinkerpop.blueprints.impls.orient.OrientGraph]].
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.06.12 17:00
 */
trait BlueprintGraph extends TransactionalGraph with IndexableGraph with KeyIndexableGraph