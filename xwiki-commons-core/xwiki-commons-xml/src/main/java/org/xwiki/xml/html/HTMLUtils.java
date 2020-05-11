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
package org.xwiki.xml.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.DocType;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * HTML Utility methods.
 *
 * @version $Id$
 * @since 1.8.3
 */
// TODO: Create a separate class for each HTML version (XHTML 1.0, HTML5, etc...)
public final class HTMLUtils
{
    /**
     * In HTML5, some elements must be expanded (for example {@code <span></span>} instead of {@code <span />}), and
     * some others must not (for example {@code <br />} instead of {@code <br></br>}. Thus for the list of elements
     * below we need special handling (not expanding).
     */
    private static final List<String> OMIT_ELEMENT_EXPANDING_SET = Arrays.asList(
        "area", "base", "br", "col", "hr", "img", "input", "link", "meta", "param");

    /**
     * JDOM's XMLOutputter class converts reserved XML character ({@code &}) into its entity
     * format {@code &amp;}. However since we're using HTML Cleaner
     * (http://htmlcleaner.sourceforge.net/) and since it's buggy for unicode character escapes we have turned off
     * character escaping for it and thus we need to perform selective escaping here.
     * <p>
     * Moreover, since we support HTML5, we need to
     * expand empty elements on some elements and not on the others. For example: {@code <span></span>} is valid
     * meanwhile:
     * <pre>{@code
     * <br>
     * </br>}</pre>
     * is not. See {@code OMIT_ELEMENT_EXPANDING_SET} for the list of elements to not expand.
     */
    // TODO: Remove the complex escaping code when SF HTML Cleaner will do proper escaping
    public static class XWikiXMLOutputter extends XMLOutputter
    {
        /**
         * Regex for a character reference as defined in:
         * https://www.w3.org/TR/WD-xml-lang-970630#dt-charref.
         */
        private static final String CHARACTER_REFERENCE = "&amp;#[0-9]+;|&amp;#x[0-9a-fA-F]+;";

        /**
         * Regex to recognize a character reference Entity.
         */
        private static final Pattern CHARACTER_REFERENCE_PATTERN = Pattern.compile(CHARACTER_REFERENCE);

        /**
         * Escaped ampersand character.
         */
        private static final String AMPERSAND = "&amp;";

        /**
         * Whether to omit the document type when printing the W3C Document or not.
         */
        private boolean omitDocType;

        /**
         * @param format the JDOM class used to control output formats, see {@link org.jdom.output.Format}
         * @param omitDocType if true then omit the document type when printing the W3C Document
         * @see XMLOutputter#XMLOutputter(Format)
         */
        public XWikiXMLOutputter(Format format, boolean omitDocType)
        {
            super(format);
            this.omitDocType = omitDocType;
        }

        @Override
        public String escapeElementEntities(String text)
        {
            String result = super.escapeElementEntities(text);

            // "\r" characters are automatically transformed in &#xD; but we want to keep the original \r there.
            return cleanAmpersandEscape(result).replaceAll("&#xD;", "\r");
        }

        @Override
        public String escapeAttributeEntities(String text)
        {
            String result = super.escapeAttributeEntities(text);
            return cleanAmpersandEscape(result);
        }

        /**
         * Remove ampersand escapes when it's not needed (i.e. on character entities).
         *
         * @param text the text escaped.
         * @return the text cleaned
         */
        private String cleanAmpersandEscape(String text)
        {
            StringBuilder buffer = new StringBuilder(text);
            // find all occurrences of &
            int pos = buffer.indexOf(AMPERSAND);
            while (pos > -1 && pos < buffer.length()) {
                // Check if the & is a character entity
                Matcher matcher = CHARACTER_REFERENCE_PATTERN.matcher(buffer.substring(pos));
                if (matcher.lookingAt()) {
                    // We've found an entity, replace the "&amp;" by a single "&"
                    buffer.replace(pos, pos + 5, "&");
                    pos = pos + matcher.end() - matcher.start();
                } else {
                    // don't do anything, just move on.
                    pos += 5;
                }
                pos = buffer.indexOf(AMPERSAND, pos);
            }
            return buffer.toString();
        }

        @Override
        protected void printDocType(Writer out, DocType docType) throws IOException
        {
            if (!this.omitDocType) {
                super.printDocType(out, docType);
            }
        }

