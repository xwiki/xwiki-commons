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

import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xwiki.component.annotation.Role;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.Patch;
import org.xwiki.stability.Unstable;

/**
 * Computes the differences between two XML documents.
 * 
 * @version $Id$
 * @since 11.6RC1
 */
@Role
@Unstable
public interface XMLDiff
{
    /**
     * whatever.
     *
     * @since 12.4RC1
     */
    @Unstable
    void xxx();

    /**
     * Computes the difference between two XML nodes and their descendants. When a value node type (e.g. text,
     * attribute, comment) is modified we compute the difference on the text value using the splitter indicated by the
     * configuration. Otherwise the difference is expressed at node level, as if two lists of nodes are compared.
     * <p>
     * The result is a mapping between nodes from the left side and the patches that need to be applied to these nodes
     * in order for the left tree to become the right tree. If the root nodes of the left and right trees don't match
     * then this change is mapped to the {@code null} key.
     * 
     * @param left the left side of the comparison
     * @param right the right side of the comparison
     * @param config the configuration
     * @return the differences between the two XML nodes
     * @throws DiffException if the difference can't be computed
     */
    Map<Node, Patch<?>> diff(Node left, Node right, XMLDiffConfiguration config) throws DiffException;

    /**
     * Computes the difference between two lists of XML nodes. The nodes that match (are similar on both sides) are
     * compared at lower level (we look for differences in their descendants).
     * 
     * @param left the left side of the comparison
     * @param right the right side of the comparison
     * @param config the configuration
     * @return the differences between the two lists of XML nodes
     * @throws DiffException if the difference can't be computed
     */
    Map<Node, Patch<?>> diff(NodeList left, NodeList right, XMLDiffConfiguration config) throws DiffException;

    /**
     * Computes the difference between two maps of XML nodes. This is mostly used to compare the attributes of two XML
     * elements. The values of the attributes that are on both sides are compared using the splitter indicated by the
     * configuration. The deltas that correspond to deleted or added attributes have index {@code -1} because attribute
     * order is not significant (we're comparing two maps of nodes anyway).
     * 
     * @param left the left side of the comparison
     * @param right the right side of the comparison
     * @param config the configuration
     * @return the differences between the two maps of XML nodes
     * @throws DiffException if the difference can't be computed
     */
    Map<Node, Patch<?>> diff(NamedNodeMap left, NamedNodeMap right, XMLDiffConfiguration config) throws DiffException;
}
