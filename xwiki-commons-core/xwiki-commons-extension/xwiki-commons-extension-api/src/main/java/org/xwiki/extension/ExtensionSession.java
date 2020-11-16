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

import org.xwiki.stability.Unstable;

/**
 * A short lived container used to store extensions manipulations related data which can be disposed.
 * <p>
 * Any entry value implementing {@link java.io.Closeable} will be called when the session is destroyed.
 * 
 * @version $Id$
 * @since 12.10RC1
 */
@Unstable
public interface ExtensionSession
{
    /**
     * @param key the key associated to the session entry
     * @param value the value
     */
    void set(String key, Object value);

    /**
     * @param <T> the type of value associated with the passed key
     * @param key the key associated to the session entry
     * @return the value associated to the passed key
     */
    <T> T get(String key);
}
