/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.ext.jmx

import javax.management.{MBeanInfo, MBeanOperationInfo, MBeanAttributeInfo, AttributeNotFoundException, JMException, Attribute, AttributeList, DynamicMBean}
import java.beans.{PropertyDescriptor, Introspector}
import collection.JavaConversions._
import java.lang.reflect.Method

/**
 * Wraps any object into an mbean.
 *
 * The class hierarchy of the given instance is searched for any interface
 * whose name ends with `MBean`. If such an interface is found, it is used
 * to generate the mbean information. Otherwise all properties and public
 * methods are included.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.12.12 21:06
 */
class MBeanWrapper(ref: AnyRef) extends DynamicMBean {
  ref.getClass.ensuring(c => !c.isInterface, "Cannot wrap interfaces")

  private val properties = {
    val bi = Introspector.getBeanInfo(getExportType(ref))
    bi.getPropertyDescriptors.filter(p => p.getName != "class")
  }

  private val mbeanInfo = {
    val bi = Introspector.getBeanInfo(getExportType(ref))
    val attr = properties.map(MBeanWrapper.createAttributeInfo)
    val ops = bi.getMethodDescriptors
      .map(_.getMethod)
      .withFilter(m => m.getDeclaringClass.getName != "java.lang.Object")
      .withFilter(m => !m.getName.contains("$"))
      .map(MBeanWrapper.createOperationInfo)
    MBeanWrapper.createMBeanInfo(ref, attr, ops)
  }

  private[this] def getExportType(ref: AnyRef): Class[_] = {
    MBeanWrapper.findMBeanInterface(ref).getOrElse(ref.getClass)
  }

  private[this] def noAttribute(name: String) = throw new AttributeNotFoundException(name)

  @throws(classOf[JMException])
  def getAttribute(attribute: String) = properties.find(p => p.getName == attribute)
    .flatMap(p => Option(p.getReadMethod)).map(_.invoke(ref)).getOrElse(noAttribute(attribute))

  @throws(classOf[JMException])
  def setAttribute(attribute: Attribute) {
    properties.find(p => p.getName == attribute.getName)
      .flatMap(p => Option(p.getWriteMethod))
      .map(m => m.invoke(ref, attribute.getValue))
  }

  def getAttributes(attributes: Array[String]) = new AttributeList(
    attributes.toList.map(name => (name, getAttribute(name))).map(t => new Attribute(t._1, t._2))
  )

  def setAttributes(attributes: AttributeList) = {
    attributes.asList().foreach(setAttribute)
    attributes
  }

  @throws(classOf[JMException])
  def invoke(actionName: String, params: Array[AnyRef], signature: Array[String]) = null

  def getMBeanInfo = mbeanInfo
}

object MBeanWrapper {

  /**
   * Inspects the class of the given reference and returns
   * the first interface that is implemented by the given
   * object whose name ends with `MBean`.
   *
   * @param ref
   * @return
   */
  def findMBeanInterface(ref: AnyRef): Option[Class[_]] = {
    ref.getClass.getInterfaces.find(i => i.getSimpleName.toLowerCase.endsWith("mbean"))
  }

  private def createAttributeInfo(p: PropertyDescriptor) =
    new MBeanAttributeInfo(p.getName, p.getPropertyType.getName, p.getReadMethod, p.getWriteMethod)
  private def createOperationInfo(m: Method) = new MBeanOperationInfo(m.getName, m)
  private def createMBeanInfo(ref: AnyRef, attr: Array[MBeanAttributeInfo], ops: Array[MBeanOperationInfo]) =
    new MBeanInfo(ref.getClass.getName, ref.getClass.getName, attr, null, ops, null)
}