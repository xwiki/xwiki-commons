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

import jakarta.inject.Inject;

import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Helper to implement {@link ConfigurationSource}.
 * 
 * @version $Id$
 * @since 17.5.0
 */
public abstract class AbstractSystemOverwriteConfigurationSource extends AbstractConfigurationSource
    implements Initializable
{
    @Inject
    protected ComponentDescriptor<AbstractSystemOverwriteConfigurationSource> componentDescriptor;

    @Inject
    protected ComponentManager componentManager;

    /**
     * True if it should be possible to overwrite this configuration source using system environment variables or
     * properties.
     */
    protected boolean systemOverwriteEnabled;

    protected ConfigurationSource systemConfigurationSource;

    @Override
    public void initialize() throws InitializationException
    {
        if (this.systemOverwriteEnabled) {
            try {
                this.systemConfigurationSource = this.componentManager.getInstance(ConfigurationSource.class, "system");
            } catch (ComponentLookupException e) {
                throw new InitializationException("Failed to lookup the system configuration source", e);
            }
        }
    }

    /**
     * @param key the standard key name
     * @return the name of key when overwritten by the system configuration
     */
    protected String toSystemOverwriteKey(String key)
    {
        return this.componentDescriptor.getRoleHint() + "." + key;
    }

    @Override
    public <T> T getProperty(String key)
    {
        if (this.systemConfigurationSource != null) {
            String systemOverwriteKey = toSystemOverwriteKey(key);
            if (this.systemConfigurationSource.containsKey(systemOverwriteKey)) {
                return this.systemConfigurationSource.getProperty(systemOverwriteKey);
            }
        }

        return getPropertyInternal(key);
    }

    /**
     * @param <T> the value type
     * @param key the property key for which we want the value
     * @return the property as an untyped Object or null if the key wasn't found. In general you should prefer
     *         {@link #getProperty(String, Class)} or {@link #getProperty(String, Object)}
     */
    protected abstract <T> T getPropertyInternal(String key);

    @Override
    public <T> T getProperty(String key, T defaultValue)
    {
        if (this.systemConfigurationSource != null) {
            String systemOverwriteKey = toSystemOverwriteKey(key);
            if (this.systemConfigurationSource.containsKey(systemOverwriteKey)) {
                return this.systemConfigurationSource.getProperty(systemOverwriteKey, defaultValue);
            }
        }

        return getPropertyInternal(key, defaultValue);
    }

    /**
     * @param <T> the value type
     * @param key the property key for which we want the value
     * @param defaultValue the value to use if the key isn't found
     * @return the property value is found or the default value if the key wasn't found
     */
    protected abstract <T> T getPropertyInternal(String key, T defaultValue);

    @Override
    public <T> T getProperty(String key, Class<T> valueClass)
    {
        if (this.systemConfigurationSource != null) {
            String systemOverwriteKey = toSystemOverwriteKey(key);
            if (this.systemConfigurationSource.containsKey(systemOverwriteKey)) {
                return this.systemConfigurationSource.getProperty(systemOverwriteKey, valueClass);
            }
        }

        return getPropertyInternal(key, valueClass);
    }

    protected abstract <T> T getPropertyInternal(String key, Class<T> valueClass);

    @Override
    public <T> T getProperty(String key, Class<T> valueClass, T defaultValue)
    {
        if (this.systemConfigurationSource != null) {
            String systemOverwriteKey = toSystemOverwriteKey(key);
            if (this.systemConfigurationSource.containsKey(systemOverwriteKey)) {
                return this.systemConfigurationSource.getProperty(systemOverwriteKey, valueClass, defaultValue);
            }
        }

        return getPropertyInternal(key, valueClass, defaultValue);
    }

    /**
     * @param <T> the value type
     * @param key the property key for which we want the value
     * @param valueClass the type of object that should be returned. The value is converted to the passed type.
     * @param defaultValue the value to use if the key isn't found
     * @return the property value is found or the default value if the key wasn't found.
     */
    protected abstract <T> T getPropertyInternal(String key, Class<T> valueClass, T defaultValue);

    @Override
    public List<String> getKeys()
    {
        if (this.systemConfigurationSource != null) {
            Set<String> keys = new LinkedHashSet<>();

            // Add current keys
            keys.addAll(getKeysInternal());

            // Add system ones
            keys.addAll(this.systemConfigurationSource.getKeys(toSystemOverwriteKey("")));

            return new ArrayList<>(keys);
        }

        return getKeysInternal();
    }

    /**
     * @return the list of available keys in the configuration source
     */
    protected abstract List<String> getKeysInternal();

    @Override
    public List<String> getKeys(String prefix)
    {
        if (this.systemConfigurationSource != null) {
            Set<String> keys = new LinkedHashSet<>();

            // Add current keys
            keys.addAll(getKeysInternal(prefix));

            // Add system ones
            keys.addAll(this.systemConfigurationSource.getKeys(toSystemOverwriteKey(prefix)));

            return new ArrayList<>(keys);
        }

        return getKeysInternal(prefix);
    }

    /**
     * @param prefix the prefix to filter the keys
     * @return the list of available keys in the configuration source that start with the passed prefix
     */
    protected abstract List<String> getKeysInternal(String prefix);

    @Override
    public boolean containsKey(String key)
    {
        if (this.systemConfigurationSource != null
            && this.systemConfigurationSource.containsKey(toSystemOverwriteKey(key))) {
            return true;
        }

        return containsKeyInternal(key);
    }

    /**
     * @param key the key to check
     * @return true if the key is present in the configuration source or false otherwise
     */
    protected abstract boolean containsKeyInternal(String key);

    @Override
    public boolean isEmpty()
    {
        if (this.systemConfigurationSource != null
            && !this.systemConfigurationSource.isEmpty(toSystemOverwriteKey(""))) {
            return false;
        }

        return isEmptyInternal();
    }

    /**
     * @return true if the configuration source doesn't have any key or false otherwise
     */
    protected abstract boolean isEmptyInternal();

    @Override
    public boolean isEmpty(String prefix)
    {
        if (this.systemConfigurationSource != null
            && !this.systemConfigurationSource.isEmpty(toSystemOverwriteKey(prefix))) {
            return false;
        }

        return isEmptyInternal(prefix);
    }

    /**
     * @param prefix the prefix to filter the keys
     * @return true if the configuration source doesn't have any key or false otherwise
     */
    protected abstract boolean isEmptyInternal(String prefix);
}
