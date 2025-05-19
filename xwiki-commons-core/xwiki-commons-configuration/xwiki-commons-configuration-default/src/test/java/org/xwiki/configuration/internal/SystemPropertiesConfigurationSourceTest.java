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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.configuration.ConfigurationSaveException;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Validate {@link SystemPropertiesConfigurationSource}.
 * 
 * @version $Id$
 */
@ComponentTest
class SystemPropertiesConfigurationSourceTest
{
    public static class TestSystemPropertiesConfigurationSource extends SystemPropertiesConfigurationSource
    {
        private Properties properties = new Properties();

        @Override
        String getSystemProperty(String key)
        {
            return properties.getProperty(key);
        }

        @Override
        Properties getSystemProperties()
        {
            return this.properties;
        }

        @Override
        String setSystemProperty(String key, String value)
        {
            return (String) this.properties.setProperty(key, value);
        }

        @Override
        void setSystemProperties(Properties properties)
        {
            this.properties = properties;
        }
    }

    @InjectMockComponents
    private TestSystemPropertiesConfigurationSource configuration;

    @MockComponent
    private ConverterManager converterManager;

    @BeforeEach
    void beforeEach()
    {
        when(this.converterManager.convert(Integer.class, "15")).thenReturn(15);
        when(this.converterManager.convert(String.class, "value")).thenReturn("value");
        when(this.converterManager.convert(String.class, 15)).thenReturn("15");
    }

    @Test
    void getProperty()
    {
        assertNull(this.configuration.getProperty(null));
        assertNull(this.configuration.getProperty("doesnotexist"));
        assertNull(this.configuration.getProperty("key", Integer.class));
        assertEquals(1, this.configuration.getProperty("key", 1));

        this.configuration.setSystemProperty("key", "value");

        assertNull(this.configuration.getProperty("key"));

        this.configuration.setSystemProperty(SystemPropertiesConfigurationSource.PREFIX + "key", "15");

        assertEquals("15", this.configuration.getProperty("key"));
        assertEquals(15, this.configuration.getProperty("key", 1));
        assertEquals(15, this.configuration.getProperty("key", Integer.class));
        assertEquals(15, this.configuration.getProperty("key", Integer.class, 1));
    }

    @Test
    void getKeys()
    {
        assertEquals(List.of(), this.configuration.getKeys());
        assertEquals(List.of(), this.configuration.getKeys("prefix"));

        this.configuration.setSystemProperty("key", "value");

        assertEquals(List.of(), this.configuration.getKeys());
        assertEquals(List.of(), this.configuration.getKeys("prefix"));
        assertEquals(List.of(), this.configuration.getKeys("key"));

        this.configuration.setSystemProperty(SystemPropertiesConfigurationSource.PREFIX + "key", "15");

        assertEquals(List.of("key"), this.configuration.getKeys());
        assertEquals(List.of(), this.configuration.getKeys("prefix"));
        assertEquals(List.of("key"), this.configuration.getKeys("k"));
        assertEquals(List.of("key"), this.configuration.getKeys("key"));
    }

    @Test
    void isEmpty()
    {
        assertTrue(this.configuration.isEmpty());
        assertTrue(this.configuration.isEmpty("prefix"));

        this.configuration.setSystemProperty("key", "value");

        assertTrue(this.configuration.isEmpty());
        assertTrue(this.configuration.isEmpty("prefix"));
        assertTrue(this.configuration.isEmpty("key"));

        this.configuration.setSystemProperty(SystemPropertiesConfigurationSource.PREFIX + "key", "15");

        assertFalse(this.configuration.isEmpty());
        assertTrue(this.configuration.isEmpty("prefix"));
        assertFalse(this.configuration.isEmpty("key"));
    }

    @Test
    void setProperty() throws ConfigurationSaveException
    {
        assertTrue(this.configuration.getSystemProperties().isEmpty());

        this.configuration.setProperty("key", "value");

        assertEquals("value", this.configuration.getSystemProperty(SystemPropertiesConfigurationSource.PREFIX + "key"));
    }

    @Test
    void setProperties() throws ConfigurationSaveException
    {
        this.configuration.setSystemProperty("key1", "value1");
        this.configuration.setSystemProperty(SystemPropertiesConfigurationSource.PREFIX + "key2", "value2");

        assertEquals("value1", this.configuration.getSystemProperty("key1"));
        assertEquals("value2",
            this.configuration.getSystemProperty(SystemPropertiesConfigurationSource.PREFIX + "key2"));

        this.configuration.setProperties(Map.of("key3", 15));

        assertEquals("value1", this.configuration.getSystemProperty("key1"));
        assertNull(this.configuration.getSystemProperty(SystemPropertiesConfigurationSource.PREFIX + "key2"));
        assertEquals("15", this.configuration.getSystemProperty(SystemPropertiesConfigurationSource.PREFIX + "key3"));
    }
}
