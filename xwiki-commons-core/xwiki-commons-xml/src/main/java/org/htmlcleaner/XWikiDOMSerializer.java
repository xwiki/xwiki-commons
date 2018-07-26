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
package org.htmlcleaner;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;

/**
 * Generate a W3C Document from a SF's HTML Cleaner TagNode.
 *
 * Some code has been copy-pasted from SF's HTML Cleaner code (which is under a BDS license, see
 * http://htmlcleaner.sourceforge.net/license.php). Our goal is to remove this class completely if we can get SF's HTML
 * Cleaner to support the CDATA-related use cases that force us to have this class.
 *
 * Remove when the following issues have been fixed:
 * <ul>
 *     <li>https://sourceforge.net/p/htmlcleaner/bugs/169/</li>
 * </ul>
 *
 * Note: Even though in a public package this code is not meant to be a public API. We've had to put in under the {@code
 * org.htmlcleaner} package because of https://sourceforge.net/p/htmlcleaner/bugs/167/.
 *
 * @version $Id$
 * @since 1.8.2
 */
public class XWikiDOMSerializer
{
    /**
     * The Regex Pattern to recognize a CDATA block.
     */
    private static final Pattern CDATA_PATTERN = Pattern.compile("<!\\[CDATA\\[.*(\\]\\]>|<!\\[CDATA\\[)",
        Pattern.DOTALL);

    private static final String CSS_COMMENT_START = "/*";

    private static final String CSS_COMMENT_END = "*/";

    private static final String JS_COMMENT = "//";

    private static final String NEW_LINE = "\n";

    private static final String SCRIPT_TAG_NAME = "script";

    private static final String STYLE_TAG_NAME = "style";

    private static final String HTML_TAG_NAME = "html";

    /**
     * The HTML Cleaner properties set by the user to control the HTML cleaning.
     */
    private CleanerProperties props;

    /**
     * @param props the HTML Cleaner properties set by the user to control the HTML cleaning.
     */
    public XWikiDOMSerializer(CleanerProperties props)
    {
        this.props = props;
    }

    /**
     * @param documentDocumentBuilder the {@link DocumentBuilder} instance to use, DocumentBuilder is not garantied to
     * be thread safe so at most the safe instance should be used only in the same thread
     * @param rootNode the HTML Cleaner root node to serialize
     * @return the W3C Document object
     * @throws ParserConfigurationException if there's an error during serialization
     */
    public Document createDOM(DocumentBuilder documentDocumentBuilder, TagNode rootNode)
        throws ParserConfigurationException
    {
        DOMImplementation impl = documentDocumentBuilder.getDOMImplementation();

        // Copied from the source code of HTML Cleaner.

        Document document;

        //
        // Where a DOCTYPE is supplied in the input, ensure that this is in the output DOM. See issue #27
        //
        // Note that we may want to fix incorrect DOCTYPEs in future; there are some fairly
        // common patterns for errors with the older HTML4 doctypes.
        //
        if (rootNode.getDocType() != null) {
            String qualifiedName = rootNode.getDocType().getPart1();
            String publicId = rootNode.getDocType().getPublicId();
            String systemId = rootNode.getDocType().getSystemId();

            //
            // If there is no qualified name, set it to html. See bug #153.
            //
            if (qualifiedName == null) {
                qualifiedName = HTML_TAG_NAME;
            }

            DocumentType documentType = impl.createDocumentType(qualifiedName, publicId, systemId);

            //
            // While the qualified name is "HTML" for some DocTypes, we want the actual document root name to be "html".
            // See bug #116
            //
            if ("HTML".equals(qualifiedName)) {
                qualifiedName = HTML_TAG_NAME;
            }
            document = impl.createDocument(rootNode.getNamespaceURIOnPath(""), qualifiedName, documentType);
        } else {
            document = documentDocumentBuilder.newDocument();
            Element rootElement = document.createElement(rootNode.getName());
            document.appendChild(rootElement);
        }

        //
        // Copy across root node attributes - see issue 127. Thanks to rasifiel for the patch
        //
        Map<String, String> attributes = rootNode.getAttributes();
        Iterator<Map.Entry<String, String>> entryIterator = attributes.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, String> entry = entryIterator.next();
            String attrName = entry.getKey();
            String attrValue = entry.getValue();
            attrValue = Utils.escapeXml(attrValue, props, true);

            document.getDocumentElement().setAttribute(attrName, attrValue);

            //
            // Flag the attribute as an ID attribute if appropriate. Thanks to Chris173
            //
            if ("id".equalsIgnoreCase(attrName)) {
                document.getDocumentElement().setIdAttribute(attrName, true);
            }
        }

        createSubnodes(document, document.getDocumentElement(), rootNode.getAllChildren());

