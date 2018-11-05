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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.concurrent.ContextStore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Validate {@link DefaultContextStoreManager}.
 * 
 * @version $Id$
 */
public class DefaultContextStoreManagerTest
{
    private ComponentManager componentManager;

    private DefaultContextStoreManager manager;

    private ContextStore storeEmpty;

    private ContextStore store1;

    private ContextStore store2;

    @BeforeEach
    public void beforeEach() throws IllegalAccessException
    {
        this.componentManager = mock(ComponentManager.class);
        this.manager = new DefaultContextStoreManager();

        FieldUtils.writeField(this.manager, "componentManager", this.componentManager, true);

        this.storeEmpty = mock(ContextStore.class);

        this.store1 = mock(ContextStore.class);
        when(this.store1.getSupportedEntries()).thenReturn(Arrays.asList("entry11", "entry12"));
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                if (invocation.<Set<String>>getArgument(1).contains("entry11")) {
                    invocation.<Map<String, Serializable>>getArgument(0).put("entry11", "value11");
                }
                if (invocation.<Set<String>>getArgument(1).contains("entry12")) {
                    invocation.<Map<String, Serializable>>getArgument(0).put("entry12", "value12");
                }

                return null;
            }
        }).when(store1).save(any(), any());

        this.store2 = mock(ContextStore.class);
        when(this.store2.getSupportedEntries()).thenReturn(Arrays.asList("entry2"));
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                if (invocation.<Set<String>>getArgument(1).contains("entry2")) {
                    invocation.<Map<String, Serializable>>getArgument(0).put("entry2", "value2");
                }

                return null;
            }
        }).when(this.store2).save(any(), any());
    }

    private <T> Set<T> toSet(T... array)
    {
        return new HashSet<>(Arrays.asList(array));
    }

    private <T> Set<T> toSet(Collection<T> collection)
    {
        return new HashSet<>(collection);
    }

    private <K, V> Map<K, V> toMap(Object... array)
    {
        Map<K, V> map = new HashMap<>();

        for (int i = 0; i < array.length; i += 2) {
            map.put((K) array[i], (V) array[i + 1]);
        }

        return map;
    }

    private void register(ContextStore... stores) throws ComponentLookupException
    {
        when(this.componentManager.getInstanceList(ContextStore.class)).thenReturn(Arrays.asList(stores));
    }

    // Tests

    @Test
    public void getSupportedEntries() throws ComponentLookupException
    {
        assertTrue(this.manager.getSupportedEntries().isEmpty());

        register(this.storeEmpty);

        assertTrue(this.manager.getSupportedEntries().isEmpty());

        register(this.storeEmpty, this.store1);

        assertEquals(toSet("entry11", "entry12"), toSet(this.manager.getSupportedEntries()));

        register(this.storeEmpty, this.store1, this.store2);

        assertEquals(toSet("entry11", "entry12", "entry2"), toSet(this.manager.getSupportedEntries()));
    }

    @Test
    public void save() throws ComponentLookupException
    {
        assertTrue(this.manager.save(toSet()).isEmpty());
        assertTrue(this.manager.save(toSet("noentry")).isEmpty());
        assertTrue(this.manager.save(toSet("entry11", "entry12", "entry2")).isEmpty());

        register(this.storeEmpty);

        assertTrue(this.manager.save(toSet()).isEmpty());
        assertTrue(this.manager.save(toSet("noentry")).isEmpty());
        assertTrue(this.manager.save(toSet("entry11", "entry12", "entry2")).isEmpty());

        register(this.storeEmpty, this.store1);

        assertTrue(this.manager.save(toSet()).isEmpty());
        assertTrue(this.manager.save(toSet("noentry")).isEmpty());
        assertEquals(toMap("entry11", "value11"), this.manager.save(toSet("entry11")));
        assertEquals(toMap("entry11", "value11", "entry12", "value12"), this.manager.save(toSet("entry11", "entry12")));

        register(this.storeEmpty, this.store1, this.store2);

        assertTrue(this.manager.save(toSet()).isEmpty());
        assertTrue(this.manager.save(toSet("noentry")).isEmpty());
        assertEquals(toMap("entry11", "value11"), this.manager.save(toSet("entry11")));
        assertEquals(toMap("entry11", "value11", "entry12", "value12"), this.manager.save(toSet("entry11", "entry12")));
        assertEquals(toMap("entry11", "value11", "entry12", "value12"), this.manager.save(toSet("entry11", "entry12")));
        assertEquals(toMap("entry11", "value11", "entry12", "value12", "entry2", "value2"),
            this.manager.save(toSet("entry11", "entry12", "entry2")));
    }

    @Test
    public void restore() throws ComponentLookupException
    {
        this.manager.restore(null);
        this.manager.restore(toMap());

        register(this.storeEmpty);

        this.manager.restore(null);
        this.manager.restore(toMap());

        verifyNoMoreInteractions(this.storeEmpty);

        Map<String, Serializable> contextStore = toMap("key", "value");

        this.manager.restore(contextStore);

        verify(this.storeEmpty).restore(same(contextStore));

        register(this.storeEmpty, this.store1, this.store2);

        this.manager.restore(contextStore);

        verify(this.storeEmpty, times(2)).restore(same(contextStore));
        verify(this.store1).restore(same(contextStore));
        verify(this.store2).restore(same(contextStore));
    }
}
