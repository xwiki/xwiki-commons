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

import jakarta.inject.Inject;

import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.properties.ConverterManager;

/**
 * Helper to implement a {@link ConfigurationSource} with automatic conversion of property values based on the
 * {@code xwiki-properties} framework.
 * 
 * @version $Id$
 * @since 17.5.0RC1
 */
public abstract class AbstractPropertiesConfigurationSource extends AbstractSystemOverwriteConfigurationSource
{
    /**
     * Component used for performing type conversions.
     */
    @Inject
    protected ConverterManager converterManager;

    @Override
    @SuppressWarnings("unchecked")
    protected <T> T getPropertyInternal(String key, T defaultValue)
    {
        T result;
        if (containsKey(key)) {
            if (defaultValue != null) {
                result = getConvertedProperty(key, (Class<T>) defaultValue.getClass(), defaultValue);
            } else {
                result = getProperty(key);
            }
        } else {
            result = defaultValue;
        }

        return result;
    }

    @Override
    protected <T> T getPropertyInternal(String key, Class<T> valueClass)
    {
        return getConvertedProperty(key, valueClass, null);
    }

    @Override
    protected <T> T getPropertyInternal(String key, Class<T> valueClass, T defaultValue)
    {
        return getConvertedProperty(key, valueClass, defaultValue);
    }

    /**
     * @param <T> the value type
     * @param key the property key for which we want the value
     * @param valueClass the type of object that should be returned. The value is converted to the passed type.
     * @param defaultValue the value to use if the key isn't found
     * @return the value associated with the key, converted to the request type
     */
    protected <T> T getConvertedProperty(String key, Class<T> valueClass, T defaultValue)
    {
        Object value = getProperty(key);
        if (value != null) {
            return this.converterManager.convert(valueClass, value);
        }

        return defaultValue;
    }
}
