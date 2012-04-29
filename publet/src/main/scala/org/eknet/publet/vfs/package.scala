package org.eknet.publet

/**
 * Yet another virtual file system. It is very simple and should provide
 * means to mount resources of different sources into one commons resource
 * tree.
 *
 * Therefore the [[org.eknet.publet.vfs.Resource]] class don't hold  a
 * reference to its parent resource. This makes them easier to implement
 * and to add to different parents.
 *
 * The [[org.eknet.publet.vfs.fs]] contains a default implementation that
 * maps to the local file system, while [[org.eknet.publet.vfs.util]] provides
 * utility implementations.
 *
 */
package vfs {}