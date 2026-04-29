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

import java.util.List;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.collection.AbstractCollectionConverter;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Validate {@link ObjectArrayConverter} component (mostly {@link AbstractCollectionConverter} actually).
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ObjectArrayConverterTest
{
    @Inject
    private ConverterManager converterManager;

    @Test
    void convertFromString()
    {
        assertArrayEquals(new Object[] {"1", "2", "3"}, this.converterManager.convert(Object[].class, "1, 2, 3"));
    }

    @Test
    void convertFromIterable()
    {
        assertArrayEquals(new Object[] {"1", 2, "3"},
            this.converterManager.convert(Object[].class, List.of("1", 2, "3")));
    }

    @Test
    void convertFromArray()
    {
        assertArrayEquals(new Object[] {1, 2, 3},
            this.converterManager.convert(Object[].class, new Integer[] {1, 2, 3}));
    }

    @Test
    void convertFromNull()
    {
        assertNull(this.converterManager.convert(Object[].class, null));
    }

    @Test
    void convertFromObjectArray()
    {
        Object[] expect = new Object[] {"1", "2", "3"};

        assertSame(expect, this.converterManager.convert(Object[].class, expect));
    }
}
