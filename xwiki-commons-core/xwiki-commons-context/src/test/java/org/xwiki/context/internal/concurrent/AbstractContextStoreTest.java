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
package org.xwiki.context.internal.concurrent;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.context.internal.concurrent.AbstractContextStore.SubContextStore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * @version $Id$
 */
class AbstractContextStoreTest
{
    private AbstractContextStore component = new AbstractContextStore("entry1", "entry2")
    {
        @Override
        public void save(Map<String, Serializable> contextStore, Collection<String> entries)
        {
            // Test class
        }

        @Override
        public void restore(Map<String, Serializable> contextStore)
        {
            // Test class
        }
    };

    // Tests

    @Test
    void get()
    {
        Map<String, Serializable> contextStore = new HashMap<>();

        assertEquals("def", this.component.get(contextStore, "key", "def"));

        contextStore.put("key", null);

        assertNull(this.component.get(contextStore, "key", "def"));
    }

    @Test
    void save()
    {
        Map<String, Serializable> contextStore = new HashMap<>();

        this.component.save(contextStore, "key", "value", Arrays.asList());

        assertFalse(contextStore.containsKey("key"));

        this.component.save(contextStore, "key", null, Arrays.asList("key"));

        assertTrue(contextStore.containsKey("key"));
        assertNull(contextStore.get("key"));
    }

    @Test
    void saveSubContext()
    {
        SubContextStore subStore = mock(SubContextStore.class);

        this.component.save(subStore, "prefix", Arrays.asList());

        verifyNoMoreInteractions(subStore);

        this.component.save(subStore, "prefix", Arrays.asList("prefix"));

        verify(subStore).save("prefix", "");

        this.component.save(subStore, "prefix.", Arrays.asList("prefix.subkey"));

        verify(subStore).save("prefix.subkey", "subkey");
    }
}
