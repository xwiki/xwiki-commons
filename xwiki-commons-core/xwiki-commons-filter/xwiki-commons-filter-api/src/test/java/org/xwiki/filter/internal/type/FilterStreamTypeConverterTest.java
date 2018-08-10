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
package org.xwiki.filter.internal.type;

import org.junit.jupiter.api.Test;
import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link FilterStreamTypeConverter}.
 *
 * @version $Id$
 */
@ComponentTest
public class FilterStreamTypeConverterTest
{
    @InjectMockComponents
    private FilterStreamTypeConverter converter;

    @Test
    public void convertToTypeObject() throws Exception
    {
        FilterStreamType type =
            this.converter.convert(FilterStreamType.class, FilterStreamType.XWIKI_XAR_CURRENT.serialize());
        assertEquals(FilterStreamType.XWIKI_XAR_CURRENT, type);
    }

    @Test
    public void convertToTypeObjectWhenNull() throws Exception
    {
        FilterStreamType type = this.converter.convert(FilterStreamType.class, null);
        assertNull(type);
    }

    @Test
    public void convertToString() throws Exception
    {
        String typeId = this.converter.convert(String.class, FilterStreamType.XWIKI_XAR_CURRENT);
        assertEquals(FilterStreamType.XWIKI_XAR_CURRENT.serialize(), typeId);
    }

    @Test
    public void convertToStringWhenNull() throws Exception
    {
        String typeId = this.converter.convert(String.class, null);
        assertNull(typeId);
    }
}
