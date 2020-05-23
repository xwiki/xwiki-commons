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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.properties.converter.Converter;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link ColorConverter} component.
 * 
 * @version $Id$
 */
@ComponentTest
@AllComponents
class DeprecatedListConverterTest
{
    private Converter listConverter;

    public List<Integer> field1;

    public List<List<Integer>> field2;

    @BeforeEach
    void setUp(MockitoComponentManager componentManager) throws Exception
    {
        this.listConverter = componentManager.getInstance(Converter.class, List.class.getName());
    }

    @Test
    void convert() throws SecurityException, NoSuchFieldException
    {
        assertEquals(new ArrayList<>(Arrays.asList("1", "2", "3")),
            this.listConverter.convert(List.class, "1, 2, 3"));

        assertEquals(new ArrayList<>(Arrays.asList("1", "\n", "2", "\n", "3")),
            this.listConverter.convert(List.class, "1,\n 2,\n 3"));

        assertEquals(
            new ArrayList<>(Arrays.asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))),
            this.listConverter.convert(DeprecatedListConverterTest.class.getField("field1").getGenericType(), "1, 2, 3"));

        assertEquals(new ArrayList(Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6))),
            this.listConverter.convert(DeprecatedListConverterTest.class.getField("field2").getGenericType(),
                "'\\'1\\', 2, 3', \"4, 5, 6\""));

        assertEquals(new ArrayList<>(Arrays.asList("1:2")), this.listConverter.convert(List.class, "1:2"));
    }
}
