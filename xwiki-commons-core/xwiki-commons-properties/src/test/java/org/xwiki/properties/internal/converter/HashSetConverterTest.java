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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.observation.EventListener;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Validate {@link HashSetConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class HashSetConverterTest
{
    @InjectComponentManager
    private MockitoComponentManager componentManager;

    private ConverterManager converterManager;

    @BeforeEach
    void setup() throws Exception
    {
        this.converterManager = this.componentManager.getInstance(ConverterManager.class);
    }

    @Test
    void convertFromHashSet()
    {
        HashSet<String> expect = new LinkedHashSet<>(Arrays.asList("1", "2", "3"));

        assertSame(expect, this.converterManager.convert(HashSet.class, expect));
    }

    @Test
    void convertToPrivateListTypes() throws ComponentLookupException
    {
        // Trigger the listener
        this.componentManager.getInstance(EventListener.class, ConverterRegistratorListener.NAME);

        assertEquals(Set.of("1", "2"), this.converterManager.convert(Set.of().getClass(), "1,2"));
        assertEquals(Set.of("1", "2"), this.converterManager.convert(Set.of(1).getClass(), "1,2"));
        assertEquals(Set.of("1", "2"), this.converterManager.convert(Set.of(1, 2, 3).getClass(), "1,2"));
        assertEquals(Set.of("1", "2"), this.converterManager.convert(Collections.emptySet().getClass(), "1,2"));
        assertEquals(Set.of("1", "2"),
            this.converterManager.convert(Collections.unmodifiableSet(Collections.emptySet()).getClass(), "1,2"));
    }
}
