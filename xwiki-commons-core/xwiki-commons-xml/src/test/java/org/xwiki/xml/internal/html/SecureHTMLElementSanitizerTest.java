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
package org.xwiki.xml.internal.html;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.xml.html.HTMLConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link SecureHTMLElementSanitizer}.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@ComponentTest
@ComponentList({
    HTMLDefinitions.class,
    MathMLDefinitions.class,
    SVGDefinitions.class
})
class SecureHTMLElementSanitizerTest
{
    private static final String ALLOWED_ATTRIBUTE = "allowed_attribute";

    private static final String BLINK = "blink";

    private static final String GET_ELEMENT_BY_ID = "getElementById";

    private static final String DIV = "div";

    private static final String IMAGE = "image";

    private static final String MAP = "map";

    private static final String HEADERS = "headers";

    private static final String SECTION = "section";

    private static final String SPAN = "span";

    private static final String ONERROR = "onerror";

    private static final String XWIKI = "XWiki";

    @MockComponent
    private HTMLElementSanitizerConfiguration htmlElementSanitizerConfiguration;

    @InjectMockComponents
    private SecureHTMLElementSanitizer secureHTMLElementSanitizer;

    @BeforeComponent
    void setupMocks()
    {
        when(this.htmlElementSanitizerConfiguration.getForbidTags())
            .thenReturn(Collections.singletonList(BLINK));
        when(this.htmlElementSanitizerConfiguration.getForbidAttributes())
            .thenReturn(Collections.singletonList(HTMLConstants.ATTRIBUTE_ALT));
        // "name" is loosened to additionally allow "div" and "span", while "headers" (allowed everywhere by default)
        // is restricted to "section".
        when(this.htmlElementSanitizerConfiguration.getExtraElementRestrictedAttributes())
            .thenReturn(Map.of(
                HTMLConstants.ATTRIBUTE_NAME, Set.of(DIV, SPAN),
                HEADERS, Set.of(SECTION)));
        when(this.htmlElementSanitizerConfiguration.getExtraAllowedTags())
            .thenReturn(Collections.singletonList(HTMLConstants.TAG_SCRIPT));
        when(this.htmlElementSanitizerConfiguration.getExtraAllowedAttributes())
            .thenReturn(Arrays.asList(ALLOWED_ATTRIBUTE, ONERROR));
        when(this.htmlElementSanitizerConfiguration.getExtraUriSafeAttributes())
            .thenReturn(Collections.singletonList(HTMLConstants.ATTRIBUTE_SRC));
        when(this.htmlElementSanitizerConfiguration.getExtraDataUriTags())
            .thenReturn(Arrays.asList(HTMLConstants.TAG_SCRIPT, HTMLConstants.TAG_NAV));
        when(this.htmlElementSanitizerConfiguration.isAllowUnknownProtocols())
            .thenReturn(false);
        when(this.htmlElementSanitizerConfiguration.getAllowedUriRegexp())
            .thenReturn("^(xwiki|https):");
    }

    @Test
    void forbiddenTags()
    {
        assertFalse(this.secureHTMLElementSanitizer.isElementAllowed(BLINK));
    }

    @ParameterizedTest
    @ValueSource(strings = { "form", "input", "select", "textarea", "button" })
    void formTags(String tagName)
    {
        assertFalse(this.secureHTMLElementSanitizer.isElementAllowed(tagName));
    }

    @Test
    void forbiddenAttributes()
    {
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG,
            HTMLConstants.ATTRIBUTE_ALT, XWIKI));
    }

    @Test
    void extraAllowedTags()
    {
        assertTrue(this.secureHTMLElementSanitizer.isElementAllowed(HTMLConstants.TAG_SCRIPT));
    }

    @Test
    void extraAllowedAttributes()
    {
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG, ALLOWED_ATTRIBUTE,
            "value"));
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG, ONERROR, "alert(1)"));
    }

    @Test
    void extraUriSafeAttributes()
    {
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG,
            HTMLConstants.ATTRIBUTE_SRC, "javascript:alert(1)"));
    }

    @Test
    void extraDataUriTags()
    {
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_NAV,
            HTMLConstants.ATTRIBUTE_HREF, "data:test"));
        // Script cannot be enabled for data-attributes.
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_SCRIPT,
            HTMLConstants.ATTRIBUTE_HREF, "data:script"));
    }

    @Test
    void restrictedURIs()
    {
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_A,
            HTMLConstants.ATTRIBUTE_HREF, "https://www.xwiki.org"));
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_A,
            HTMLConstants.ATTRIBUTE_HREF, "xwiki:test"));
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_A,
            HTMLConstants.ATTRIBUTE_HREF, "http://example.com"));
    }

    @Test
    void defaultElementRestrictedAttributes()
    {
        // By default, "name" is only allowed on "a" and "map".
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_A,
            HTMLConstants.ATTRIBUTE_NAME, XWIKI));
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(MAP, HTMLConstants.ATTRIBUTE_NAME, XWIKI));
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG,
            HTMLConstants.ATTRIBUTE_NAME, GET_ELEMENT_BY_ID));
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(IMAGE, HTMLConstants.ATTRIBUTE_NAME,
            GET_ELEMENT_BY_ID));
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG,
            HTMLConstants.ATTRIBUTE_NAME, ""));
        // Other attributes such as "id" remain allowed on any element.
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG,
            HTMLConstants.ATTRIBUTE_ID, GET_ELEMENT_BY_ID));
    }

    @Test
    void configuredElementRestrictedAttributes()
    {
        // "name" is additionally allowed on "div" and "span" through the configuration, without losing the built-in
        // elements.
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(DIV, HTMLConstants.ATTRIBUTE_NAME, XWIKI));
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(SPAN, HTMLConstants.ATTRIBUTE_NAME, XWIKI));
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_A,
            HTMLConstants.ATTRIBUTE_NAME, XWIKI));
        // Unrelated attributes on the configured element stay allowed.
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(DIV, HTMLConstants.ATTRIBUTE_ID, XWIKI));
        // "headers" is restricted to "section" through the configuration, even though it is allowed everywhere by
        // default.
        assertTrue(this.secureHTMLElementSanitizer.isAttributeAllowed(SECTION, HEADERS, XWIKI));
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(DIV, HEADERS, XWIKI));
    }

    @ParameterizedTest
    @CsvSource({
        "data-, false",
        "data-a, true",
        "data-x-wiki.test_\u0192, true",
        "data-x\u2713, false",
        "data-x/test, false",
        "data-x>test, false",
        "data-x:y, false"
    })
    void dataAttributes(String attribute, boolean accepted)
    {
        assertEquals(accepted, this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_DIV, attribute,
            "hello"));
    }
}
