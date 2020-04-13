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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.script.internal.safe.CollectionScriptSafeProvider;
import org.xwiki.script.internal.safe.DefaultScriptSafeProvider;
import org.xwiki.script.internal.safe.MapScriptSafeProvider;
import org.xwiki.script.internal.safe.ScriptSafeProvider;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

@ComponentList({ CollectionScriptSafeProvider.class, MapScriptSafeProvider.class })
public class DefaultScriptSafeProviderTest
{
    @Rule
    public MockitoComponentMockingRule<ScriptSafeProvider> mocker =
        new MockitoComponentMockingRule<>(DefaultScriptSafeProvider.class);

    @Test
    public void testGetWithNoProvider() throws Exception
    {
        Object safe = "";

        Assert.assertSame(safe, this.mocker.getComponentUnderTest().get(safe));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGetCollection() throws Exception
    {
        // List

        Collection unsafe = Arrays.asList("1", "2");
        Collection safe = (Collection) this.mocker.getComponentUnderTest().get(unsafe);

        Assert.assertNotSame(unsafe, safe);
        Assert.assertTrue(safe instanceof List);
        Assert.assertEquals(unsafe, safe);

        // Set

        unsafe = new LinkedHashSet(Arrays.asList("1", "2", "3", "4", "5"));
        safe = (Collection) this.mocker.getComponentUnderTest().get(unsafe);

        Assert.assertNotSame(unsafe, safe);
        Assert.assertTrue(safe instanceof Set);
        Assert.assertEquals(unsafe, safe);
        // Make sure order is kept
        Assert.assertEquals(unsafe.toString(), safe.toString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked", "cast" })
    @Test
    public void testGetMap() throws Exception
    {
        Map unsafe = new LinkedHashMap(5);
        unsafe.put("1", "1");
        unsafe.put("2", "2");
        unsafe.put("3", "3");
        unsafe.put("4", "4");
        unsafe.put("5", "5");
        Map safe = (Map) this.mocker.getComponentUnderTest().get(unsafe);

        Assert.assertNotSame(unsafe, safe);
        Assert.assertTrue(safe instanceof Map);
        Assert.assertEquals(unsafe, safe);
        // Make sure order is kept
        Assert.assertEquals(unsafe.toString(), safe.toString());
    }
}
