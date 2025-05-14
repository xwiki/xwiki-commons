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

import jakarta.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Validate {@link AbstractSystemOverwriteConfigurationSource}.
 * 
 * @version $Id$
 */
@ComponentTest
class SystemOverwriteConfigurationSourceTest
{
    public static class TestSystemOverwiteConfigurationSource extends AbstractSystemOverwriteConfigurationSource
    {
        public TestSystemOverwiteConfigurationSource()
        {
            this.systemOverwriteEnabled = true;
        }

        @Override
        protected <T> T getPropertyInternal(String key)
        {
            return (T) key;
        }

        @Override
        protected <T> T getPropertyInternal(String key, T defaultValue)
        {
            return (T) key;
        }

        @Override
        protected <T> T getPropertyInternal(String key, Class<T> valueClass)
        {
            return (T) key;
        }

        @Override
        protected <T> T getPropertyInternal(String key, Class<T> valueClass, T defaultValue)
        {
            return (T) key;
        }

        @Override
        protected List<String> getKeysInternal()
        {
            return List.of();
        }

        @Override
        protected List<String> getKeysInternal(String prefix)
        {
            return List.of();
        }

        @Override
        protected boolean containsKeyInternal(String key)
        {
            return false;
        }

        @Override
        protected boolean isEmptyInternal()
        {
            return true;
        }

        @Override
        protected boolean isEmptyInternal(String prefix)
        {
            return true;
        }
    }

    @MockComponent
    @Named("system")
    private ConfigurationSource systemConfigurationSource;

    @InjectMockComponents
    private TestSystemOverwiteConfigurationSource configuration;

    @BeforeEach
    void beforeEach()
    {
        when(this.systemConfigurationSource.getProperty("default.key")).thenReturn("system1");
        when(this.systemConfigurationSource.getProperty(eq("default.key"), any(Class.class))).thenReturn("system2");
        when(this.systemConfigurationSource.getProperty(eq("default.key"), any(Object.class))).thenReturn("system3");
        when(this.systemConfigurationSource.getProperty(eq("default.key"), any(Class.class), any(Object.class)))
            .thenReturn("system4");
        when(this.systemConfigurationSource.containsKey("default.key")).thenReturn(true);
        when(this.systemConfigurationSource.getKeys("default.")).thenReturn(List.of("system5"));
        when(this.systemConfigurationSource.getKeys("default.prefix")).thenReturn(List.of("system6"));
        when(this.systemConfigurationSource.isEmpty("default.")).thenReturn(false);
        when(this.systemConfigurationSource.isEmpty("default.prefix")).thenReturn(false);
    }

    @Test
    void overwriten()
    {
        this.configuration.systemOverwriteEnabled = true;

        assertTrue(this.configuration.containsKey("key"));
        assertEquals("system1", this.configuration.getProperty("key"));
        assertEquals("system2", this.configuration.getProperty("key", String.class));
        assertEquals("system3", this.configuration.getProperty("key", "other"));
        assertEquals("system4", this.configuration.getProperty("key", String.class, "other"));
        assertEquals(List.of("system5"), this.configuration.getKeys());
        assertEquals(List.of("system6"), this.configuration.getKeys("prefix"));
        assertFalse(this.configuration.isEmpty());
        assertFalse(this.configuration.isEmpty("prefix"));
    }

    @Test
    void notOverwritten()
    {
        this.configuration.systemOverwriteEnabled = false;

        assertFalse(this.configuration.containsKey("key"));
        assertEquals("key", this.configuration.getProperty("key"));
        assertEquals("key", this.configuration.getProperty("key", String.class));
        assertEquals("key", this.configuration.getProperty("key", "other"));
        assertEquals("key", this.configuration.getProperty("key", String.class, "other"));
        assertEquals(List.of(), this.configuration.getKeys());
        assertEquals(List.of(), this.configuration.getKeys("prefix"));
        assertTrue(this.configuration.isEmpty());
        assertTrue(this.configuration.isEmpty("prefix"));
    }
}
