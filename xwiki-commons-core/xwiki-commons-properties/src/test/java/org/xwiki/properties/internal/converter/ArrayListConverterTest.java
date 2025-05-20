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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.observation.EventListener;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Validate {@link ArrayListConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ArrayListConverterTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @InjectMockComponents
    private DefaultConverterManager converterManager;

    @Test
    void convertFromString() throws Exception
    {
        assertEquals(Arrays.asList("1", "2", "3"), this.converterManager.convert(ArrayList.class, "1, 2, 3"));
    }

    @Test
    void testConvertFromArrayList()
    {
        ArrayList<String> expect = new ArrayList<>(Arrays.asList("1", "2", "3"));

        assertSame(expect, this.converterManager.convert(ArrayList.class, expect));
    }

    @Test
    void convertToPrivateListTypes() throws ComponentLookupException
    {
        // Trigger the listener
        this.componentManager.getInstance(EventListener.class, ConverterRegistratorListener.NAME);

        assertEquals(List.of("1", "2"), this.converterManager.convert(List.of().getClass(), "1,2"));
        assertEquals(List.of("1", "2"), this.converterManager.convert(List.of(1).getClass(), "1,2"));
        assertEquals(List.of("1", "2"), this.converterManager.convert(List.of(1, 2, 3).getClass(), "1,2"));
        assertEquals(List.of("1", "2"), this.converterManager.convert(Arrays.asList().getClass(), "1,2"));
        assertEquals(List.of("1", "2"), this.converterManager.convert(Collections.emptyList().getClass(), "1,2"));
        assertEquals(List.of("1", "2"),
            this.converterManager.convert(Collections.unmodifiableList(Collections.emptyList()).getClass(), "1,2"));
    }
}
