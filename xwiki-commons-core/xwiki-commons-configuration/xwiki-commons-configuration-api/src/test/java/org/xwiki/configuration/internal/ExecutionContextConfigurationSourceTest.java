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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

/**
 * Unit tests for {@link ExecutionContextConfigurationSource}.
 */
@ComponentTest
class ExecutionContextConfigurationSourceTest
{
    @InjectMockComponents
    private ExecutionContextConfigurationSource source;

    @MockComponent
    private Execution execution;

    private ExecutionContext executionContext = new ExecutionContext();

    @Test
    void withoutExecutionContext()
    {
        // Read shouldn't throw an exception.
        assertTrue(this.source.isEmpty());
        assertTrue(this.source.getKeys().isEmpty());
        assertFalse(this.source.containsKey("foo"));
        assertTrue(this.source.getProperties().isEmpty());
        assertNull(this.source.getProperty("foo"));
        assertEquals(13, this.source.getProperty("foo", 13));
        assertNull(this.source.getProperty("foo", Integer.class));
        assertEquals(Collections.emptyList(), this.source.getProperty("foo", List.class));
        assertEquals(new Properties(), this.source.getProperty("foo", Properties.class));
        assertEquals(13, this.source.getProperty("foo", Integer.class, 13));
        assertNull(this.source.removeProperty("foo"));

        // Write should throw an exception.
        try {
            this.source.setProperty("foo", "bar");
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        try {
            this.source.setProperties(Map.of("foo", "bar"));
            fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    void withExecutionContext() throws Exception
    {
        when(this.execution.getContext()).thenReturn(this.executionContext);

        assertTrue(this.source.isEmpty());
        this.source.setProperty("foo", "bar");
        this.source.setProperty("age", 13);
        assertFalse(this.source.isEmpty());
        assertTrue(this.source.containsKey("foo"));
        assertTrue(this.source.containsKey("age"));
        assertFalse(this.source.containsKey("count"));
        assertEquals("bar", this.source.getProperty("foo"));
        assertEquals(13, this.source.getProperty("age", 27));
        assertEquals("13", this.source.getProperty("age", "27"));
        assertEquals("13", this.source.getProperty("age", String.class));
        assertEquals(27, this.source.getProperty("count", Integer.class, 27));
        assertEquals(Arrays.asList("foo", "age"), this.source.getKeys());
        assertEquals(2, this.source.getProperties().size());

        assertEquals("bar", this.source.removeProperty("foo"));
        assertNull(this.source.removeProperty("foo"));
        assertFalse(this.source.containsKey("foo"));
        assertEquals(1, this.source.getProperties().size());

        this.source.setProperty("foo", false);
        assertEquals(false, this.source.getProperty("foo"));

        this.source.setProperties(Map.of("color", "blue", "status", 3));
        assertEquals("blue", this.source.getProperty("color"));
        assertEquals(3, this.source.getProperty("status", 5));
        assertEquals(2, this.source.getProperties().size());

        assertEquals("blue", this.source.removeProperty("color"));

        ExecutionContext clonedContext = new ExecutionContext();
        clonedContext.inheritFrom(this.executionContext);
        when(this.execution.getContext()).thenReturn(clonedContext);

        assertEquals(1, this.source.getProperties().size());
        assertEquals(3, this.source.getProperty("status", 5));
    }
}
