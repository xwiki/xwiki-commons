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
package org.xwiki.diff.xml.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility methods used in this module.
 * 
 * @version $Id$
 * @since 11.6RC1
 */
public final class XMLDiffUtils
{
    /**
     * Private constructor as this is an utility class.
     */
    private XMLDiffUtils()
    {
    }

    /**
     * @param node the node for which to compute the index
     * @return the index of the given node within its siblings
     */
    public static int getNodeIndex(Node node)
    {
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return -1;
        }
        int index = 0;
        for (Node previousSibling = node.getPreviousSibling(); previousSibling != null; previousSibling =
            previousSibling.getPreviousSibling()) {
            index++;
        }
        return index;
    }

    /**
     * @param nodeList a node list
     * @return the list of nodes
     */
    public static List<Node> asList(NodeList nodeList)
    {
        List<Node> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i));
        }
        return list;
    }

    /**
     * @param string the string whose characters to return
     * @return the list of characters of the given string
     */
    public static List<Character> toCharacterList(String string)
    {
        return string.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
    }
}