        return document;
    }

    /**
     * Perform CDATA transformations if the user has specified to use CDATA inside scripts and style elements.
     *
     * @param document the W3C Document to use for creating new DOM elements
     * @param element the W3C element to which we'll add the text content to
     * @param bufferedContent the buffered text content on which we need to perform the CDATA transformations
     * @param item the current HTML Cleaner node being processed
     */
    private void flushContent(Document document, Element element, StringBuilder bufferedContent, Object item)
    {
        if (bufferedContent.length() > 0 && !(item instanceof ContentNode)) {
            // Flush the buffered content
            boolean specialCase = this.props.isUseCdataForScriptAndStyle() && isScriptOrStyle(element);
            String content = bufferedContent.toString();

            if (!specialCase) {
                content = Utils.escapeXml(content, this.props, true);
            } else if (specialCase) {
                content = processCDATABlocks(content);
            }

            // Generate a javascript comment in front on the CDATA block so that it works in IE and when
            // serving XHTML under a mimetype of HTML.
            if (specialCase) {
                if (SCRIPT_TAG_NAME.equalsIgnoreCase(element.getNodeName())) {
                    // JS
                    element.appendChild(document.createTextNode(JS_COMMENT));
                    element.appendChild(document.createCDATASection(NEW_LINE + content + NEW_LINE + JS_COMMENT));
                } else {
                    // CSS
                    element.appendChild(document.createTextNode(CSS_COMMENT_START));
                    element.appendChild(document.createCDATASection(CSS_COMMENT_END + StringUtils.chomp(content)
                        + NEW_LINE + CSS_COMMENT_START));
                    element.appendChild(document.createTextNode(CSS_COMMENT_END));
                }
            } else {
                element.appendChild(document.createTextNode(content));
            }

            bufferedContent.setLength(0);
        }
    }

    /**
     * Remove any existing CDATA section and unencode HTML entities that are not inside a CDATA block.
     *
     * @param content the text input to transform
     * @return the transformed content that will be wrapped inside a CDATA block
     */
    private String processCDATABlocks(String content)
    {
        StringBuilder result = new StringBuilder();
        Matcher matcher = CDATA_PATTERN.matcher(content);
        int cursor = 0;
        while (matcher.find()) {
            result.append(StringEscapeUtils.unescapeHtml4(content.substring(cursor, matcher.start())));
            result.append(content.substring(matcher.start() + 9, matcher.end() - matcher.group(1).length()));
            cursor = matcher.end() - matcher.group(1).length() + 3;
        }
        // Copy the remaining text data in the result buffer
        if (cursor < content.length()) {
            result.append(StringEscapeUtils.unescapeHtml4(content.substring(cursor)));
        }
        // Ensure ther's no invalid <![CDATA[ or ]]> remaining.
        String contentResult = result.toString().replace("<![CDATA[", "").replace("]]>", "");

        return contentResult;
    }

    /**
     * @param element the element to check
     * @return true if the passed element is a script or style element
     */
    protected boolean isScriptOrStyle(Element element)
    {
        String tagName = element.getNodeName();
        return SCRIPT_TAG_NAME.equalsIgnoreCase(tagName) || STYLE_TAG_NAME.equalsIgnoreCase(tagName);
    }

    /**
     * Serialize a given SF HTML Cleaner node.
     *
     * @param document the W3C Document to use for creating new DOM elements
     * @param element the W3C element to which we'll add the subnodes to
     * @param tagChildren the SF HTML Cleaner nodes to serialize for that node
     */
    private void createSubnodes(Document document, Element element, List<? extends BaseToken> tagChildren)
    {
        // We've modified the original implementation based in SF's HTML Cleaner to better handle CDATA.
        // More specifically we want to handle the following 3 use cases:
        //
        // Use case 1: useCdata = true && input is:
        // <script>...<![CDATA[...]]>...</script>
        // In this case we must make sure to have only one CDATA block.
        //
        // Use case 2: useCdata = true && input is:
        // <script>...entities not encoded (e.g. "<")...</script>
        // We must generate a CDATA block around the whole content (the HTML Tokenizer split
        // ContentToken on "<" character so we need to join them before creating the CDATA block.
        // We must also unencode any entities (i.e. transform "&lt;" into "<") since we'll be
        // wrapping them in a CDATA section.
        //
        // Use case 3: useCData = false
        // Simply group all ContentToken together.

        StringBuilder bufferedContent = new StringBuilder();

        if (tagChildren != null) {
            for (Object item : tagChildren) {
                // Flush content tokens
                flushContent(document, element, bufferedContent, item);

                if (item instanceof CommentNode) {
                    CommentNode commentToken = (CommentNode) item;
                    Comment comment = document.createComment(commentToken.getContent());
                    element.appendChild(comment);
                } else if (item instanceof ContentNode) {
                    ContentNode contentToken = (ContentNode) item;
                    bufferedContent.append(contentToken.getContent());
                } else if (item instanceof TagNode) {
                    TagNode subTagNode = (TagNode) item;
                    Element subelement = document.createElement(subTagNode.getName());
                    Map<String, String> attributes = subTagNode.getAttributes();
                    for (Map.Entry<String, String> entry : attributes.entrySet()) {
                        String attrName = entry.getKey();
                        String attrValue = entry.getValue();
                        attrValue = Utils.escapeXml(attrValue, this.props, true);
                        subelement.setAttribute(attrName, attrValue);
                    }

                    // recursively create subnodes
                    createSubnodes(document, subelement, subTagNode.getAllChildren());

                    element.appendChild(subelement);
                } else if (item instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<BaseToken> sublist = (List<BaseToken>) item;
                    createSubnodes(document, element, sublist);
                }
            }
            flushContent(document, element, bufferedContent, null);
        }
    }
}
