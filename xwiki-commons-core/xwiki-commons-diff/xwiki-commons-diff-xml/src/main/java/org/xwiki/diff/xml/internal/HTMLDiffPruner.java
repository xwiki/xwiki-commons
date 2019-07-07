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

import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xwiki.component.annotation.Component;
import org.xwiki.diff.xml.XMLDiffPruner;

/**
 * Default implementation for {@link XMLDiffPruner} that prunes the unchanged nodes from a change tree marked with
 * {@link HTMLDiffMarker}.
 * 
 * @version $Id$
 * @since 11.6RC1
 */
@Component
@Singleton
@Named("html")
public class HTMLDiffPruner implements XMLDiffPruner
{
    private static final String DIFF_CONTEXT_ATTRIBUTE = "data-xwiki-html-diff-context";

    private static final String DIFF_HIDDEN_ATTRIBUTE = "data-xwiki-html-diff-hidden";

    @Override
    public void prune(Node root)
    {
        // Mark parents and siblings we want to keep by going upwards from the change blocks.
        getElementsWithAttribute(root, HTMLDiffMarker.DIFF_BLOCK_ATTRIBUTE).stream().forEach(this::markContextElements);

        // Iterate the tree top -> down and hide nodes we don't want to keep.
        hideNodesWeDontWantToKeep(root);

        // Remove the marker from the context nodes.
        getElementsWithAttribute(root, DIFF_CONTEXT_ATTRIBUTE).stream().forEach(this::unmarkContextElement);
    }

    private void markContextElements(Node diffBlock)
    {
        Node node = diffBlock;
        while (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
            markContextElement(node);
            getContextSiblings(node).stream().forEach(this::markContextElement);
            node = node.getParentNode();
        }
    }

    private void markContextElement(Node node)
    {
        Element element = (Element) node;
        if (!element.hasAttribute(HTMLDiffMarker.DIFF_BLOCK_ATTRIBUTE)) {
            element.setAttribute(DIFF_CONTEXT_ATTRIBUTE, String.valueOf(true));
        }
    }

    private void unmarkContextElement(Node node)
    {
        ((Element) node).removeAttribute(DIFF_CONTEXT_ATTRIBUTE);
    }

    private List<Node> getContextSiblings(Node node)
    {
        // TODO
        return Collections.emptyList();
    }

    private void hideNodesWeDontWantToKeep(Node node)
    {
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            hideNodesWeDontWantToKeep(((Document) node).getDocumentElement());
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            if (element.hasAttribute(DIFF_CONTEXT_ATTRIBUTE)) {
                XMLDiffUtils.asList(element.getChildNodes()).stream().forEach(this::hideNodesWeDontWantToKeep);
            } else if (!element.hasAttribute(HTMLDiffMarker.DIFF_BLOCK_ATTRIBUTE)) {
                element.setAttribute(DIFF_HIDDEN_ATTRIBUTE, String.valueOf(true));
            }
        }
    }

    private List<Node> getElementsWithAttribute(Node root, String attribute)
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//*[@" + attribute + "]";
        try {
            return XMLDiffUtils.asList((NodeList) xpath.compile(expression).evaluate(root, XPathConstants.NODESET));
        } catch (XPathExpressionException e) {
            return Collections.emptyList();
        }
    }
}
