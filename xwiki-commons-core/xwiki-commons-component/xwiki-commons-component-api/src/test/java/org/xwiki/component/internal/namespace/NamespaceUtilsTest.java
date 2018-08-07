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
import org.xwiki.component.namespace.NamespaceUtils;

import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link NamespaceUtils}.
 * 
 * @version $Id$
 */
public class NamespaceUtilsTest
{
    @Test
    public void getPrefix()
    {
        assertNull(NamespaceUtils.getPrefix(null));
        assertNull(NamespaceUtils.getPrefix("namespace"));
        assertEquals("type", NamespaceUtils.getPrefix("type:value"));
        assertEquals("t:pe", NamespaceUtils.getPrefix("t\\:pe:value"));
        assertEquals("", NamespaceUtils.getPrefix(":value"));
    }

    @Test
    public void toNamespace()
    {
        assertNull(NamespaceUtils.toNamespace(null));
        assertEquals(new Namespace(null, "namespace"), NamespaceUtils.toNamespace("namespace"));
        assertEquals(new Namespace("type", "value"), NamespaceUtils.toNamespace("type:value"));
        assertEquals(new Namespace("t:pe", "val\\ue"), NamespaceUtils.toNamespace("t\\:pe:val\\ue"));
        assertEquals(new Namespace("", "value"), NamespaceUtils.toNamespace(":value"));
        assertEquals(Namespace.ROOT, NamespaceUtils.toNamespace("{}"));
    }

    @Test
    public void testEquals()
    {
        Namespace ns1 = new Namespace("type1", "value1");
        assertEquals(ns1, ns1);

        Namespace ns2 = new Namespace("type2", "value1");
        assertNotEquals(ns1, ns2);

        Namespace ns3 = new Namespace("type1", "value2");
        assertNotEquals(ns1, ns3);

        Namespace ns4 = new Namespace("type1", "value1");
        assertEquals(ns1, ns4);
    }
}
