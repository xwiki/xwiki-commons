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
 * @since 17.5.0RC1
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
     * @param key the key
     * @return the key to use with the system ConfigurationSource
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

    protected abstract boolean isEmptyInternal(String prefix);
}
