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

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSaveException;
import org.xwiki.configuration.ConfigurationSource;

/**
 * {@link System} properties based configuration source.
 * <p>
 * System properties are expected to be prefixed with {@code xconf_} to be taken into account by this
 * {@link org.xwiki.configuration.ConfigurationSource}.
 * <p>
 * For example the {@link ConfigurationSource} property key "configuration.key" will lead the the environment variable
 * "xconf.configuration.key".
 * 
 * @version $Id$
 * @since 17.4.0RC1
 */
@Component
@Singleton
@Named(SystemPropertiesConfigurationSource.HINT)
public class SystemPropertiesConfigurationSource extends AbstractPropertiesConfigurationSource
{
    /**
     * The hint to use to get this configuration source.
     */
    public static final String HINT = "systemproperties";

    /**
     * The prefix used to identify system properties used to overwrite the configuration.
     */
    public static final String PREFIX = "xconf.";

    String getSystemProperty(String key)
    {
        return System.getProperty(toSystemKey(key));
    }

    Properties getSystemProperties()
    {
        return System.getProperties();
    }

    String setSystemProperty(String key, String value)
    {
        return System.setProperty(key, value);
    }

    void setSystemProperties(Properties properties)
    {
        System.setProperties(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getPropertyInternal(String key)
    {
        if (key == null) {
            return null;
        }

        return (T) getSystemProperty(toSystemKey(key));
    }

    @Override
    public List<String> getKeysInternal()
    {
        return getKeys("");
    }

    @Override
    public List<String> getKeysInternal(String prefix)
    {
        String systemPrefix = toSystemKey(prefix);

        return getSystemProperties().keySet().stream().filter(k -> isSystemKey(k, systemPrefix))
            .map(this::fromSystemKey).toList();
    }

    @Override
    public boolean containsKeyInternal(String key)
    {
        if (key == null) {
            return false;
        }

        return getSystemProperties().containsKey(toSystemKey(key));
    }

    @Override
    public boolean isEmptyInternal()
    {
        return isEmpty("");
    }

    @Override
    public boolean isEmptyInternal(String prefix)
    {
        Properties properties = getSystemProperties();

        if (properties.isEmpty()) {
            return properties.isEmpty();
        }

        String systemPrefix = toSystemKey(prefix);

        return properties.keySet().stream().noneMatch(k -> isSystemKey(k, systemPrefix));
    }

    @Override
    public void setProperty(String key, Object value) throws ConfigurationSaveException
    {
        setSystemProperty(toSystemKey(key), this.converterManager.convert(String.class, value));
    }

    @Override
    public void setProperties(Map<String, Object> properties) throws ConfigurationSaveException
    {
        Properties systemProperties = new Properties();

        // Keep non configuration system properties
        getSystemProperties().entrySet().stream().filter(e -> !isSystemKey(e.getKey(), PREFIX))
            .forEach(e -> systemProperties.put(e.getKey(), e.getValue()));

        // Add new configuration to system properties
        properties.forEach((key, value) -> systemProperties.setProperty(toSystemKey(key),
            this.converterManager.convert(String.class, value)));

        setSystemProperties(systemProperties);
    }

    private boolean isSystemKey(Object key, String systemPrefix)
    {
        return key instanceof String keyString && keyString.startsWith(systemPrefix);
    }

    private String toSystemKey(Object key)
    {
        return PREFIX + key;
    }

    private String fromSystemKey(Object key)
    {
        return key.toString().substring(PREFIX.length());
    }
}
