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
import org.xwiki.stability.Unstable;

/**
 * Prunes the nodes that haven't changed from a change tree previously marked with a {@link XMLDiffMarker}.
 * 
 * @version $Id$
 * @since 11.6RC1
 */
@Role
@Unstable
public interface XMLDiffPruner
{
    /**
     * Prunes the unchanged nodes from the given tree that was previously marked with a {@link XMLDiffMarker}.
     * 
     * @param node the tree to be pruned
     */
    void prune(Node node);
}
