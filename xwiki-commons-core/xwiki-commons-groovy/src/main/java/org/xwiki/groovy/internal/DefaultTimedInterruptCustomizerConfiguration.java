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
package org.xwiki.groovy.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.groovy.TimedInterruptCustomizerConfiguration;

/**
 * Default configuration implementation for the Timed Interrupt Compilation Customizer.
 *
 * @version $Id$
 * @since 4.1M1
 */
@Component
@Singleton
public class DefaultTimedInterruptCustomizerConfiguration implements TimedInterruptCustomizerConfiguration
{
    /**
     * Prefix for configuration keys for Groovy Compiler Customizers.
     */
    private static final String PREFIX = "groovy.customizer.timedInterrupt.";

    /**
     * By default we timeout after 1 minute.
     */
    private static final Long SCRIPT_TIMEOUT = 60L;

    /**
     * Defines from where to read the configuration data.
     */
    @Inject
    private ConfigurationSource configuration;

    @Override
    public long getTimeout()
    {
        return this.configuration.getProperty(PREFIX + "timeout", SCRIPT_TIMEOUT);
    }
}
