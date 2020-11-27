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
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.ExtensionAuthor;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link ExtensionAuthorConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ExtensionAuthorConverterTest
{
    @InjectMockComponents
    private DefaultConverterManager manager;

    @Test
    void convertFromString()
    {
        assertNull(this.manager.convert(ExtensionAuthor.class, null));
        assertEquals(new DefaultExtensionAuthor("", (String) null), this.manager.convert(ExtensionAuthor.class, ""));

        assertEquals(new DefaultExtensionAuthor("name", (String) null),
            this.manager.convert(ExtensionAuthor.class, "name"));
        assertEquals(new DefaultExtensionAuthor("name", "url"),
            this.manager.convert(ExtensionAuthor.class, "name/url"));
        assertEquals(new DefaultExtensionAuthor("id\\", "url"),
            this.manager.convert(ExtensionAuthor.class, "id\\\\/url"));

        assertEquals(new DefaultExtensionAuthor("name/url", (String) null),
            this.manager.convert(ExtensionAuthor.class, "name\\/url"));
        assertEquals(new DefaultExtensionAuthor("/url", (String) null),
            this.manager.convert(ExtensionAuthor.class, "\\/url"));
        assertEquals(new DefaultExtensionAuthor("name\\/url", (String) null),
            this.manager.convert(ExtensionAuthor.class, "name\\\\\\/url"));
    }

    @Test
    void convertToString()
    {
        assertEquals("", this.manager.getConverter(ExtensionAuthor.class).convert(String.class,
            new DefaultExtensionAuthor(null, (String) null)));
        assertEquals("/url", this.manager.getConverter(ExtensionAuthor.class).convert(String.class,
            new DefaultExtensionAuthor(null, "url")));

        assertEquals("name", this.manager.getConverter(ExtensionAuthor.class).convert(String.class,
            new DefaultExtensionAuthor("name", (String) null)));
        assertEquals("name/url", this.manager.getConverter(ExtensionAuthor.class).convert(String.class,
            new DefaultExtensionAuthor("name", "url")));
        assertEquals("name\\/url", this.manager.getConverter(ExtensionAuthor.class).convert(String.class,
            new DefaultExtensionAuthor("name/url", (String) null)));
    }
}
