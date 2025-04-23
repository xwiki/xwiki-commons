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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * Validate {@link SystemEnvConfigurationSource}.
 * 
 * @version $Id$
 */
@ComponentTest
class SystemEnvConfigurationSourceTest
{
    public static class TestSystemEnvConfigurationSource extends SystemEnvConfigurationSource
    {
        private final Map<String, String> env = new LinkedHashMap<>();

        @Override
        Map<String, String> getenv()
        {
            return env;
        }

        @Override
        String getenv(String name)
        {
            return env.get(name);
        }
    }

    @InjectMockComponents
    private TestSystemEnvConfigurationSource configuration;

    @MockComponent
    private ConverterManager converterManager;

    @BeforeEach
    void beforeEach()
    {
        when(this.converterManager.convert(Integer.class, "14")).thenReturn(14);
        when(this.converterManager.convert(Integer.class, "15")).thenReturn(15);
    }

    @Test
    void getProperty()
    {
        assertNull(this.configuration.getProperty(null));
        assertNull(this.configuration.getProperty("doesnotexist"));
        assertNull(this.configuration.getProperty("key", Integer.class));
        assertEquals(1, this.configuration.getProperty("key", 1));

        this.configuration.getenv().put("key", "value");

        assertNull(this.configuration.getProperty("key"));

        this.configuration.getenv().put(SystemEnvConfigurationSource.PREFIX + "key", "14");
        this.configuration.getenv().put(SystemEnvConfigurationSource.PREFIX + "KEY", "15");

        assertEquals("14", this.configuration.getProperty("key"));
        assertEquals("15", this.configuration.getProperty("KEY"));

        assertEquals(14, this.configuration.getProperty("key", 1));
        assertEquals(14, this.configuration.getProperty("key", Integer.class));
        assertEquals(14, this.configuration.getProperty("key", Integer.class, 1));

        this.configuration.getenv().put(SystemEnvConfigurationSource.PREFIX + "key1_KEY2_key3_KEY4", "15");

        assertEquals("15", this.configuration.getProperty("key1:KEY2_key3.KEY4"));

        this.configuration.getenv().put(SystemEnvConfigurationSource.PREFIX + "key1_KEY2_key3_KEY4", "15");

        assertEquals("15", this.configuration.getProperty("key1:KEY2_key3.KEY4"));

        this.configuration.getenv().put(SystemEnvConfigurationSource.PREFIX + "_31Key_40", "value");

        assertEquals("value", this.configuration.getProperty("1Key@"));
    }

    @Test
    void getKeys()
    {
        assertEquals(List.of(), this.configuration.getKeys());
        assertEquals(List.of(), this.configuration.getKeys("prefix"));

        this.configuration.getenv().put("key", "value");

        assertEquals(List.of(), this.configuration.getKeys());
        assertEquals(List.of(), this.configuration.getKeys("prefix"));
        assertEquals(List.of(), this.configuration.getKeys("key"));

        this.configuration.getenv().put(SystemEnvConfigurationSource.PREFIX + "key", "15");

        assertEquals(List.of("key"), this.configuration.getKeys());
        assertEquals(List.of(), this.configuration.getKeys("prefix"));
        assertEquals(List.of("key"), this.configuration.getKeys("k"));
        assertEquals(List.of("key"), this.configuration.getKeys("key"));

        this.configuration.getenv().put(SystemEnvConfigurationSource.PREFIX + "key1_KEY2_key3_KEY4", "15");

        assertEquals(List.of("key", "key1.KEY2.key3.KEY4"), this.configuration.getKeys());
        assertEquals(List.of(), this.configuration.getKeys("prefix"));
        assertEquals(List.of("key1.KEY2.key3.KEY4"), this.configuration.getKeys("key1."));
    }

    @Test
    void isEmpty()
    {
        assertTrue(this.configuration.isEmpty());
        assertTrue(this.configuration.isEmpty("prefix"));

        this.configuration.getenv().put("key", "value");

        assertTrue(this.configuration.isEmpty());
        assertTrue(this.configuration.isEmpty("prefix"));
        assertTrue(this.configuration.isEmpty("key"));

        this.configuration.getenv().put(SystemEnvConfigurationSource.PREFIX + "key", "15");

        assertFalse(this.configuration.isEmpty());
        assertTrue(this.configuration.isEmpty("prefix"));
        assertFalse(this.configuration.isEmpty("key"));

        this.configuration.getenv().put(SystemEnvConfigurationSource.PREFIX + "key1_KEY2_key3_KEY4", "15");

        assertFalse(this.configuration.isEmpty());
        assertTrue(this.configuration.isEmpty("prefix"));
        assertFalse(this.configuration.isEmpty("key1."));
    }
}
