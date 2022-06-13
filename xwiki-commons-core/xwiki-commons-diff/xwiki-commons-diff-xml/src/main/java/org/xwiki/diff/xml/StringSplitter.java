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
package org.xwiki.diff.xml;

import java.util.List;

import org.xwiki.component.annotation.Role;

/**
 * Used to split strings before computing their changes and to re-create the strings after applying patches.
 * 
 * @version $Id$
 * @since 11.10.1
 * @since 12.0RC1
 */
@Role
public interface StringSplitter
{
    /**
     * Splits the given string into a list of objects, which can be characters, words, phrases, etc. depending on the
     * implementation. A larger list of objects will lead to a more detailed difference report when comparing strings.
     * 
     * @param text the string to split
     * @return the list of objects on which to compute the changes when comparing strings
     */
    List<Object> split(String text);

    /**
     * The reverse operation of {@link #split(String)}. Recreates the string from a list of objects.
     * 
     * @param objects a list of objects produced by {@link #split(String)} or by patching such a list
     * @return a string that produces the given list of objects when passed to {@link #split(String)}
     */
    String join(List<Object> objects);
}
