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
package org.xwiki.configuration.internal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.xwiki.configuration.ConfigurationSource;

/**
 * Base class for composing (aka chaining) several Configuration Sources. The order of sources is important. Sources
 * located before other sources take priority.
 * 
 * @version $Id$
 * @since 7.4M1
 */
public abstract class AbstractCompositeConfigurationSource extends AbstractConfigurationSource
    implements Iterable<ConfigurationSource>
{
    @Override
    public boolean containsKey(String key)
    {
        boolean result = false;

        for (ConfigurationSource source : this) {
            if (source.containsKey(key)) {
                result = true;
                break;
            }
        }

        return result;
    }

    @Override
    public <T> T getProperty(String key)
    {
        T result = null;

        for (ConfigurationSource source : this) {
            if (source.containsKey(key)) {
                result = source.<T>getProperty(key);
                break;
            }
        }

        return result;
    }

    @Override
    public <T> T getProperty(String key, Class<T> valueClass)
    {
        T result = null;

        for (ConfigurationSource source : this) {
            if (source.containsKey(key)) {
                result = source.getProperty(key, valueClass);
                break;
            }
        }

        // List and Properties must return empty collections and not null values.
        if (result == null) {
            result = getDefault(valueClass);
        }

        return result;
    }

    @Override
    public <T> T getProperty(String key, T defaultValue)
    {
        T result = null;

        for (ConfigurationSource source : this) {
            if (source.containsKey(key)) {
                result = source.<T>getProperty(key, defaultValue);
                break;
            }
        }

        if (result == null) {
            result = defaultValue;
        }

        return result;
    }

    @Override
    public List<String> getKeys()
    {
        // We use a linked hash set in order to keep the keys in the order in which they were defined in the sources.
        Set<String> keys = new LinkedHashSet<>();

        for (ConfigurationSource source : this) {
            keys.addAll(source.getKeys());
        }

        return new ArrayList<>(keys);
    }

    @Override
    public List<String> getKeys(String prefix)
    {
        // We use a linked hash set in order to keep the keys in the order in which they were defined in the sources.
        Set<String> keys = new LinkedHashSet<>();

        for (ConfigurationSource source : this) {
            keys.addAll(source.getKeys(prefix));
        }

        return new ArrayList<>(keys);
    }

    @Override
    public boolean isEmpty()
    {
        for (ConfigurationSource source : this) {
            if (!source.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEmpty(String prefix)
    {
        for (ConfigurationSource source : this) {
            if (!source.isEmpty(prefix)) {
                return false;
            }
        }

        return true;
    }
}
