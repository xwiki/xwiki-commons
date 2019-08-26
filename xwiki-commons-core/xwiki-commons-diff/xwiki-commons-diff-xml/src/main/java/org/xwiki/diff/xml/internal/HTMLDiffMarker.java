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

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xwiki.component.annotation.Component;
import org.xwiki.diff.xml.XMLDiffMarker;

/**
 * Default implementation of {@link XMLDiffMarker} that displays (marks) the differences in-line (only the left side of
 * the comparison is marked).
 * 
 * @version $Id$
 * @since 11.6RC1
 */
@Component
@Singleton
@Named("html")
public class HTMLDiffMarker extends AbstractXMLDiffMarker
{
    static final String DIFF_BLOCK_ATTRIBUTE = "data-xwiki-html-diff-block";

    private static final List<String> IGNORED_TAGS = Arrays.asList("");

    private static final List<String> ACCEPTED_ATTRIBUTES = Arrays.asList("align", "background", "bgcolor", "border",
        "cite", "class", "color", "cols", "colspan", "controls", "coords", "data", "dir", "disabled", "height",
        "hidden", "high", "icon", "label", "low", "multiple", "placeholder", "rows", "rowspan", "shape", "size", "src",
        "start", "style", "summary", "type", "value", "width", "wrap");

    private static final List<String> BLOCK_ELEMENTS_WE_CAN_DUPLICATE =
        Arrays.asList("audio", "video", "figure", "figcaption", "ol", "ul", "li", "dl", "dd", "dt", "table", "tr",
            "address", "div", "p", "h1", "h2", "h3", "h4", "h5", "h6", "pre", "blockquote", "form", "fieldset", "hr",
            "article", "aside", "details", "footer", "header", "main", "nav", "section", "center");

    private static final List<String> TAGS_THAT_DONT_ACCEPT_INLINE_MARKER =
        Arrays.asList("html", "head", "base", "meta", "title", "link", "script", "style", "br", "hr", "img", "picture",
            "embed", "iframe", "area", "param", "source", "track", "input", "textarea", "select", "optgroup", "option",
            "dl", "ol", "ul", "table", "thead", "tfoot", "tbody", "colgroup", "col", "tr");

    private static final String DELETED = "deleted";

    private static final String INSERTED = "inserted";

    private static final String RIGHT_BLOCK = "xwiki-html-diff-block-right";

    //
    // Filtering
    //

    @Override
    protected boolean acceptChangesFor(Element element)
    {
        return !IGNORED_TAGS.contains(element.getNodeName());
    }

    @Override
    protected boolean acceptChangesFor(Attr attribute)
    {
        return ACCEPTED_ATTRIBUTES.contains(attribute.getName());
    }

    //
    // Mark Change Blocks
    //

    @Override
    protected boolean acceptAsDiffBlock(Element element)
    {
        return BLOCK_ELEMENTS_WE_CAN_DUPLICATE.contains(element.getNodeName());
    }

    @Override
    protected void markDiffBlock(Element element)
    {
        element.setAttribute(DIFF_BLOCK_ATTRIBUTE, DELETED);
    }

    @Override
    protected void unmarkDiffBlock(Element element)
    {
        element.removeAttribute(DIFF_BLOCK_ATTRIBUTE);
    }

    @Override
    protected boolean isMarkedAsDiffBlock(Element element)
    {
        return element.hasAttribute(DIFF_BLOCK_ATTRIBUTE);
    }

    //
    // Mark in-line changes
    //

    @Override
    protected boolean supportsInlineMarkerElements(Element parent)
    {
        return !TAGS_THAT_DONT_ACCEPT_INLINE_MARKER.contains(parent.getNodeName());
    }

    @Override
    protected String getInlineMarkerElementName()
    {
        return "span";
    }

    @Override
    protected void markElementModified(Element element, boolean deleted)
    {
        String attribute = element.hasAttribute(DIFF_BLOCK_ATTRIBUTE) ? DIFF_BLOCK_ATTRIBUTE : "data-xwiki-html-diff";
        markElementModified(element, attribute, deleted);
    }

    private void markElementModified(Element element, String markerAttribute, boolean deleted)
    {
        // Note that we don't use the class attribute because it may conflict with some patches.
        element.setAttribute(markerAttribute, deleted ? DELETED : INSERTED);
    }

    @Override
    protected Node getOrCreateRightNode(Node left)
    {
        if (left.getNodeType() == Node.ATTRIBUTE_NODE) {
            Element elementRight = (Element) getOrCreateRightNode(((Attr) left).getOwnerElement());
            return elementRight.getAttributeNode(left.getNodeName());
        } else if (left.getNodeType() == Node.ELEMENT_NODE && isMarkedAsDiffBlock((Element) left)) {
            Element diffBlockLeft = (Element) left;
            // We don't look for the next sibling because the right block is inserted as the next sibling after all the
            // patches have been applied (otherwise we change the index of the child nodes and this can invalidate
            // patches). We store the reference to the right block using custom user data on the left block.
            Element diffBlockRight = (Element) diffBlockLeft.getUserData(RIGHT_BLOCK);
            if (diffBlockRight == null) {
                diffBlockRight = (Element) diffBlockLeft.cloneNode(true);
                markElementModified(diffBlockLeft, DIFF_BLOCK_ATTRIBUTE, true);
                markElementModified(diffBlockRight, DIFF_BLOCK_ATTRIBUTE, false);
                // We don't add the right block to the document yet. We only save its reference. See #cleanUp(Node).
                diffBlockLeft.setUserData(RIGHT_BLOCK, diffBlockRight, null);
            }
            return diffBlockRight;
        }

        // Go upwards in the tree.
        Node parentLeft = left.getParentNode();
        if (parentLeft != null) {
            Node parentRight = getOrCreateRightNode(parentLeft);
            return parentRight.getChildNodes().item(XMLDiffUtils.getNodeIndex(left));
        }

        return null;
    }

    @Override
    protected void cleanUp(Node node)
    {
        // Insert the right blocks at the end so that we don't change the index of the child nodes while patches are
        // being applied.
        insertRightBlocks(node);

        super.cleanUp(node);
    }

    private void insertRightBlocks(Node node)
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//*[@" + DIFF_BLOCK_ATTRIBUTE + " = '" + DELETED + "']";
        try {
            XMLDiffUtils.asList((NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET)).stream()
                .forEach(this::insertRightBlock);
        } catch (XPathExpressionException e) {
            // This shouldn't happen.
        }
    }

    private void insertRightBlock(Node leftBlock)
    {
        Element rightBlock = (Element) leftBlock.getUserData(RIGHT_BLOCK);
        if (rightBlock != null) {
            leftBlock.getParentNode().insertBefore(rightBlock, leftBlock.getNextSibling());
        }
    }
}
