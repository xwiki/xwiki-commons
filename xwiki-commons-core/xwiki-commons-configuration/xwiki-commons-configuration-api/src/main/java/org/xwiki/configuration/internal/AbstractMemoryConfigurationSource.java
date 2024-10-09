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

import org.apache.commons.beanutils.ConvertUtils;

/**
 * Base class for configuration sources that store the configuration in memory.
 * 
 * @version $Id$
 * @since 16.1.0RC1
 * @since 15.10.6
 */
public abstract class AbstractMemoryConfigurationSource extends AbstractConfigurationSource
{
    protected abstract Map<String, Object> getProperties();

    @Override
    public void setProperties(Map<String, Object> newProperties)
    {
        Map<String, Object> currentProperties = getProperties();
        currentProperties.clear();
        currentProperties.putAll(newProperties);
    }

    @Override
    public void setProperty(String key, Object value)
    {
        getProperties().put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T removeProperty(String key)
    {
        return (T) getProperties().remove(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key, T defaultValue)
    {
        T result;

        if (getProperties().containsKey(key)) {
            Object value = getProperties().get(key);
            if (value != null && defaultValue != null && !defaultValue.getClass().isInstance(value)) {
                value = ConvertUtils.convert(value, defaultValue.getClass());
            }
            result = (T) value;
        } else {
            result = defaultValue;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key, Class<T> valueClass)
    {
        T result;

        if (getProperties().containsKey(key)) {
            Object value = getProperties().get(key);
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
        return (T) getProperties().get(key);
    }

    @Override
    public List<String> getKeys()
    {
        return new ArrayList<>(getProperties().keySet());
    }

    @Override
    public boolean containsKey(String key)
    {
        return getProperties().containsKey(key);
    }

    @Override
    public boolean isEmpty()
    {
        return getProperties().isEmpty();
    }
}
