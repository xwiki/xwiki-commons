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

import java.util.Collections;
import java.util.List;

import org.xwiki.component.annotation.Role;

/**
 * Used to configure the way we compute changes between two XML documents.
 * 
 * @version $Id$
 * @since 11.10.1
 * @since 12.0RC1
 */
@Role
public interface XMLDiffConfiguration
{
    /**
     * @return the list of filters that should be applied on the XML documents before and after computing their changes
     */
    default List<XMLDiffFilter> getFilters()
    {
        return Collections.emptyList();
    }

    /**
     * @param nodeType an XML DOM node type
     * @return the splitter to use for the specified node type
     */
    default StringSplitter getSplitterForNodeType(short nodeType)
    {
        return null;
    }

    /**
     * @return the threshold below which two nodes are considered to be very similar; the value must be between 0 and 1,
     *         where 0 means identical nodes and 1 means completely different nodes
     */
    default double getSimilarityThreshold()
    {
        return 0;
    }
}
