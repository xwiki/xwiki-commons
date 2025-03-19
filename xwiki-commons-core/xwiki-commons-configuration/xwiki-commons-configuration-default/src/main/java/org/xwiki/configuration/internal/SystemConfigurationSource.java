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
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Composite Configuration Source that looks in the following sources in that order:
 * <ul>
 * <li>Environment variables.</li>
 * <li>Java system properties.</li>
 * </ul>
 * 
 * @version $Id$
 * @since 17.4.0RC1
 */
@Component
@Singleton
@Named("system")
public class SystemConfigurationSource extends CompositeConfigurationSource implements Initializable
{
    @Inject
    @Named(SystemEnvConfigurationSource.HINT)
    private ConfigurationSource varsSource;

    @Inject
    @Named(SystemPropertiesConfigurationSource.HINT)
    private ConfigurationSource propertiesSource;

    @Override
    public void initialize() throws InitializationException
    {
        addConfigurationSource(this.varsSource);
        addConfigurationSource(this.propertiesSource);
    }
}