        @Override
        protected void printElement(Writer out, Element element, int level, NamespaceStack namespaces)
            throws IOException
        {
            // We override the code from the super class to not expand some empty elements.
            boolean currentFormatPolicy = currentFormat.getExpandEmptyElements();
            try {
                String elementName = element.getName();
                for (String name : OMIT_ELEMENT_EXPANDING_SET) {
                    if (name.equals(elementName)) {
                        // We do not expand this empty element
                        currentFormat.setExpandEmptyElements(false);
                        break;
                    }
                }

                // Call the method from the super class
                super.printElement(out, element, level, namespaces);

            } finally {
                // Reset the format
                currentFormat.setExpandEmptyElements(currentFormatPolicy);
            }
        }
    }

    /**
     * Private constructor since this is a utility class that shouldn't be instantiated (all methods are static).
     */
    private HTMLUtils()
    {
        // Nothing to do
    }

    /**
     * @param document the W3C Document to transform into a String
     * @return the XML as a String
     */
    public static String toString(Document document)
    {
        return HTMLUtils.toString(document, false, false);
    }

    /**
     * @param document the W3C Document to transform into a String
     * @param omitDeclaration whether the XML declaration should be printed or not
     * @param omitDoctype whether the document type should be printed or not
     * @return the XML as a String
     */
    public static String toString(Document document, boolean omitDeclaration, boolean omitDoctype)
    {
        // Note: We don't use javax.xml.transform.Transformer since it prints our valid XHTML as HTML which is not
        // XHTML compliant. For example it transforms our "<hr/>" into "<hr>.
        DOMBuilder builder = new DOMBuilder();
        org.jdom.Document jdomDoc = builder.build(document);

        Format format = Format.getRawFormat();
        // Force newlines to use \n since otherwise the default is \n\r.
        // See http://www.jdom.org/docs/apidocs/org/jdom/output/Format.html#setLineSeparator(java.lang.String)
        format.setLineSeparator("\n");

        // Make sure all elements are expanded so that they can also be rendered fine in browsers that only support
        // HTML.
        format.setExpandEmptyElements(true);

        format.setOmitDeclaration(omitDeclaration);

        XMLOutputter outputter = new XWikiXMLOutputter(format, omitDoctype);

        return outputter.outputString(jdomDoc);
    }

    /**
     * Strip the HTML envelope if it exists. Precisely this means removing the head tag and move all tags in the body
     * tag directly under the html element. This is useful for example if you wish to insert an HTML fragment into an
     * existing HTML page.
     *
     * @param document the w3c Document to strip
     */
    public static void stripHTMLEnvelope(Document document)
    {
        org.w3c.dom.Element root = document.getDocumentElement();
        if (root.getNodeName().equalsIgnoreCase(HTMLConstants.TAG_HTML)) {
            // Look for a head element below the root element and for a body element
            Node bodyNode = null;
            Node headNode = null;
            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeName().equalsIgnoreCase(HTMLConstants.TAG_HEAD)) {
                    headNode = node;
                } else if (node.getNodeName().equalsIgnoreCase(HTMLConstants.TAG_BODY)) {
                    bodyNode = node;
                }
            }

            if (headNode != null) {
                root.removeChild(headNode);
            }

            if (bodyNode != null) {
                // Move all children of body node under the root element
                NodeList bodyChildrenNodes = bodyNode.getChildNodes();
                while (bodyChildrenNodes.getLength() > 0) {
                    root.insertBefore(bodyChildrenNodes.item(0), null);
                }
                root.removeChild(bodyNode);
            }
        }
    }

    /**
     * Remove the first element inside a parent element and copy the element's children in the parent.
     *
     * @param document the w3c document from which to remove the top level paragraph
     * @param parentTagName the name of the parent tag to look under
     * @param elementTagName the name of the first element to remove
     */
    public static void stripFirstElementInside(Document document, String parentTagName, String elementTagName)
    {
        NodeList parentNodes = document.getElementsByTagName(parentTagName);
        if (parentNodes.getLength() > 0) {
            Node parentNode = parentNodes.item(0);
            // Look for a p element below the first parent element
            Node pNode = parentNode.getFirstChild();
            if (elementTagName.equalsIgnoreCase(pNode.getNodeName())) {
                // Move all children of p node under the root element
                NodeList pChildrenNodes = pNode.getChildNodes();
                while (pChildrenNodes.getLength() > 0) {
                    parentNode.insertBefore(pChildrenNodes.item(0), null);
                }
                parentNode.removeChild(pNode);
            }
        }
    }
}
