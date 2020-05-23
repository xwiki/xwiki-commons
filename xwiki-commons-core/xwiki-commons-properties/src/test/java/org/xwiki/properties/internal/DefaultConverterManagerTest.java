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
package org.xwiki.properties.internal;

import java.awt.Color;

import org.junit.jupiter.api.Test;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validate {@link DefaultConverterManager}.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class DefaultConverterManagerTest
{
    @InjectMockComponents
    private DefaultConverterManager converterManager;

    public enum TestEnum
    {
        ENUMVALUE
    }

    @Test
    void convert()
    {
        assertEquals(Integer.valueOf(42), this.converterManager.convert(Integer.class, "42"));
    }

    @Test
    void convertEnum()
    {
        assertEquals(TestEnum.ENUMVALUE, this.converterManager.convert(TestEnum.class, "ENUMVALUE"));
    }

    @Test
    void convertColor()
    {
        assertEquals(Color.WHITE, this.converterManager.convert(Color.class, "#ffffff"));
    }

    @Test
    void convertUnsupportedType()
    {
        Throwable exception = assertThrows(ConversionException.class,
            () -> this.converterManager.convert(DefaultConverterManagerTest.class, "#ffffff"));
        assertEquals("Failed to find a Converter to convert from [java.lang.String] to "
            + "[org.xwiki.properties.internal.DefaultConverterManagerTest]", exception.getMessage());
    }

    @Test
    void convertSame()
    {
        assertSame(Color.WHITE, this.converterManager.convert(Color.class, Color.WHITE));
    }
}
