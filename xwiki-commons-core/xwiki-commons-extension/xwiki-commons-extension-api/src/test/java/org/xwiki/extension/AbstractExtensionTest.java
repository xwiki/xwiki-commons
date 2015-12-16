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
package org.xwiki.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.extension.repository.ExtensionRepository;

/**
 * @version $Id$
 */
public class AbstractExtensionTest
{
    private AbstractExtension extension;

    private ExtensionRepository repository;

    private ExtensionId id;

    private String type;

    @Before
    public void before()
    {
        this.repository = mock(ExtensionRepository.class);
        this.id = new ExtensionId("extesionid", "extensionversion");
        this.type = "extensiontype";

        this.extension = new AbstractExtension(this.repository, this.id, this.type)
        {
        };
    }

    @Test
    public void testGet()
    {
        assertSame(this.repository, this.extension.get("repository"));
        assertEquals(this.id.getId(), this.extension.get("id"));
        assertEquals(this.id.getVersion(), this.extension.get("version"));
        assertEquals(this.type, this.extension.get("type"));
    }
}
