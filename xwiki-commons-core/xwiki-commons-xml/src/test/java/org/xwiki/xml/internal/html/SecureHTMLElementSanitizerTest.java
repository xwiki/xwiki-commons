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

import org.junit.jupiter.api.Test;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.xml.html.HTMLConstants;

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

    private static final String ONERROR = "onerror";

    @MockComponent
    private HTMLElementSanitizerConfiguration htmlElementSanitizerConfiguration;

    @InjectMockComponents
    private SecureHTMLElementSanitizer secureHTMLElementSanitizer;

    @BeforeComponent
    void setupMocks()
    {
        when(this.htmlElementSanitizerConfiguration.getForbidTags())
            .thenReturn(Collections.singletonList(HTMLConstants.TAG_A));
        when(this.htmlElementSanitizerConfiguration.getForbidAttributes())
            .thenReturn(Collections.singletonList(HTMLConstants.ATTRIBUTE_ALT));
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
        assertFalse(this.secureHTMLElementSanitizer.isElementAllowed(HTMLConstants.TAG_A));
    }

    @Test
    void forbiddenAttributes()
    {
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG,
            HTMLConstants.ATTRIBUTE_ALT, "XWiki"));
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
}
