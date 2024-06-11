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
package org.xwiki.extension.internal.converter;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.ExtensionSupporter;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.xwiki.extension.DefaultExtensionSupportPlansTest.supporter;

/**
 * Validate {@link ExtensionSupporterConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ExtensionSupporterConverterTest
{
    @InjectMockComponents
    private DefaultConverterManager manager;

    private void assertFromString(String supporter, String supporterURL, String input)
    {
        assertEquals(supporter(supporter, supporterURL), this.manager.convert(ExtensionSupporter.class, input));
    }

    private void assertToString(String expect, String supporter, String supporterURL)
    {
        assertEquals(expect, this.manager.getConverter(ExtensionSupporter.class).convert(String.class,
            supporter(supporter, supporterURL)));
    }

    @Test
    void convertFromString()
    {
        assertNull(this.manager.convert(ExtensionSupporter.class, null));
        assertFromString("", null, "");

        assertFromString("name", null, "name");
        assertFromString("name", "http://host", "name/http://host");
        assertFromString("id\\", "http://host", "id\\\\/http://host");
        assertFromString("name", "http://host", "name/http://host");

        assertFromString("name/url", null, "name\\/url");
        assertFromString("name//url", null, "name\\/\\/url");
        assertFromString("/url", null, "\\/url");
        assertFromString("name\\/url", null, "name\\\\\\/url");
    }

    @Test
    void convertToString()
    {
        assertToString("", null, null);
        assertToString("/http://host", null, "http://host");

        assertToString("name", "name", null);
        assertToString("name/http://host", "name", "http://host");
        assertToString("name\\/http:\\/\\/host", "name/http://host", null);
    }
}
