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
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
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
public class EnumConverterTest
{
    @InjectComponentManager
    MockitoComponentManager componentManager;

    private ConverterManager converterManager;

    public enum EnumTest
    {
        VALUE1,
        Value2
    }

    @BeforeEach
    void setup() throws Exception
    {
        this.converterManager = this.componentManager.getInstance(ConverterManager.class);
    }

    @Test
    void convertValid()
    {
        assertEquals(EnumTest.VALUE1, this.converterManager.convert(EnumTest.class, "VALUE1"));
    }

    @Test
    void convertIgnireCase()
    {
        assertEquals(EnumTest.VALUE1, this.converterManager.convert(EnumTest.class, "value1"));
    }

    @Test
    void convertToString()
    {
        assertEquals("VALUE1", this.converterManager.convert(String.class, EnumTest.VALUE1));
    }

    @Test
    void convertInvalid()
    {
        Throwable exception = assertThrows(ConversionException.class,
            () -> this.converterManager.convert(EnumTest.class, "notexistingvalue"));
        assertEquals("Unable to convert value [notexistingvalue]. Allowed values are (case insensitive) \"VALUE1\" "
            + "or \"Value2\".", exception.getMessage());
    }

    @Test
    void convertNull()
    {
        assertNull(this.converterManager.convert(EnumTest.class, null));
    }
}
