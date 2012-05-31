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

/**
 * Extends a {@link UnifiedDiffElement} with information about changes inside that element. This allows us to display
 * changes at two levels of granularity (elements and their sub-elements).
 * 
 * @param <E> the type of elements that are compared to produce the first level of diff
 * @param <F> the type of sub-elements that are compared to produce the second level of diff
 * @version $Id$
 * @since 4.1RC1
 */
public class ExtendedDiffElement<E, F> extends UnifiedDiffElement<E>
{
    /**
     * The list of chunks of sub-elements that form this element.
     */
    private final List<InlineDiffChunk<F>> chunks;

    /**
     * Creates a new element with the provided information.
     * 
     * @param element the element that is extended
     * @param chunks the list of chunks of sub-elements that form the element
     */
    public ExtendedDiffElement(UnifiedDiffElement<E> element, List<InlineDiffChunk<F>> chunks)
    {
        super(element.getIndex(), element.getType(), element.getValue());
        this.chunks = chunks;
    }

    /**
     * @return the list of chunks of sub-elements that form this element
     */
    public List<InlineDiffChunk<F>> getChunks()
    {
        return chunks;
    }
}
