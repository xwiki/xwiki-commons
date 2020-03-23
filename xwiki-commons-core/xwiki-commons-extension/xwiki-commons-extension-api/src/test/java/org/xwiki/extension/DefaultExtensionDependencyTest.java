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
package org.xwiki.extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link DefaultExtensionDependency}.
 * 
 * @version $Id$
 */
public class DefaultExtensionDependencyTest
{
    @Test
    public void properties()
    {
        DefaultExtensionDependency dependency =
            new DefaultExtensionDependency("id", new DefaultVersionConstraint("version"));

        Map<String, Object> properties = new HashMap<>();

        properties.put("key1", "value1");

        dependency.setProperties(properties);

        assertNotSame(properties, dependency.getProperties());
        assertEquals(properties, dependency.getProperties());
        assertEquals("value1", dependency.getProperty("key1"));
        assertNull(dependency.getProperty("nokey"));
        assertEquals("default", dependency.getProperty("nokey", "default"));

        dependency.putProperty("key2", "value2");

        assertNotEquals(properties, dependency.getProperties());

        properties.put("key2", "value2");

        assertEquals(properties, dependency.getProperties());
    }

    @Test
    public void equals()
    {
        assertEquals(new DefaultExtensionDependency("id", new DefaultVersionConstraint("version")),
            new DefaultExtensionDependency("id", new DefaultVersionConstraint("version")));

        assertNotEquals(new DefaultExtensionDependency("id", new DefaultVersionConstraint("version")),
            new DefaultExtensionDependency("id2", new DefaultVersionConstraint("version")));

        assertNotEquals(new DefaultExtensionDependency("id", new DefaultVersionConstraint("version")),
            new DefaultExtensionDependency("id", new DefaultVersionConstraint("version2")));
    }

    @Test
    public void isCompatibleWithExtensionId()
    {
        DefaultExtensionDependency dependency =
            new DefaultExtensionDependency("id", new DefaultVersionConstraint("[1.0, 2.0]"));

        assertTrue(dependency.isCompatible(new ExtensionId("id", "1.0")));
        assertTrue(dependency.isCompatible(new ExtensionId("id", "1.1")));
        assertTrue(dependency.isCompatible(new ExtensionId("id", "2.0")));

        assertFalse(dependency.isCompatible(new ExtensionId("id2", "1.0")));
        assertFalse(dependency.isCompatible(new ExtensionId("id", "0.9")));
        assertFalse(dependency.isCompatible(new ExtensionId("id", "3.0")));
    }

    @Test
    public void isCompatibleWithExtension()
    {
        DefaultExtensionDependency dependency =
            new DefaultExtensionDependency("id", new DefaultVersionConstraint("[1.0, 2.0]"));

        assertTrue(
            dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id", "1.0"), "type")));
        assertTrue(
            dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id", "1.1"), "type")));
        assertTrue(
            dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id", "2.0"), "type")));

        assertFalse(
            dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id", "0.9"), "type")));
        assertFalse(
            dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id", "3.0"), "type")));

        assertTrue(dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id2", "1.0"),
            "type", new ExtensionId("id", "1.0"))));
        assertTrue(dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id2", "1.0"),
            "type", new ExtensionId("id", "1.1"))));
        assertTrue(dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id2", "1.0"),
            "type", new ExtensionId("id", "2.0"))));

        assertFalse(dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id2", "1.0"),
            "type", new ExtensionId("id", "0.9"))));
        assertFalse(dependency.isCompatible(new AbstractExtensionTest.TestExtension(new ExtensionId("id2", "1.0"),
            "type", new ExtensionId("id", "3.0"))));
    }

    @Test
    public void repositories()
    {
        DefaultExtensionDependency dependency =
            new DefaultExtensionDependency("id", new DefaultVersionConstraint("version"));

        assertTrue(dependency.getRepositories().isEmpty());

        dependency.setRepositories(Arrays.asList(AbstractExtensionTest.DESCRIPTOR1));

        assertEquals(Arrays.asList(AbstractExtensionTest.DESCRIPTOR1), dependency.getRepositories());

        dependency.addRepository(AbstractExtensionTest.DESCRIPTOR2);

        assertEquals(Arrays.asList(AbstractExtensionTest.DESCRIPTOR1, AbstractExtensionTest.DESCRIPTOR2),
            dependency.getRepositories());
    }

    @Test
    public void addExclusion()
    {
        DefaultExtensionDependency dependency =
            new DefaultExtensionDependency("id", new DefaultVersionConstraint("version"));

        assertTrue(dependency.getExclusions().isEmpty());

        dependency.addExclusion(new DefaultExtensionPattern("excludeddependency"));

        assertEquals(1, dependency.getExclusions().size());
    }
}
