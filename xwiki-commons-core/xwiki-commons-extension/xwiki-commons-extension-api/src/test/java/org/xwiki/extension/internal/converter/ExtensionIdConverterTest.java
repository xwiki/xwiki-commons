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
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link ExtensionIdConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ExtensionIdConverterTest
{
    @InjectMockComponents
    private DefaultConverterManager manager;

    @Test
    void convertFromString()
    {
        assertEquals(new ExtensionId("id"), this.manager.convert(ExtensionId.class, "id"));
        assertEquals(new ExtensionId("id", new DefaultVersion("1.0")),
            this.manager.convert(ExtensionId.class, "id/1.0"));
        assertEquals(new ExtensionId("id\\", new DefaultVersion("1.0")),
            this.manager.convert(ExtensionId.class, "id\\\\/1.0"));

        assertEquals(new ExtensionId("id/1.0"),
            this.manager.convert(ExtensionId.class, "id\\/1.0"));
        assertEquals(new ExtensionId("/1.0"), this.manager.convert(ExtensionId.class, "\\/1.0"));
        assertEquals(new ExtensionId("id\\/1.0"),
            this.manager.convert(ExtensionId.class, "id\\\\\\/1.0"));
    }
}
