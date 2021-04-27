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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.xwiki.context.concurrent.ContextStore;

/**
 * Helper to implement {@link ContextStore}.
 * 
 * @version $Id$
 * @since 10.10RC1
 */
public abstract class AbstractContextStore implements ContextStore
{
    @FunctionalInterface
    protected interface SubContextStore
    {
        /**
         * Put in the context the value associated with the provided key prefix and suffix.
         *
         * @param key the main key (key prefix) of the value to save
         * @param subkey the sub key (key suffix) of the value to save
         */
        void save(String key, String subkey);
    }

    private List<String> supportedEntries;

    /**
     * @param entries the supported entries
     */
    public AbstractContextStore(String... entries)
    {
        this.supportedEntries = Collections.unmodifiableList(Arrays.asList(entries));
    }

    @Override
    public Collection<String> getSupportedEntries()
    {
        return this.supportedEntries;
    }

    protected void save(Map<String, Serializable> contextStore, String key, Serializable value,
        Collection<String> entries)
    {
        if (entries.contains(key)) {
            contextStore.put(key, value);
        }
    }

    protected void save(SubContextStore store, String prefix, Collection<String> entries)
    {
        for (String key : entries) {
            if (key.startsWith(prefix)) {
                store.save(key, key.substring(prefix.length()));
            }
        }
    }

    /**
     * @since 11.8RC1
     */
    protected <T> T get(Map<String, Serializable> contextStore, String key, T def)
    {
        if (contextStore.containsKey(key)) {
            return (T) contextStore.get(key);
        }

        return def;
    }
}
