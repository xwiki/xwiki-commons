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
package org.xwiki.script.internal.safe;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentTest
// @formatter:off
@ComponentList({ 
    CollectionScriptSafeProvider.class, 
    MapScriptSafeProvider.class 
})
// @formatter:on
class DefaultScriptSafeProviderTest
{
    @InjectMockComponents
    private DefaultScriptSafeProvider scriptSafeProvider;

    @Test
    void getWithNoProvider()
    {
        Object safe = "";

        assertSame(safe, this.scriptSafeProvider.get(safe));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void getCollection()
    {
        // List

        Collection unsafe = Arrays.asList("1", "2");
        Collection safe = (Collection) this.scriptSafeProvider.get(unsafe);

        assertNotSame(unsafe, safe);
        assertTrue(safe instanceof List);
        assertEquals(unsafe, safe);

        // Set

        unsafe = new LinkedHashSet(Arrays.asList("1", "2", "3", "4", "5"));
        safe = (Collection) this.scriptSafeProvider.get(unsafe);

        assertNotSame(unsafe, safe);
        assertTrue(safe instanceof Set);
        assertEquals(unsafe, safe);
        // Make sure order is kept
        assertEquals(unsafe.toString(), safe.toString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "cast" })
    @Test
    void getMap()
    {
        Map unsafe = new LinkedHashMap(5);
        unsafe.put("1", "1");
        unsafe.put("2", "2");
        unsafe.put("3", "3");
        unsafe.put("4", "4");
        unsafe.put("5", "5");
        Map safe = (Map) this.scriptSafeProvider.get(unsafe);

        assertNotSame(unsafe, safe);
        assertTrue(safe instanceof Map);
        assertEquals(unsafe, safe);
        // Make sure order is kept
        assertEquals(unsafe.toString(), safe.toString());
    }
}
