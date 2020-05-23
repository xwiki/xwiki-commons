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

import java.awt.Color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.properties.converter.Converter;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validate {@link ColorConverter} component.
 * 
 * @version $Id$
 */
@ComponentTest
@AllComponents
class DeprecatedColorConverterTest
{
    private Converter colorConverter;

    @BeforeEach
    void setUp(MockitoComponentManager componentManager) throws Exception
    {
        this.colorConverter = componentManager.getInstance(Converter.class, Color.class.getName());
    }

    @Test
    void convertRGB()
    {
        assertEquals(Color.WHITE, this.colorConverter.convert(Color.class, "255 , 255 , 255"));
    }

    @Test
    void convertHTML()
    {
        assertEquals(Color.WHITE, this.colorConverter.convert(Color.class, "#ffffff"));
        assertEquals(Color.WHITE, this.colorConverter.convert(Color.class, "#FFFFFF"));
    }

    @Test
    void convertInvalid()
    {
        Throwable exception = assertThrows(ConversionException.class,
            () -> this.colorConverter.convert(Color.class, "wrongformat"));
        assertEquals("Color value should be in the form of '#xxxxxx' or 'r,g,b'", exception.getMessage());
    }
}
