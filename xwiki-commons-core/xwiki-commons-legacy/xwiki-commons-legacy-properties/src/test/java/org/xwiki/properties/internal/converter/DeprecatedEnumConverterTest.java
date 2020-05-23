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
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.properties.converter.Converter;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validate {@link EnumConverter} component.
 * 
 * @version $Id$
 */
@ComponentTest
@AllComponents
class DeprecatedEnumConverterTest
{
    public Converter enumConverter;

    public enum EnumTest
    {
        VALUE
    }

    @BeforeEach
    void setUp(MockitoComponentManager componentManager) throws Exception
    {
        this.enumConverter = componentManager.getInstance(Converter.class, "enum");
    }

    @Test
    void convertValid()
    {
        assertEquals(EnumTest.VALUE, this.enumConverter.convert(EnumTest.class, "VALUE"));
    }

    @Test
    void convertIgnoreCase()
    {
        assertEquals(EnumTest.VALUE, this.enumConverter.convert(EnumTest.class, "value"));
    }

    @Test
    void convertInvalid()
    {
        Throwable exception = assertThrows(ConversionException.class,
            () -> this.enumConverter.convert(EnumTest.class, "notexistingvalue"));
        assertEquals("Unable to convert value [notexistingvalue]. Allowed values are (case insensitive) \"VALUE\".",
            exception.getMessage());
    }

    @Test
    void testConvertNull()
    {
        assertNull(this.enumConverter.convert(EnumTest.class, null));
    }
}
