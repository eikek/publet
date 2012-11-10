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

package org.eknet.publet.auth

import org.eknet.publet.event.Event

/**
 * Event that is emitted if a store has been modified that contains
 * information used for authentication or authorization.
 *
 * Realms can listen on that event to clear its caches.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.11.12 18:45
 */
final class AuthDataChanged extends Event {

}
