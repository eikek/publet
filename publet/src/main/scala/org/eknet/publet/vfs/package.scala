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