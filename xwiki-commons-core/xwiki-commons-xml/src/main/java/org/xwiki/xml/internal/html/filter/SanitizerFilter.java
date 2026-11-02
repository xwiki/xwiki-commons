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
/*
 * Alternatively, at your choice, the contents of this file may be used under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.xwiki.xml.internal.html.filter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLElementSanitizer;
import org.xwiki.xml.html.filter.AbstractHTMLFilter;
import org.xwiki.xml.internal.html.MathMLDefinitions;
import org.xwiki.xml.internal.html.SVGDefinitions;

/**
 * Sanitizer that sanitizes the document.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Component
@Named("sanitizer")
@Singleton
public class SanitizerFilter extends AbstractHTMLFilter
{
    private static final String MATHML_NAMESPACE = "http://www.w3.org/1998/Math/MathML";

    private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";

    private static final String HTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

    private static final String MATH = "math";

    private static final String SVG = "svg";

    private static final String ANNOTATION_XML = "annotation-xml";

    @Inject
    protected HTMLElementSanitizer htmlElementSanitizer;

    @Inject
    private SVGDefinitions svgDefinitions;

    @Inject
    private MathMLDefinitions mathMLDefinitions;

    @Override
    public void filter(Document document, Map<String, String> cleaningParameters)
    {
        String restricted = cleaningParameters.get(HTMLCleanerConfiguration.RESTRICTED);
        if ("true".equalsIgnoreCase(restricted)) {
            cleanDocument(document.getDocumentElement());
        }
    }

    protected static class TagInformation
    {
        /**
         * Placeholder for a tag that is either unknown or used in the wrong namespace.
         */
        public static final TagInformation INVALID = new TagInformation(null, null);

        /**
         * The name of the tag.
         */
        public final String tagName;

        /**
         * The namespace of the tag.
         */
        public final String namespace;

        /**
         * Default constructor.
         *
         * @param tagName the name of the tag
         * @param namespace the namespace of the tag
         */
        TagInformation(String tagName, String namespace)
        {
            this.tagName = tagName;
            this.namespace = namespace;
        }
    }

    private void cleanDocument(Element rootElement)
    {
        List<Element> elementsToRemove = new ArrayList<>();
        traverseWithNamespace(rootElement, (element, currentNamespace) -> {
            if (currentNamespace == TagInformation.INVALID
                || !this.htmlElementSanitizer.isElementAllowed(element.getTagName()))
            {
                elementsToRemove.add(element);
                return true;
            } else {
                getAttributes(element).stream()
                    .filter(
                        attr -> !this.htmlElementSanitizer.isAttributeAllowed(element.getTagName(), attr.getName(),
                            attr.getValue())
                    )
                    .forEach(element::removeAttributeNode);
                return false;
            }
        });

        elementsToRemove.forEach(element -> element.getParentNode().removeChild(element));
    }

    protected void traverseWithNamespace(Element rootElement, BiPredicate<Element, TagInformation> traversal)
    {
        traverseWithNamespace(rootElement, traversal, node -> false);
    }

    protected void traverseWithNamespace(Element rootElement, BiPredicate<Element, TagInformation> traversal,
        Predicate<Node> commentHandler)
    {
        Node node = rootElement;

        Deque<TagInformation> parentNamespace = new ArrayDeque<>();
        TagInformation currentNamespace = new TagInformation("html", HTML_NAMESPACE);
        parentNamespace.push(currentNamespace);

        while (node != null) {
            boolean skipChildren = false;

            if (node.getNodeType() == Node.ELEMENT_NODE && node instanceof Element element) {
                currentNamespace = checkNamespace(element, parentNamespace.peek());
                skipChildren = traversal.test(element, currentNamespace);
            } else if (node.getNodeType() == Node.COMMENT_NODE) {
                skipChildren = commentHandler.test(node);
            }

            if (node.getFirstChild() != null && !skipChildren) {
                node = node.getFirstChild();
                parentNamespace.push(currentNamespace);
            } else {
                while (node.getNextSibling() == null && node != rootElement) {
                    node = node.getParentNode();
                    currentNamespace = parentNamespace.pop();
                }

                node = node.getNextSibling();
            }
        }
    }

    /**
     * Computes the namespace of the current element if it is allowed.
     * <p>
     * The namespace is computed like browsers compute it when parsing the serialized HTML, and then checked
     * following the logic in DOMPurify by Cure53 and other contributors | Released under the Apache license 2.0 and
     * Mozilla Public License 2.0 - <a href="https://github.com/cure53/DOMPurify/blob/main/LICENSE">LICENSE</a>.
     *
     * @param element the element to check
     * @param parentTag the information of the parent tag
     * @return the tag information of the current tag or {@link TagInformation#INVALID} if the element must not be
     * there
     */
    private TagInformation checkNamespace(Element element, TagInformation parentTag)
    {
        TagInformation result = TagInformation.INVALID;

        String tagName = element.getTagName();
        String namespace = getParsedNamespace(tagName, parentTag);
        if (isValidNamespace(tagName, namespace, parentTag)) {
            result = new TagInformation(tagName, namespace);
        }

        return result;
    }

    /**
     * Computes the namespace that browsers assign to the element when parsing it as HTML, following the tree
     * construction rules of the <a href="https://html.spec.whatwg.org/multipage/parsing.html#tree-construction">HTML
     * standard</a>.
     *
     * @param tagName the name of the element
     * @param parentTag the information of the parent tag
     * @return the namespace of the element
     */
    private String getParsedNamespace(String tagName, TagInformation parentTag)
    {
        String result;

        if (isParsedAsHTML(tagName, parentTag)) {
            // The only ways to start foreign content are the math and svg elements.
            if (MATH.equals(tagName)) {
                result = MATHML_NAMESPACE;
            } else if (SVG.equals(tagName)) {
                result = SVG_NAMESPACE;
            } else {
                result = HTML_NAMESPACE;
            }
        } else {
            // Inside foreign content, elements stay in the namespace of their parent. Elements that would make the
            // parser leave the foreign content aren't SVG or MathML elements and are thus rejected later.
            result = parentTag.namespace;
        }

        return result;
    }

    /**
     * @param tagName the name of the element
     * @param parentTag the information of the parent tag
     * @return {@code true} if browsers parse the element using the rules for HTML content, i.e., if the parent is an
     *     HTML element or an integration point that allows the element
     */
    private boolean isParsedAsHTML(String tagName, TagInformation parentTag)
    {
        boolean result;

        if (SVG_NAMESPACE.equals(parentTag.namespace)) {
            result = this.svgDefinitions.isHTMLIntegrationPoint(parentTag.tagName);
        } else if (MATHML_NAMESPACE.equals(parentTag.namespace)) {
            result = (this.mathMLDefinitions.isTextIntegrationPoint(parentTag.tagName)
                && !this.mathMLDefinitions.isMathMLInTextIntegrationPoint(tagName))
                || (ANNOTATION_XML.equals(parentTag.tagName) && SVG.equals(tagName));
        } else {
            result = true;
        }

        return result;
    }

    /**
     * Checks if the element is allowed in the given namespace, following {@code _checkValidNamespace} in DOMPurify.
     * Like DOMPurify, this doesn't allow HTML content in SVG, not even in SVG elements that are HTML integration
     * points.
     *
     * @param tagName the name of the element
     * @param namespace the namespace of the element
     * @param parentTag the information of the parent tag
     * @return {@code true} if the element is allowed in the namespace
     */
    private boolean isValidNamespace(String tagName, String namespace, TagInformation parentTag)
    {
        boolean result;

        if (SVG_NAMESPACE.equals(namespace)) {
            result = isValidSVGElement(tagName, parentTag);
        } else if (MATHML_NAMESPACE.equals(namespace)) {
            result = isValidMathMLElement(tagName, parentTag);
        } else {
            result = isValidHTMLElement(tagName, parentTag);
        }

        return result;
    }

    private boolean isValidSVGElement(String tagName, TagInformation parentTag)
    {
        boolean result;

        if (HTML_NAMESPACE.equals(parentTag.namespace)) {
            // The only way to switch from HTML to SVG is the svg element.
            result = SVG.equals(tagName);
        } else if (MATHML_NAMESPACE.equals(parentTag.namespace)) {
            // The only way to switch from MathML to SVG is the svg element inside annotation-xml or a text
            // integration point.
            result = SVG.equals(tagName) && (ANNOTATION_XML.equals(parentTag.tagName)
                || this.mathMLDefinitions.isTextIntegrationPoint(parentTag.tagName));
        } else {
            result = this.svgDefinitions.isSVGTag(tagName);
        }

        return result;
    }

    private boolean isValidMathMLElement(String tagName, TagInformation parentTag)
    {
        boolean result;

        if (HTML_NAMESPACE.equals(parentTag.namespace)) {
            // The only way to switch from HTML to MathML is the math element.
            result = MATH.equals(tagName);
        } else if (SVG_NAMESPACE.equals(parentTag.namespace)) {
            // Switching from SVG to MathML would require an HTML integration point, which aren't allowed in SVG.
            result = false;
        } else {
            result = this.mathMLDefinitions.isMathMLTag(tagName);
        }

        return result;
    }

    private boolean isValidHTMLElement(String tagName, TagInformation parentTag)
    {
        boolean result;

        if (SVG_NAMESPACE.equals(parentTag.namespace)) {
            // HTML integration points aren't allowed in SVG.
            result = false;
        } else if (MATHML_NAMESPACE.equals(parentTag.namespace)
            && !this.mathMLDefinitions.isTextIntegrationPoint(parentTag.tagName))
        {
            // The only way to switch from MathML to HTML is a text integration point.
            result = false;
        } else {
            // Disallow elements that only exist in MathML or SVG and should never appear in HTML.
            result = !this.mathMLDefinitions.isMathMLTag(tagName)
                && (!this.svgDefinitions.isSVGTag(tagName) || this.svgDefinitions.isCommonHTMLElement(tagName));
        }

        return result;
    }

    protected List<Attr> getAttributes(Element element)
    {
        NamedNodeMap attributeNodes = element.getAttributes();
        List<Attr> result = new ArrayList<>();

        for (int i = 0, length = attributeNodes.getLength(); i < length; ++i) {
            result.add((Attr) attributeNodes.item(i));
        }

        return result;
    }
}
