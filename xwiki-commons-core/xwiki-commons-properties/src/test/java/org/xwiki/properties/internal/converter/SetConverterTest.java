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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link HashSetConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class SetConverterTest
{
    @InjectComponentManager
    MockitoComponentManager componentManager;

    private ConverterManager converterManager;

    public Set<Integer> setField1;

    public Set<Set<Integer>> setField2;

    @BeforeEach
    void setUp() throws Exception
    {
        this.converterManager = this.componentManager.getInstance(ConverterManager.class);
    }

    @Test
    void convert() throws Exception
    {
        assertEquals(new LinkedHashSet<>(Arrays.asList("1", "2", "3")),
            this.converterManager.convert(Set.class, "1, 2, 3"));

        assertEquals(new LinkedHashSet<>(Arrays.asList("1", "\n", "2", "\n", "3")),
            this.converterManager.convert(Set.class, "1,\n 2,\n 3"));

        assertEquals(
            new LinkedHashSet<>(Arrays.asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3))),
            this.converterManager.convert(SetConverterTest.class.getField("setField1").getGenericType(), "1, 2, 3"));

        assertEquals(
            new LinkedHashSet<Set<Integer>>(Arrays.asList(new LinkedHashSet<>(Arrays.asList(1, 2, 3)),
                new LinkedHashSet<>(Arrays.asList(4, 5, 6)))), this.converterManager.convert(
                SetConverterTest.class.getField("setField2").getGenericType(), "'\\'1\\', 2, 3', \"4, 5, 6\""));

        assertEquals(new HashSet<>(Arrays.asList("1:2")), this.converterManager.convert(Set.class, "1:2"));
    }
}
