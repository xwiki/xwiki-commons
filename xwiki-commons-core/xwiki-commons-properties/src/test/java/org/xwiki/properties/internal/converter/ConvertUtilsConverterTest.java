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

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validate {@link ConvertUtilsConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ConvertUtilsConverterTest
{
    @InjectMockComponents
    private DefaultConverterManager converter;

    public Integer[] field;

    @Test
    void convert() throws SecurityException
    {
        assertEquals(Integer.valueOf(42), this.converter.convert(Integer.class, "42"));
    }

    @Test
    void convertArrays() throws SecurityException, NoSuchFieldException
    {
        assertArrayEquals(new int[] {1, 2, 3}, this.converter.<int[]>convert(int[].class, "1, 2, 3"));

        assertArrayEquals(new Integer[] {1, 2, 3}, this.converter.<Integer[]>convert(Integer[].class, "1, 2, 3"));

        assertArrayEquals(new Integer[] {1, 2, 3}, this.converter
            .<Integer[]>convert(ConvertUtilsConverterTest.class.getField("field").getGenericType(), "1, 2, 3"));
    }

    @Test
    void convertWhenNoConverterAvailable()
    {
        assertThrows(ConversionException.class, () -> this.converter.convert(ConvertUtilsConverter.class, ""),
            "Failed to find a Converter to convert from [java.lang.String] to " + "["
                + ConvertUtilsConverter.class.getName() + "]");
    }

    @Test
    void convertNull()
    {
        assertNull(this.converter.convert(String.class, null));
    }
}
