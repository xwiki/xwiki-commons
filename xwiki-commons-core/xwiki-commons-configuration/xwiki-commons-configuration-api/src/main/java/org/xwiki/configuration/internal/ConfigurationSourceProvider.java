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
 * Provide an empty configuration if none can be lookuped.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class ConfigurationSourceProvider implements Provider<ConfigurationSource>
{
    /**
     * Used to lookup existing {@link ConfigurationSource} components.
     */
    @Inject
    private ComponentManager componentManager;

    @Override
    public ConfigurationSource get()
    {
        ConfigurationSource configurationSource;

        try {
            configurationSource = this.componentManager.lookup(ConfigurationSource.class);
        } catch (ComponentLookupException e) {
            try {
                configurationSource = this.componentManager.lookup(ConfigurationSource.class, "void");
            } catch (ComponentLookupException e1) {
                configurationSource = new VoidConfigurationSource();
            }
        }

        return configurationSource;
    }
}
