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

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HTMLElementSanitizerConfiguration}.
 *
 * @version $Id$
 */
@ComponentTest
class HTMLElementSanitizerConfigurationTest
{
    private static final String EXTRA_ELEMENT_RESTRICTED_ATTRIBUTES_CONFIGURATION =
        "xml.htmlElementSanitizer.extraElementRestrictedAttributes";

    @MockComponent
    @Named("restricted")
    private ConfigurationSource configurationSource;

    @InjectMockComponents
    private HTMLElementSanitizerConfiguration configuration;

    private void setExtraElementRestrictedAttributes(Properties value)
    {
        when(this.configurationSource.getProperty(eq(EXTRA_ELEMENT_RESTRICTED_ATTRIBUTES_CONFIGURATION),
            eq(Properties.class), any())).thenReturn(value);
    }

    @Test
    void getExtraElementRestrictedAttributesDefaultsToEmptyMap()
    {
        setExtraElementRestrictedAttributes(new Properties());

        assertEquals(Map.of(), this.configuration.getExtraElementRestrictedAttributes());
    }

    @Test
    void getExtraElementRestrictedAttributesParsesWhitespaceAndCommaSeparatedElements()
    {
        Properties properties = new Properties();
        // Whitespace- and comma-separated values are both accepted, with arbitrary surrounding whitespace.
        properties.setProperty("name", "div span,  section");

        setExtraElementRestrictedAttributes(properties);

        assertEquals(Map.of("name", Set.of("div", "span", "section")),
            this.configuration.getExtraElementRestrictedAttributes());
    }

    @Test
    void getExtraElementRestrictedAttributesNormalizesToLowercase()
    {
        Properties properties = new Properties();
        properties.setProperty("Name", "DIV Span");

        setExtraElementRestrictedAttributes(properties);

        assertEquals(Map.of("name", Set.of("div", "span")),
            this.configuration.getExtraElementRestrictedAttributes());
    }

    @Test
    void getExtraElementRestrictedAttributesMergesAttributesDifferingOnlyByCase()
    {
        Properties properties = new Properties();
        properties.setProperty("name", "div");
        properties.setProperty("NAME", "span");

        setExtraElementRestrictedAttributes(properties);

        assertEquals(Map.of("name", Set.of("div", "span")),
            this.configuration.getExtraElementRestrictedAttributes());
    }
}
