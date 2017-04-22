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
package org.xwiki.context;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @version $Id$
 * @since 4.3M1
 */
public class ExecutionContextTest
{
    @Test
    public void inheritance()
    {
        ExecutionContext context = new ExecutionContext();
        ExecutionContext parent = new ExecutionContext();

        parent.newProperty("inherited").inherited().initial("test").declare();

        parent.newProperty("shadowed").inherited().initial("original").declare();

        context.newProperty("shadowed").inherited().initial("shadowed").declare();

        context.inheritFrom(parent);

        assertEquals("test", context.getProperty("inherited"));
        assertEquals("shadowed", context.getProperty("shadowed"));
    }

    @Test(expected = IllegalStateException.class)
    public void illegalInheritance()
    {
        ExecutionContext context = new ExecutionContext();
        ExecutionContext parent = new ExecutionContext();

        parent.newProperty("inherited").inherited().initial("test").makeFinal().declare();
        context.newProperty("inherited").inherited().initial("test").makeFinal().declare();

        context.inheritFrom(parent);
    }

    @Test
    public void getProperties()
    {
        ExecutionContext context = new ExecutionContext();
        context.setProperty("key", "value");
        Map<String, Object> properties = context.getProperties();
        assertEquals(1, properties.size());
        assertThat(properties, hasEntry("key", (Object) "value"));
    }

    @Test
    public void setProperties()
    {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        ExecutionContext context = new ExecutionContext();
        context.setProperties(properties);

        assertEquals("value1", context.getProperty("key1"));
        assertEquals("value2", context.getProperty("key2"));
    }

    @Test
    public void removeProperty()
    {
        ExecutionContext context = new ExecutionContext();
        context.setProperty("key", "value");

        assertEquals("value", context.getProperty("key"));

        context.removeProperty("key");

        assertNull(context.getProperty("key"));
    }

    @Test
    public void removeUnexistingProperty()
    {
        ExecutionContext context = new ExecutionContext();
        context.removeProperty("doesnotexist");
    }
}
