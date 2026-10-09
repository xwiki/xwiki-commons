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
package org.xwiki.netflux.internal;

import org.junit.jupiter.api.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultNetfluxConfiguration}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultNetfluxConfigurationTest
{
    @InjectMockComponents
    private DefaultNetfluxConfiguration configuration;

    @MockComponent
    private ConfigurationSource configurationSource;

    @Test
    void defaultValues()
    {
        when(this.configurationSource.getProperty(anyString(), any(Object.class)))
            .then(invocation -> invocation.getArgument(1));

        assertEquals(75_000L, this.configuration.getMaxIdleTimeout());
        assertEquals(25_000L, this.configuration.getPingInterval());
        assertEquals(3, this.configuration.getPingMaxMissedPongs());
    }

    @Test
    void configuredValues()
    {
        when(this.configurationSource.getProperty("netflux.maxIdleTimeout", 75_000L)).thenReturn(130_000L);
        when(this.configurationSource.getProperty("netflux.pingInterval", 25_000L)).thenReturn(30_000L);
        when(this.configurationSource.getProperty("netflux.pingMaxMissedPongs", 3)).thenReturn(5);

        assertEquals(130_000L, this.configuration.getMaxIdleTimeout());
        assertEquals(30_000L, this.configuration.getPingInterval());
        assertEquals(5, this.configuration.getPingMaxMissedPongs());
    }
}
