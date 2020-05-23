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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import com.google.common.reflect.TypeToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Validate {@link ListConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ListConverterTest
{
    @InjectComponentManager
    MockitoComponentManager componentManager;

    private ConverterManager converterManager;

    public List<Integer> listField1;

    public List<List<Integer>> listField2;

    @BeforeEach
    void setup() throws Exception
    {
        this.converterManager = this.componentManager.getInstance(ConverterManager.class);
    }

    @Test
    void convertFromString() throws Exception
    {
        assertEquals(Arrays.asList("1", "2", "3"), this.converterManager.convert(List.class, "1, 2, 3"));

        assertEquals(Arrays.asList("1", "\n", "2", "\n", "3"),
            this.converterManager.convert(List.class, "1,\n 2,\n 3"));

        assertEquals(Arrays.asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)),
            this.converterManager.convert(ListConverterTest.class.getField("listField1").getGenericType(), "1, 2, 3"));

        assertEquals(Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6)), this.converterManager
            .convert(ListConverterTest.class.getField("listField2").getGenericType(), "'\\'1\\', 2, 3', \"4, 5, 6\""));

        assertEquals(Arrays.asList("1:2"), this.converterManager.convert(List.class, "1:2"));

        assertEquals(Arrays.asList(1, 2, 3),
            this.converterManager.convert(new DefaultParameterizedType(null, List.class, Integer.class), "1, 2, 3"));

        assertEquals(Arrays.asList(1, 2, 3), this.converterManager.convert(new TypeToken<List<? extends Integer>>()
        {
        }.getType(), "1, 2, 3"));

        assertEquals(Arrays.asList("1", "2", "3"), this.converterManager.convert(new TypeToken<List<?>>()
        {
        }.getType(), "1, 2, 3"));
    }

    @Test
    void convertFromIterable()
    {
        assertEquals(Arrays.asList(1, 2, 3), this.converterManager
            .convert(new DefaultParameterizedType(null, List.class, Integer.class), Arrays.asList("1", "2", "3")));
    }

    @Test
    void convertFromArray()
    {
        assertEquals(Arrays.asList(1, 2, 3), this.converterManager
            .convert(new DefaultParameterizedType(null, List.class, Integer.class), new String[]{ "1", "2", "3" }));
    }

    @Test
    void convertFromList()
    {
        List<String> expect = Arrays.asList("1", "2", "3");

        assertSame(expect, this.converterManager.convert(List.class, expect));
    }
}
