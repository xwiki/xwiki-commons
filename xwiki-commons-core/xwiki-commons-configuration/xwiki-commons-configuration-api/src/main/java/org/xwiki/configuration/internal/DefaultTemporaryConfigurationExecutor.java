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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSaveException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.configuration.TemporaryConfigurationExecutor;

/**
 * Default implementation of {@link TemporaryConfigurationExecutor}.
 * 
 * @version $Id$
 * @since 16.1.0RC1
 * @since 15.10.6
 */
@Component
@Singleton
public class DefaultTemporaryConfigurationExecutor implements TemporaryConfigurationExecutor
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Override
    public <V> V call(String sourceHint, Map<String, Object> temporaryConfiguration, Callable<V> callable)
        throws Exception
    {
        ConfigurationSource configurationSource = getConfigurationSource(sourceHint);
        Map<String, Pair<Boolean, Object>> backup = setConfiguration(configurationSource, temporaryConfiguration);
        try {
            return callable.call();
        } finally {
            restoreConfiguration(configurationSource, backup);
        }
    }

    private ConfigurationSource getConfigurationSource(String sourceHint) throws ComponentLookupException
    {
        ComponentManager componentManager = this.componentManagerProvider.get();
        return componentManager.getInstance(ConfigurationSource.class, sourceHint);
    }

    private Map<String, Pair<Boolean, Object>> setConfiguration(ConfigurationSource configurationSource,
        Map<String, Object> temporaryConfiguration) throws ConfigurationSaveException
    {
        Map<String, Pair<Boolean, Object>> backup = new HashMap<>();
        for (Map.Entry<String, Object> entry : temporaryConfiguration.entrySet()) {
            backup.put(entry.getKey(), new ImmutablePair<>(configurationSource.containsKey(entry.getKey()),
                configurationSource.getProperty(entry.getKey())));
            configurationSource.setProperty(entry.getKey(), entry.getValue());
        }
        return backup;
    }

    private void restoreConfiguration(ConfigurationSource configurationSource,
        Map<String, Pair<Boolean, Object>> backup) throws ConfigurationSaveException
    {
        for (Map.Entry<String, Pair<Boolean, Object>> entry : backup.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue().getLeft())) {
                // The property existed before, restore its previous value.
                configurationSource.setProperty(entry.getKey(), entry.getValue().getRight());
            } else {
                // The property didn't exist before, remove it.
                configurationSource.removeProperty(entry.getKey());
            }
        }
    }
}
