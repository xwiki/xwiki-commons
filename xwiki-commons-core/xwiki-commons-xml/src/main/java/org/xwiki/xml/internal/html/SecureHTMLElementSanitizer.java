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
package org.xwiki.xml.internal.html;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.stability.Unstable;
import org.xwiki.xml.html.HTMLElementSanitizer;

/**
 * Secure default implementation of {@link HTMLElementSanitizer} based on a definition of allowed elements and
 * attributes.
 * <p>
 * This is heavily inspired by DOMPurify by Cure53 and other contributors | Released under the Apache license 2.0 and
 * Mozilla Public License 2.0 - <a href="https://github.com/cure53/DOMPurify/blob/main/LICENSE">LICENSE</a>.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Component
@Named(SecureHTMLElementSanitizer.HINT)
@Singleton
@Unstable
public class SecureHTMLElementSanitizer implements HTMLElementSanitizer, Initializable
{
    /**
     * The hint of this component.
     */
    public static final String HINT = "secure";

    static final Pattern IS_SCRIPT_OR_DATA = Pattern.compile("^(?:\\w+script|data):", Pattern.CASE_INSENSITIVE);

    static final Pattern ATTR_WHITESPACE =
        Pattern.compile("[\\u0000-\\u0020\\u00A0\\u1680\\u180E\\u2000-\\u2029\\u205F\\u3000]");

    /**
     * Pattern that matches valid data-attributes.
     * <p>
     * Following the <a href="https://html.spec.whatwg.org/multipage/dom.html
     #embedding-custom-non-visible-data-with-the-data-*-attributes">HTML standard</a>
     * this means that the name starts with "data-", has at least one character after the hyphen and is
     * <a href="https://html.spec.whatwg.org/multipage/infrastructure.html#xml-compatible">XML-compatible</a>,
     * i.e., matches the <a href="https://www.w3.org/TR/xml/#NT-Name">Name production</a> without ":".
     */
    static final Pattern DATA_ATTR = Pattern.compile("^data-[A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6"
        + "\\u00F8-\\u02ff\\u0370-\\u037d\\u037f-\\u1fff\\u200c\\u200d\\u2070-\\u218f\\u2c00-\\u2fef\\u3001-\\ud7ff"
        + "\\uf900-\\ufdcf\\ufdf0-\\ufffd\\x{10000}-\\x{EFFFF}\\-.0-9\\u00b7\\u0300-\\u036f\\u203f-\\u2040]+$");

    static final Pattern ARIA_ATTR = Pattern.compile("^aria-[\\-\\w]+$");

    static final Pattern IS_ALLOWED_URI = Pattern.compile("^(?:(?:f|ht)tps?|mailto|tel|callto|cid|xmpp):",
        Pattern.CASE_INSENSITIVE);

    static final Pattern IS_NO_URI = Pattern.compile("^(?:[^a-z]|[a-z+.\\-]+(?:[^a-z+.\\-:]|$))",
        Pattern.CASE_INSENSITIVE);

    @Inject
    private HTMLElementSanitizerConfiguration htmlElementSanitizerConfiguration;

    @Inject
    private HTMLDefinitions htmlDefinitions;

    @Inject
    private SVGDefinitions svgDefinitions;

    @Inject
    private MathMLDefinitions mathMLDefinitions;

    /**
     * Additionally allowed elements.
     */
    private final Set<String> extraAllowedTags;

    /**
     * Additionally allowed attributes.
     */
    private final Set<String> extraAllowedAttributes;

    /**
     * XML attributes that should be allowed.
     */
    private final Set<String> xmlAttributes;

    /**
     * Tags that are safe for data: URIs.
     */
    private final Set<String> dataUriTags;

    /**
     * Attributes safe for values like "javascript:".
     */
    private final Set<String> uriSafeAttributes;

    private final Set<String> forbidTags;

    private final Set<String> forbidAttributes;

    private boolean allowUnknownProtocols;

    private Pattern allowedUriPattern;

    /**
     * Default constructor.
     */
    public SecureHTMLElementSanitizer()
    {
        this.dataUriTags = new HashSet<>(Arrays.asList("audio", "video", "img", "source", "image", "track"));

        this.uriSafeAttributes = new HashSet<>(
            Arrays.asList("alt", "class", "for", "id", "label", "name", "pattern", "placeholder", "role", "summary",
                "title", "value", "style", "xmlns"));

        this.xmlAttributes =
            new HashSet<>(Arrays.asList("xlink:href", "xml:id", "xlink:title", "xml:space", "xmlns:xlink"));

        this.extraAllowedTags = new HashSet<>();

        this.extraAllowedAttributes = new HashSet<>();

        this.forbidTags = new HashSet<>();

        this.forbidAttributes = new HashSet<>();

        this.allowedUriPattern = IS_ALLOWED_URI;
    }

    @Override
    public void initialize() throws InitializationException
    {
        this.extraAllowedTags.addAll(this.htmlElementSanitizerConfiguration.getExtraAllowedTags());
        this.extraAllowedAttributes.addAll(this.htmlElementSanitizerConfiguration.getExtraAllowedAttributes());
        this.uriSafeAttributes.addAll(this.htmlElementSanitizerConfiguration.getExtraUriSafeAttributes());
        this.dataUriTags.addAll(this.htmlElementSanitizerConfiguration.getExtraDataUriTags());
        this.allowUnknownProtocols = this.htmlElementSanitizerConfiguration.isAllowUnknownProtocols();
        this.forbidTags.addAll(this.htmlElementSanitizerConfiguration.getForbidTags());
        this.forbidAttributes.addAll(this.htmlElementSanitizerConfiguration.getForbidAttributes());
        String configuredRegexp = this.htmlElementSanitizerConfiguration.getAllowedUriRegexp();
        if (StringUtils.isNotBlank(configuredRegexp)) {
            this.allowedUriPattern = Pattern.compile(configuredRegexp, Pattern.CASE_INSENSITIVE);
        }
    }

    @Override
    public boolean isElementAllowed(String elementName)
    {
        return !this.forbidTags.contains(elementName)
            && (this.extraAllowedTags.contains(elementName) || isElementSafe(elementName));
    }

    private boolean isElementSafe(String elementName)
    {
        return this.htmlDefinitions.isSafeTag(elementName) || this.svgDefinitions.isSafeTag(elementName)
            || this.mathMLDefinitions.isSafeTag(elementName);
    }

    @Override
    public boolean isAttributeAllowed(String elementName, String attributeName, String attributeValue)
    {
        boolean result = false;

        String lowerElement = elementName.toLowerCase();
        String lowerAttribute = attributeName.toLowerCase();

        if ((DATA_ATTR.matcher(lowerAttribute).matches() || ARIA_ATTR.matcher(lowerAttribute).matches())
            && !this.forbidAttributes.contains(lowerAttribute))
        {
            result = true;
        } else if (isAttributeAllowed(lowerAttribute) && !this.forbidAttributes.contains(lowerAttribute)) {
            result = isAllowedValue(lowerElement, lowerAttribute, attributeValue);
        }

        return result;
    }

    private boolean isAllowedValue(String lowercaseElementName, String lowercaseAttributeName, String attributeValue)
    {
        // Break into several statements to avoid too long boolean expression.
        boolean result = StringUtils.isBlank(attributeValue);
        if (!result) {
            String valueNoWhitespace = ATTR_WHITESPACE.matcher(attributeValue).replaceAll("");
            result = this.uriSafeAttributes.contains(lowercaseAttributeName);
            result = result || IS_NO_URI.matcher(valueNoWhitespace).find();
            result = result || this.allowedUriPattern.matcher(valueNoWhitespace).find();
            result = result || isAllowedDataValue(lowercaseElementName, lowercaseAttributeName, attributeValue);
            result = result || (this.allowUnknownProtocols && !isScriptOrData(attributeValue));
        }
        return result;
    }

    private boolean isAttributeAllowed(String attributeName)
    {
        boolean result = this.extraAllowedAttributes.contains(attributeName);
        result = result || this.htmlDefinitions.isAllowedAttribute(attributeName);
        result = result || this.svgDefinitions.isAllowedAttribute(attributeName);
        result = result || this.mathMLDefinitions.isAllowedAttribute(attributeName);
        result = result || this.xmlAttributes.contains(attributeName);
        return result;
    }

    private boolean isScriptOrData(String attributeValue)
    {
        return IS_SCRIPT_OR_DATA.matcher(ATTR_WHITESPACE.matcher(attributeValue).replaceAll("")).find();
    }

    private boolean isAllowedDataValue(String elementName, String attributeName, String attributeValue)
    {
        boolean attributeAllowsData = "src".equals(attributeName) || "xlink:href".equals(attributeName)
            || "href".equals(attributeName);
        return attributeAllowsData && !"script".equals(elementName) && attributeValue.startsWith("data:")
            && this.dataUriTags.contains(elementName);
    }
}
