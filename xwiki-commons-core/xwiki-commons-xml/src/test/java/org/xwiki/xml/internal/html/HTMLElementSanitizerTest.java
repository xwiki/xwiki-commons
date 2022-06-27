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

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.xml.html.HTMLConstants;
import org.xwiki.xml.html.HTMLElementSanitizer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Test the {@link SecureHTMLElementSanitizer}.
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HTMLElementSanitizerTest
{
    private static final String JAVASCRIPT_ALERT = "javascript:alert(1)";

    @InjectMockComponents
    private SecureHTMLElementSanitizer secureHTMLElementSanitizer;

    @InjectMockComponents
    private InsecureHTMLElementSanitizer insecureHTMLElementSanitizer;

    @Test
    void scriptIsDisallowedInSecureSanitizer()
    {
        assertFalse(this.secureHTMLElementSanitizer.isElementAllowed(HTMLConstants.TAG_SCRIPT));
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG, "onerror", "alert(1)"));
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_A,
            HTMLConstants.ATTRIBUTE_HREF, JAVASCRIPT_ALERT));
    }

    @ParameterizedTest
    @MethodSource("allSanitizersProvider")
    void dataAttributeIsAllowed(Supplier<HTMLElementSanitizer> htmlElementSanitizer)
    {
        assertTrue(htmlElementSanitizer.get().isAttributeAllowed(HTMLConstants.TAG_SPAN, "data-foo", JAVASCRIPT_ALERT));
    }

    @ParameterizedTest
    @MethodSource("allSanitizersProvider")
    void dataImgIsAllowed(Supplier<HTMLElementSanitizer> htmlElementSanitizer)
    {
        assertTrue(htmlElementSanitizer.get().isAttributeAllowed(HTMLConstants.TAG_IMG, HTMLConstants.ATTRIBUTE_SRC,
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4"
                + "  //8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg=="));
    }

    @ParameterizedTest
    @MethodSource("allSanitizersProvider")
    void linkProtocolsAreAllowed(Supplier<HTMLElementSanitizer> htmlElementSanitizer)
    {
        assertTrue(htmlElementSanitizer.get().isAttributeAllowed(HTMLConstants.TAG_A, HTMLConstants.ATTRIBUTE_HREF,
            "https://www.xwiki.org"));
        assertTrue(htmlElementSanitizer.get().isAttributeAllowed(HTMLConstants.TAG_A, HTMLConstants.ATTRIBUTE_HREF,
            "tel:+1234567890"));
    }

    @Test
    void arbitraryAttributesAreDisallowedInSecureSanitizer()
    {
        assertFalse(this.secureHTMLElementSanitizer.isElementAllowed("foo"));
        assertFalse(this.secureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_SPAN, "bar", "baz"));
    }

    @Test
    void arbitraryAttributesAreAllowedInInsecureSanitizer()
    {
        assertTrue(this.insecureHTMLElementSanitizer.isElementAllowed("xwiki"));
        assertTrue(this.insecureHTMLElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_SPAN, "hello", "world"));
    }

    private Arguments getSecureSanitizer()
    {
        // Use a supplier because the components haven't been injected yet when the arguments are collected.
        return arguments(Named.<Supplier<HTMLElementSanitizer>>of("secure", () -> this.secureHTMLElementSanitizer));
    }

    private Arguments getInsecureSanitizer()
    {
        return arguments(Named.<Supplier<HTMLElementSanitizer>>of("insecure", () -> this.insecureHTMLElementSanitizer));
    }

    Stream<Arguments> allSanitizersProvider()
    {
        return Stream.of(getSecureSanitizer(), getInsecureSanitizer());
    }
}
