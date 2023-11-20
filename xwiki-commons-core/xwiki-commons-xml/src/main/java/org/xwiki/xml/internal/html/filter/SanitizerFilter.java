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
        public static final TagInformation INVALID = new TagInformation(null, null);

        public final String tagName;

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

            if (node.getNodeType() == Node.ELEMENT_NODE && node instanceof Element) {
                Element element = (Element) node;

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
     * Tries to follow the logic in DOMPurify by Cure53 and other contributors | Released under the Apache license
     * 2.0 and Mozilla Public License 2.0 - <a href="https://github.com/cure53/DOMPurify/blob/main/LICENSE">LICENSE</a>.
     *
     * @param element the element to check
     * @param parentTag the information of the parent tag
     * @return the tag information of the current tag or {@link TagInformation#INVALID} if the element must not be
     * there
     */
    private TagInformation checkNamespace(Element element, TagInformation parentTag)
    {
        TagInformation result = TagInformation.INVALID;

        // Stay in parent SVG/MathML namespace if the current element clearly belongs to the parent namespace.
        if (SVG_NAMESPACE.equals(parentTag.namespace) && isPureSVGTag(element.getTagName(), parentTag)) {
            result = new TagInformation(element.getTagName(), SVG_NAMESPACE);
        } else if (MATHML_NAMESPACE.equals(parentTag.namespace)
            && this.mathMLDefinitions.isMathMLTag(element.getTagName()))
        {
            result = new TagInformation(element.getTagName(), MATHML_NAMESPACE);
        } else if (areHTMLChildrenAllowed(parentTag)) {
            // If HTML children are allowed, only allow the element if is actually an HTML element or the root
            // element of MathML/SVG.
            if ("math".equals(element.getTagName())) {
                result = new TagInformation(element.getTagName(), MATHML_NAMESPACE);
            } else if ("svg".equals(element.getTagName())) {
                result = new TagInformation(element.getTagName(), SVG_NAMESPACE);
            } else if (isPossiblyHtmlTag(element.getTagName())) {
                result = new TagInformation(element.getTagName(), HTML_NAMESPACE);
            }
        }
        return result;
    }

    /**
     * @param tagName the tag name to check
     * @param parentTag the parent information
     * @return if the tag is an SVG tag and not also an HTML tag that is nested in an HTML integration point in SVG
     */
    private boolean isPureSVGTag(String tagName, TagInformation parentTag)
    {
        return this.svgDefinitions.isSVGTag(tagName) && (
            !this.svgDefinitions.isHTMLIntegrationPoint(parentTag.tagName)
                || !this.svgDefinitions.isCommonHTMLElement(tagName));
    }

    private boolean areHTMLChildrenAllowed(TagInformation parent)
    {
        boolean result = HTML_NAMESPACE.equals(parent.namespace);
        result = result || (SVG_NAMESPACE.equals(parent.namespace)
            && this.svgDefinitions.isHTMLIntegrationPoint(parent.tagName));
        result = result || (MATHML_NAMESPACE.equals(parent.namespace)
            && this.mathMLDefinitions.isTextOrHTMLIntegrationPoint(parent.tagName));
        return result;
    }

    /**
     * @param tagName the tag name to check
     * @return if the given tag is neither a MathML tag nor an SVG tag that is also an HTML tag
     */
    private boolean isPossiblyHtmlTag(String tagName)
    {
        return !this.mathMLDefinitions.isMathMLTag(tagName)
            && (!this.svgDefinitions.isSVGTag(tagName) || this.svgDefinitions.isCommonHTMLElement(tagName));
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
