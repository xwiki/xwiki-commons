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

import org.xwiki.diff.DiffConfiguration;

/**
 * Configuration for {@link UnifiedDiffDisplayer}.
 * 
 * @param <E> the type of elements that are compared to produce the first level diff
 * @param <F> the type of sub-elements that can be compared to produce the second level diff when an element is modified
 * @version $Id$
 * @since 4.1RC1
 */
public class UnifiedDiffConfiguration<E, F> extends DiffConfiguration<F>
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The number of unmodified elements to display before and after a chunk of modified elements. This number
     * determines how blocks are created. If the distance between two changes is less than the double of this value then
     * those changes are put in the same block (they share the same context).
     */
    private int contextSize = 3;

    /**
     * The object used to split the modified elements into sub-elements in order to produce a more detailed diff. This
     * allows us to display changes at two levels of granularity (elements and their sub-elements).
     */
    private Splitter<E, F> splitter;

    /**
     * @return the number of unmodified elements to display before and after a chunk of modified elements
     */
    public int getContextSize()
    {
        return contextSize;
    }

    /**
     * Sets the number of unmodified elements to display before and after a chunk of modified elements.
     * 
     * @param contextSize the context size
     */
    public void setContextSize(int contextSize)
    {
        this.contextSize = contextSize;
    }

    /**
     * @return the object used to split the modified elements into sub-elements in order to produce a more detailed diff
     */
    public Splitter<E, F> getSplitter()
    {
        return splitter;
    }

    /**
     * Sets The object used to split the modified elements into sub-elements in order to produce a more detailed diff.
     * 
     * @param splitter the splitter
     */
    public void setSplitter(Splitter<E, F> splitter)
    {
        this.splitter = splitter;
    }
}
