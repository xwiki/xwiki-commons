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
package org.xwiki.xstream.internal;

import org.xwiki.component.annotation.Role;

/**
 * An extension point to add ways to check if an object is serializable.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
@Role
public interface XStreamChecker
{
    /**
     * @param o the object to check
     * @return true of the object is known to be serializable, false if it's known to not be serializable. Null if
     *         unknown.
     */
    Boolean isSerializable(Object o);

    /**
     * @param c the class to check
     * @return true of the class is known to be serializable, false if it's known to not be serializable. Null if
     *         unknown.
     */
    Boolean isSerializable(Class<?> c);
}
