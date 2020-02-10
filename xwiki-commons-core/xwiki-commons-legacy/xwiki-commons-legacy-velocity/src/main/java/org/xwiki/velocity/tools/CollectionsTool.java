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
package org.xwiki.velocity.tools;

import java.util.List;

/**
 * Velocity Tool allowing to create various type of collections.
 *
 * @version $Id$
 * @since 4.0M1
 * @deprecated since 12.1RC1, use {@link org.xwiki.velocity.tools.CollectionTool} instead
 */
@Deprecated
public class CollectionsTool extends CollectionTool
{
    /**
     * Reverse the order of the elements within a list, so that the last element is moved to the beginning of the list,
     * the next-to-last element to the second position, and so on. The input list is modified in place, so this
     * operation will succeed only if the list is modifiable.
     *
     * @param <E> the type of the elements in the list
     * @param input the list to reverse
     * @return {@code true} if the list was successfully reversed, {@code false} otherwise
     */
    public <E> boolean reverse(List<E> input)
    {
        return super.reverseModifiable(input);
    }

    /**
     * Sort the elements within a list according to their natural order. The input list is modified in place, so this
     * operation will succeed only if the list is modifiable.
     *
     * @param <E> the type of the elements in the list
     * @param input the list to sort
     * @return {@code true} if the list was successfully sorted, {@code false} otherwise
     */
    public <E extends Comparable<E>> boolean sort(List<E> input)
    {
        return super.sortModifiable(input);
    }
}
