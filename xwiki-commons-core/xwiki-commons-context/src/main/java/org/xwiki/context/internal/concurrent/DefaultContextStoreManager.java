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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.concurrent.ContextStore;
import org.xwiki.context.concurrent.ContextStoreManager;

/**
 * Default implementation of {@link ContextStoreManager}.
 * 
 * @version $Id$
 * @since 10.10RC1
 */
@Component
@Singleton
public class DefaultContextStoreManager implements ContextStoreManager
{
    @Inject
    private ComponentManager componentManager;

    @Override
    public Collection<String> getSupportedEntries() throws ComponentLookupException
    {
        // Make sure to return a stable list
        Set<String> entries = new TreeSet<>();

        List<ContextStore> stores = this.componentManager.getInstanceList(ContextStore.class);

        for (ContextStore store : stores) {
            entries.addAll(store.getSupportedEntries());
        }

        return entries;
    }

    @Override
    public Map<String, Serializable> save(Collection<String> entries) throws ComponentLookupException
    {
        if (entries == null) {
            return null;
        }

        Map<String, Serializable> context;

        if (!entries.isEmpty()) {
            context = new HashMap<>();

            List<ContextStore> stores = this.componentManager.getInstanceList(ContextStore.class);

            for (ContextStore store : stores) {
                store.save(context, entries);
            }
        } else {
            context = Collections.emptyMap();
        }

        return context;
    }

    @Override
    public void restore(Map<String, Serializable> contextStore) throws ComponentLookupException
    {
        if (contextStore != null && !contextStore.isEmpty()) {
            List<ContextStore> stores = this.componentManager.getInstanceList(ContextStore.class);

            for (ContextStore store : stores) {
                store.restore(contextStore);
            }
        }
    }
}
