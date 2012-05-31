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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Common code for Configuration Source Provider implementations.
 * 
 * @version $Id$
 * @since 4.1M2
 */
@Component
@Singleton
public abstract class AbstractConfigurationSourceProvider implements Provider<ConfigurationSource>
{
    /**
     * Used to lookup existing {@link org.xwiki.configuration.ConfigurationSource} components.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * @param hint the hint of the Configuration Source to lookup
     * @return the Configuration Source implementation with the passed hint or if not found a Memory Configuration
     *         Source and if not found a Void Configuration Source
     */
    protected ConfigurationSource get(String hint)
    {
        ConfigurationSource configurationSource;

        try {
            if (hint == null) {
                configurationSource = this.componentManager.getInstance(ConfigurationSource.class);
            } else {
                configurationSource = this.componentManager.getInstance(ConfigurationSource.class, hint);
            }
        } catch (ComponentLookupException e) {
            try {
                configurationSource = this.componentManager.getInstance(ConfigurationSource.class, "memory");
            } catch (ComponentLookupException e1) {
                configurationSource = getVoidConfigurationSource();
            }
        }

        return configurationSource;
    }

    /**
     * @return an empty configuration source
     */
    private ConfigurationSource getVoidConfigurationSource()
    {
        ConfigurationSource configurationSource;

        try {
            configurationSource = this.componentManager.getInstance(ConfigurationSource.class, "void");
        } catch (ComponentLookupException e) {
            configurationSource = new VoidConfigurationSource();
        }

        return configurationSource;
    }
}
