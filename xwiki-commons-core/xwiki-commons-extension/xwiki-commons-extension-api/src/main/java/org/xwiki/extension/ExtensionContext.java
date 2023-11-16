/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.extension;

import java.util.Optional;

import org.xwiki.component.annotation.Role;

/**
 * Manipulate extension specific contextual informations.
 * 
 * @version $Id$
 * @since 12.10
 */
@Role
public interface ExtensionContext
{
    /**
     * Increment by 1 the session level and create or return the session associated with the current thread.
     * 
     * @return the current session
     */
    ExtensionSession pushSession();

    /**
     * Decrement by 1 the session level and destroy the current session when reaching the first level.
     */
    void popSession();

    /**
     * @return the current {@link ExtensionSession}
     * @since 14.7RC1
     * @since 14.4.4
     * @since 13.10.9
     */
    default Optional<ExtensionSession> getExtensionSession()
    {
        return Optional.empty();
    }
}
