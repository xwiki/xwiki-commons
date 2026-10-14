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
package org.xwiki.extension.repository.aether.internal.configuration;

import java.io.File;

import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.environment.Environment;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultAetherConfiguration}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultAetherConfigurationTest
{
    @InjectMockComponents
    private DefaultAetherConfiguration configuration;

    @MockComponent
    private Provider<ConfigurationSource> configurationSourceProvider;

    @MockComponent
    private Environment environment;

    private ConfigurationSource configurationSource;

    @BeforeEach
    void beforeEach()
    {
        this.configurationSource = mock(ConfigurationSource.class);
        when(this.configurationSourceProvider.get()).thenReturn(this.configurationSource);
    }

    @Test
    void getLocalRepositoryWhenNotConfigured()
    {
        File temporaryDirectory = new File("tmp");
        when(this.environment.getTemporaryDirectory()).thenReturn(temporaryDirectory);

        assertEquals(new File(temporaryDirectory, "aether-repository"), this.configuration.getLocalRepository());
    }

    @Test
    void getLocalRepositoryWhenConfigured()
    {
        when(this.configurationSource.getProperty("extension.aether.localRepository")).thenReturn("/configured/path");

        assertEquals(new File("/configured/path"), this.configuration.getLocalRepository());
    }
}
