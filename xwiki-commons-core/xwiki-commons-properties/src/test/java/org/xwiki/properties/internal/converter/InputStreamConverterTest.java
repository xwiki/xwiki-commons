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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link InputStreamConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class InputStreamConverterTest
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
    void testFromString() throws IOException
    {
        assertTrue(Arrays.equals(new byte[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9 },
            IOUtils.toByteArray(this.converterManager.<InputStream>convert(InputStream.class, "1,2,3,4,5,6,7,8,9"))));
    }

    @Test
    void convertNull()
    {
        assertNull(this.converterManager.convert(InputStream.class, null));
    }
}
