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
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Provide the default ConfigurationSource provider for the hint "configurationSource".
 * 
 * @version $Id$
 * @since 3.5M1
 * @deprecated since 4.0M1 use provider with default hint
 */
@Component
@Named("configurationSource")
@Singleton
@Deprecated
public class DeprecatedConfigurationSourceProvider implements Provider<ConfigurationSource>
{
    /**
     * The real default provider.
     */
    @Inject
    private Provider<ConfigurationSource> provider;

    @Override
    public ConfigurationSource get()
    {
        return this.provider.get();
    }
}
