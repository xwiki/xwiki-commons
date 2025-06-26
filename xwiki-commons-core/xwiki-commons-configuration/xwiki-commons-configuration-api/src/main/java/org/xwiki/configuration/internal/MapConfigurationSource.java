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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.ConvertUtils;
import org.xwiki.configuration.ConfigurationSource;

/**
 * A simple Map based {@link ConfigurationSource}.
 *
 * @version $Id$
 * @since 17.5.0
 */
public class MapConfigurationSource extends AbstractConfigurationSource
{
    /**
     * The properties.
     */
    private Map<String, Object> properties = new ConcurrentHashMap<>();

    @Override
    public void setProperty(String key, Object value)
    {
        this.properties.put(key, value);
    }

    @Override
    public void setProperties(Map<String, Object> properties)
    {
        this.properties = new ConcurrentHashMap<>(properties);
    }

    /**
     * @param key the key associated to the property to remove
     */
    public void removeProperty(String key)
    {
        this.properties.remove(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key, T defaultValue)
    {
        T result;

        if (this.properties.containsKey(key)) {
            Object value = this.properties.get(key);
            if (value != null && defaultValue != null && !defaultValue.getClass().isInstance(value)) {
                value = ConvertUtils.convert(value, defaultValue.getClass());
            }
            result = (T) value;
        } else {
            result = defaultValue;
        }

        return result;
    }

    @Override
    public <T> T getProperty(String key, Class<T> valueClass)
    {
        T result;

        if (this.properties.containsKey(key)) {
            Object value = this.properties.get(key);
            if (value != null && valueClass != null && !valueClass.isInstance(value)) {
                value = ConvertUtils.convert(value, valueClass);
            }
            result = (T) value;
        } else {
            result = getDefault(valueClass);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key)
    {
        return (T) this.properties.get(key);
    }

    @Override
    public List<String> getKeys()
    {
        return new ArrayList<>(this.properties.keySet());
    }

    @Override
    public List<String> getKeys(String prefix)
    {
        return this.properties.keySet().stream().filter(k -> k.startsWith(prefix)).toList();
    }

    @Override
    public boolean containsKey(String key)
    {
        return this.properties.containsKey(key);
    }

    @Override
    public boolean isEmpty()
    {
        return this.properties.isEmpty();
    }

    @Override
    public boolean isEmpty(String prefix)
    {
        return this.properties.keySet().stream().noneMatch(k -> k.startsWith(prefix));
    }
}
