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
package org.xwiki.extension.repository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link DefaultExtensionRepositoryDescriptor}.
 * 
 * @version $Id$
 * @since 10.7RC1
 */
public class DefaultExtensionRepositoryDescriptorTest
{
    @Test
    public void equals() throws URISyntaxException
    {
        assertEquals(new DefaultExtensionRepositoryDescriptor("id", "type", new URI("uri:uri")),
            new DefaultExtensionRepositoryDescriptor("id", "type", new URI("uri:uri")));

        assertNotEquals(new DefaultExtensionRepositoryDescriptor("id", "type", new URI("uri:uri")),
            new DefaultExtensionRepositoryDescriptor("id2", "type", new URI("uri:uri")));
        assertNotEquals(new DefaultExtensionRepositoryDescriptor("id", "type", new URI("uri:uri")),
            new DefaultExtensionRepositoryDescriptor("id", "type2", new URI("uri:uri")));
        assertNotEquals(new DefaultExtensionRepositoryDescriptor("id", "type", new URI("uri:uri")),
            new DefaultExtensionRepositoryDescriptor("id", "type", new URI("uri:uri2")));

        DefaultExtensionRepositoryDescriptor descriptor1 = new DefaultExtensionRepositoryDescriptor("id");
        DefaultExtensionRepositoryDescriptor descriptor2 = new DefaultExtensionRepositoryDescriptor("id");

        descriptor1.putProperty("key", "value");

        assertNotEquals(descriptor1, descriptor2);

        descriptor2.putProperty("key", "value");

        assertEquals(descriptor1, descriptor2);
    }

    @Test
    public void properties()
    {
        DefaultExtensionRepositoryDescriptor descriptor = new DefaultExtensionRepositoryDescriptor("id");

        Map<String, String> properties = new HashMap<>();

        properties.put("key1", "value1");

        descriptor.setProperties(properties);

        assertNotSame(properties, descriptor.getProperties());
        assertEquals(properties, descriptor.getProperties());
        assertEquals("value1", descriptor.getProperty("key1"));
        assertNull(descriptor.getProperty("nokey"));

        descriptor.putProperty("key2", "value2");

        assertNotEquals(properties, descriptor.getProperties());

        properties.put("key2", "value2");

        assertEquals(properties, descriptor.getProperties());
    }
}
