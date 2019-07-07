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

import org.w3c.dom.Node;
import org.xwiki.component.annotation.Role;
import org.xwiki.diff.DiffException;
import org.xwiki.stability.Unstable;

/**
 * Marks the difference between two XML documents.
 * 
 * @version $Id$
 * @since 11.6RC1
 */
@Role
@Unstable
public interface XMLDiffMarker
{
    /**
     * Computes the differences between two XML fragments rooted in the given nodes and then marks the changes by
     * modifying these nodes and their descendants, depending on the implementation. An implementation that shows the
     * differences in-line would probably change only the left side. An implementation that shows the differences
     * side-by-side would probably modify (mark) both sides.
     * <p>
     * Note that this method can return {@code false} (i.e. no markers added) even if there are differences between left
     * and right, if the implementation doesn't consider the changes to be significant (e.g. there's no visual
     * difference).
     * 
     * @param left the left side of the comparison
     * @param right the right side of the comparison
     * @return {@code true} if there is at least one difference between left and right side that has been marked,
     *         {@code false} otherwise
     * @throws DiffException if we fail to compute the difference
     */
    boolean markDiff(Node left, Node right) throws DiffException;
}
