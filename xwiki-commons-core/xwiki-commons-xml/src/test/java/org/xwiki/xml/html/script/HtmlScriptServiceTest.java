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
package org.xwiki.xml.html.script;

import javax.inject.Named;

import org.junit.jupiter.api.Test;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.xml.html.HTMLElementSanitizer;
import org.xwiki.xml.internal.html.SecureHTMLElementSanitizer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link HtmlScriptService}.
 *
 * @version $Id$
 * @since 14.10.4
 * @since 15.0RC1
 */
@ComponentTest
class HtmlScriptServiceTest
{
    @InjectMockComponents
    private HtmlScriptService htmlScriptService;

    @MockComponent
    @Named(SecureHTMLElementSanitizer.HINT)
    private HTMLElementSanitizer sanitizer;

    @Test
    void isElementSafe()
    {
        String element = "myElement";
        when(this.sanitizer.isElementAllowed(element)).thenReturn(true);
        assertTrue(this.htmlScriptService.isElementSafe(element));
        verify(this.sanitizer).isElementAllowed(element);
    }

    @Test
    void isAttributeSafe()
    {
        String element = "anotherElement";
        String attribute = "onclick";
        String value = "someDangerousValue";
        when(this.htmlScriptService.isAttributeSafe(element, attribute, value)).thenReturn(false);
        assertFalse(this.htmlScriptService.isAttributeSafe(element, attribute, value));
        verify(this.sanitizer).isAttributeAllowed(element, attribute, value);
    }

}