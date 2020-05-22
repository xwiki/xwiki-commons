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
package org.xwiki.component.internal.namespace;

import org.junit.jupiter.api.Test;
import org.xwiki.component.namespace.Namespace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link Namespace}.
 * 
 * @version $Id$
 */
public class NamespaceTest
{
    @Test
    void equalsAndHashCode()
    {
        Namespace ns1 = new Namespace("type1", "value1");
        assertEquals(ns1, ns1);
        assertEquals(ns1.hashCode(), ns1.hashCode());

        Namespace ns2 = new Namespace("type2", "value1");
        assertNotEquals(ns1, ns2);
        assertNotEquals(ns1.hashCode(), ns2.hashCode());

        Namespace ns3 = new Namespace("type1", "value2");
        assertNotEquals(ns1, ns3);
        assertNotEquals(ns1.hashCode(), ns3.hashCode());

        Namespace ns4 = new Namespace("type1", "value1");
        assertEquals(ns1, ns4);
        assertEquals(ns1.hashCode(), ns4.hashCode());

        assertNotEquals(ns1, "not a NameSpace instance");
    }

    @Test
    void serialize()
    {
        Namespace ns = new Namespace("type", "value");
        assertEquals("type:value", ns.serialize());
    }

    @Test
    void serializeWhenValueIsNull()
    {
        Namespace ns = new Namespace(null, null);
        assertNull(ns.serialize());
    }

    @Test
    void serializeWhenTypeIsNull()
    {
        Namespace ns = new Namespace(null, "value");
        assertEquals("value", ns.serialize());
    }

    @Test
    void serializeWhenBackslashCharacterInTypeAndValue()
    {
        Namespace ns = new Namespace("t\\:pe", "v\\:alue");
        assertEquals("t\\\\\\:pe:v\\:alue", ns.serialize());
    }

    @Test
    void testToString()
    {
        Namespace ns = new Namespace("type", "value");
        assertEquals("type:value", ns.toString());
    }
}
