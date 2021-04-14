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
import org.xwiki.extension.DefaultExtensionComponent;
import org.xwiki.extension.ExtensionComponent;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link ExtensionComponentConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ExtensionComponentConverterTest
{
    @InjectMockComponents
    private DefaultConverterManager manager;

    @Test
    void convertFromString()
    {
        assertNull(this.manager.convert(ExtensionComponent.class, null));
        assertEquals(new DefaultExtensionComponent("", (String) null),
            this.manager.convert(ExtensionComponent.class, ""));

        assertEquals(new DefaultExtensionComponent("name", (String) null),
            this.manager.convert(ExtensionComponent.class, "name"));
        assertEquals(new DefaultExtensionComponent("name", "hint"),
            this.manager.convert(ExtensionComponent.class, "name/hint"));
        assertEquals(new DefaultExtensionComponent("id\\", "hint"),
            this.manager.convert(ExtensionComponent.class, "id\\\\/hint"));

        assertEquals(new DefaultExtensionComponent("name/hint", (String) null),
            this.manager.convert(ExtensionComponent.class, "name\\/hint"));
        assertEquals(new DefaultExtensionComponent("/hint", (String) null),
            this.manager.convert(ExtensionComponent.class, "\\/hint"));
        assertEquals(new DefaultExtensionComponent("name\\/hint", (String) null),
            this.manager.convert(ExtensionComponent.class, "name\\\\\\/hint"));
    }

    @Test
    void convertToString()
    {
        assertEquals("name", this.manager.getConverter(ExtensionComponent.class).convert(String.class,
            new DefaultExtensionComponent("name", (String) null)));
        assertEquals("name/hint", this.manager.getConverter(ExtensionComponent.class).convert(String.class,
            new DefaultExtensionComponent("name", "hint")));
    }
}
