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

/**
 * {@link System} properties based configuration source.
 *
 * @version $Id$
 * @since 17.2.0RC1
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

    private static final String PREFIX = "xconf.";

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key)
    {
        return (T) System.getProperty(toSystemKey(key));
    }

    @Override
    public List<String> getKeys()
    {
        return getKeys("");
    }

    @Override
    public List<String> getKeys(String prefix)
    {
        return System.getProperties().keySet().stream().filter(k -> isSystemKey(k, prefix))
            .map(k -> fromSystemKey(k, prefix)).toList();
    }

    @Override
    public boolean containsKey(String key)
    {
        return System.getProperties().containsKey(toSystemKey(key));
    }

    @Override
    public boolean isEmpty()
    {
        return isEmpty("");
    }

    @Override
    public boolean isEmpty(String prefix)
    {
        Properties properties = System.getProperties();

        if (properties.isEmpty()) {
            return properties.isEmpty();
        }

        return properties.keySet().stream().filter(k -> isSystemKey(k, prefix)).findFirst().isEmpty();
    }

    @Override
    public void setProperty(String key, Object value) throws ConfigurationSaveException
    {
        System.setProperty(toSystemKey(key), this.converterManager.convert(String.class, value));
    }

    @Override
    public void setProperties(Map<String, Object> properties) throws ConfigurationSaveException
    {
        Properties systemProperties = new Properties();

        properties.forEach((key, value) -> systemProperties.setProperty(toSystemKey(key),
            this.converterManager.convert(String.class, value)));

        System.setProperties(systemProperties);
    }

    private boolean isSystemKey(Object key, String prefix)
    {
        return key instanceof String keyString && keyString.startsWith(PREFIX + prefix);
    }

    private String toSystemKey(Object key)
    {
        return PREFIX + key;
    }

    private String fromSystemKey(Object key, String prefix)
    {
        return key.toString().substring(PREFIX.length() + prefix.length());
    }
}
