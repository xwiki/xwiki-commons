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
package org.xwiki.properties.internal.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.namespace.Namespace;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link NamespaceConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class NamespaceConverterTest
{
    @InjectComponentManager
    MockitoComponentManager componentManager;

    private ConverterManager converterManager;

    @BeforeEach
    void setup() throws Exception
    {
        this.converterManager = this.componentManager.getInstance(ConverterManager.class);
    }

    @Test
    void convertToNamespace()
    {
        assertEquals(new Namespace("wiki", "wikiid"), this.converterManager.convert(Namespace.class, "wiki:wikiid"));
    }

    @Test
    void convertToString()
    {
        assertEquals("wiki:wikiid", this.converterManager.convert(String.class, new Namespace("wiki", "wikiid")));
    }
}
