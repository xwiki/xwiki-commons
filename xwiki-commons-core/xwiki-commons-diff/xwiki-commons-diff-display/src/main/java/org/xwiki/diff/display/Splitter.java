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
package org.xwiki.diff.display;

import java.util.List;

import org.xwiki.component.annotation.Role;

/**
 * Splits a composite object in multiple parts of the same type.
 * 
 * @param <E> the type of object being split
 * @param <F> the type of objects created by the split
 * @version $Id$
 * @since 4.1RC1
 */
@Role
public interface Splitter<E, F>
{
    /**
     * Splits the given composite object in multiple parts of the same type.
     * 
     * @param composite the object to split
     * @return the list of objects that make the given composite object
     */
    List<F> split(E composite);
}
