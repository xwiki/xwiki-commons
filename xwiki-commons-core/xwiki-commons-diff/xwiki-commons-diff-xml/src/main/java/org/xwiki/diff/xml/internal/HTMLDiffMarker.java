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

import org.apache.commons.lang3.StringUtils;
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

    private static final List<String> IGNORED_TAGS = List.of("");

    // Commonly used tag names

    private static final String DIR = "dir";

    private static final String STYLE = "style";

    private static final String AUDIO = "audio";

    private static final String VIDEO = "video";

    private static final String FIGURE = "figure";

    private static final String FIGCAPTION = "figcaption";

    private static final String OL = "ol";

    private static final String UL = "ul";

    private static final String DL = "dl";

    private static final String DD = "dd";

    private static final String DT = "dt";

    private static final String LI = "li";

    private static final String TABLE = "table";

    private static final String TR = "tr";

    private static final String ADDRESS = "address";

    private static final String DIV = "div";

    private static final String P = "p";

    private static final String H1 = "h1";

    private static final String H2 = "h2";

    private static final String H3 = "h3";

    private static final String H4 = "h4";

    private static final String H5 = "h5";

    private static final String H6 = "h6";

    private static final String PRE = "pre";

    private static final String BLOCKQUOTE = "blockquote";

    private static final String FORM = "form";

    private static final String FIELDSET = "fieldset";

    private static final String HR = "hr";

    private static final String ARTICLE = "article";

    private static final String ASIDE = "aside";

    private static final String DETAILS = "details";

    private static final String FOOTER = "footer";

    private static final String HEADER = "header";

    private static final String MAIN = "main";

    private static final String NAV = "nav";

    private static final String SECTION = "section";

    private static final String CENTER = "center";

    private static final String BODY = "body";

    private static final String HEAD = "head";

    private static final String HTML = "html";

    private static final String OPTGROUP = "optgroup";

    private static final String PICTURE = "picture";

    private static final String TBODY = "tbody";

    private static final String TFOOT = "tfoot";

    private static final String THEAD = "thead";

    private static final String COLGROUP = "colgroup";

    private static final String COL = "col";

    private static final String BR = "br";

    private static final String IMG = "img";

    private static final String EMBED = "embed";

    private static final String IFRAME = "iframe";

    private static final String AREA = "area";

    private static final String PARAM = "param";

    private static final String SOURCE = "source";

    private static final String TRACK = "track";

    private static final String INPUT = "input";

    private static final String TEXTAREA = "textarea";

    private static final String SELECT = "select";

    private static final String OPTION = "option";

    private static final String HGROUP = "hgroup";

    private static final String MENU = "menu";

    private static final String NOFRAMES = "noframes";

    private static final String BASE = "base";

    private static final String META = "meta";

    private static final String TITLE = "title";

    private static final String LINK = "link";

    private static final String SCRIPT = "script";

    // Commonly used attribute names

    private static final String ALIGN = "align";

    private static final String BACKGROUND = "background";

    private static final String BGCOLOR = "bgcolor";

    private static final String BORDER = "border";

    private static final String CITE = "cite";

    private static final String CLASS = "class";

    private static final String COLOR = "color";

    private static final String COLS = "cols";

    private static final String COLSPAN = "colspan";

    private static final String CONTROLS = "controls";

    private static final String COORDS = "coords";

    private static final String DATA = "data";

    private static final String DISABLED = "disabled";

    private static final String HEIGHT = "height";

    private static final String HIDDEN = "hidden";

    private static final String HIGH = "high";

    private static final String ICON = "icon";

    private static final String LABEL = "label";

    private static final String LOW = "low";

    private static final String MULTIPLE = "multiple";

    private static final String PLACEHOLDER = "placeholder";

    private static final String ROWS = "rows";

    private static final String ROWSPAN = "rowspan";

    private static final String SHAPE = "shape";

    private static final String SIZE = "size";

    private static final String SRC = "src";

    private static final String START = "start";

    private static final String SUMMARY = "summary";

    private static final String TYPE = "type";

    private static final String VALUE = "value";

    private static final String WIDTH = "width";

    private static final String WRAP = "wrap";

    private static final List<String> ACCEPTED_ATTRIBUTES = Arrays.asList(
        ALIGN, BACKGROUND, BGCOLOR, BORDER, CITE, CLASS, COLOR, COLS, COLSPAN, CONTROLS, COORDS, DATA, DIR, DISABLED,
        HEIGHT, HIDDEN, HIGH, ICON, LABEL, LOW, MULTIPLE, PLACEHOLDER, ROWS, ROWSPAN, SHAPE, SIZE, SRC, START, STYLE,
        SUMMARY, TYPE, VALUE, WIDTH, WRAP
    );

    private static final List<String> BLOCK_ELEMENTS_WE_CAN_DUPLICATE = Arrays.asList(
        AUDIO, VIDEO, FIGURE, FIGCAPTION, OL, UL, LI, DL, DD, DT, TABLE, TR, ADDRESS, DIV, P, H1, H2, H3, H4, H5, H6,
        PRE, BLOCKQUOTE, FORM, FIELDSET, HR, ARTICLE, ASIDE, DETAILS, FOOTER, HEADER, MAIN, NAV, SECTION, CENTER
    );

    private static final List<String> TAGS_THAT_DONT_ACCEPT_INLINE_MARKER = Arrays.asList(
        HTML, HEAD, BASE, META, TITLE, LINK, SCRIPT, STYLE, BR, HR, IMG, PICTURE, EMBED, IFRAME, AREA, PARAM, SOURCE,
        TRACK, INPUT, TEXTAREA, SELECT, OPTGROUP, OPTION, DL, OL, UL, TABLE, THEAD, TFOOT, TBODY, COLGROUP, COL, TR
    );

    private static final List<String> TAGS_WITH_INSIGNIFICANT_WHITESPACE = Arrays.asList(
        BODY, COLGROUP, DL, HEAD, HTML, OL, OPTGROUP, PICTURE, SELECT, TABLE, TBODY, TFOOT, THEAD, TR, UL
    );

    private static final List<String> BLOCK_ELEMENTS = Arrays.asList(
        ADDRESS, ARTICLE, ASIDE, AUDIO, BLOCKQUOTE, CENTER, DD, DETAILS, DIR, DIV, DL, DT, FIELDSET, FIGCAPTION, FIGURE,
        FOOTER, FORM, H1, H2, H3, H4, H5, H6, HEADER, HGROUP, HR, LI, MAIN, MENU, NAV, NOFRAMES, OL, P, PRE, SECTION,
        TABLE, UL, VIDEO
    );

    private static final String DELETED = "deleted";

    private static final String INSERTED = "inserted";

    private static final String RIGHT_BLOCK = "xwiki-html-diff-block-right";

    //
    // Normalize
    //

    @Override
    protected void normalize(Node node)
    {
        super.normalize(node);

        // Remove whitespace-only text nodes that are not significant in HTML.
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//text()";
        try {
            XMLDiffUtils.asList((NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET)).stream()
                .filter(this::isInsignificantWhitespaceOnlyTextNode).forEach(this::detachNode);
        } catch (XPathExpressionException e) {
            // This shouldn't happen.
        }
    }

    private boolean isInsignificantWhitespaceOnlyTextNode(Node node)
    {
        // Is whitespace-only text node ...
        return node.getNodeValue().trim().isEmpty()
            // ... and is not significant (either because of its parent or because of its siblings).
            && (TAGS_WITH_INSIGNIFICANT_WHITESPACE.contains(node.getParentNode().getNodeName().toLowerCase())
            || isBetweenBlockElements(node));
    }

    private boolean isBetweenBlockElements(Node node)
    {
        // Is not the only child ...
        return (node.getPreviousSibling() != null || node.getNextSibling() != null)
            // ... and either doesn't have a previous sibling or the previous sibling is a block element.
            && isBlockElementOrNull(node.getPreviousSibling())
            // ... and either doesn't have a next sibling or the next sibling is a block element.
            && isBlockElementOrNull(node.getNextSibling());
    }

    private boolean isBlockElementOrNull(Node node)
    {
        return node == null || BLOCK_ELEMENTS.contains(StringUtils.lowerCase(node.getNodeName()));
    }

    private void detachNode(Node node)
    {
        node.getParentNode().removeChild(node);
    }

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
