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

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.context.internal.ExecutionContextProperty;

/**
 * @version $Id$
 * @since 4.3M1
 */
public class ExecutionContextPropertyTest
{
    /**
     * Access the properties via reflection. This method requires ReflectPermission suppressAccessChecks.
     *
     * @param context The execution context
     * @param key The property key
     * @return the execution context property corresponding to the given key.
     */
    @SuppressWarnings("unchecked")
    private ExecutionContextProperty fetch(ExecutionContext context, String key) throws Exception
    {
        Field propertiesField = ExecutionContext.class.getDeclaredField("properties");

        propertiesField.setAccessible(true);

        Map<String, ExecutionContextProperty> properties =
            (Map<String, ExecutionContextProperty>) propertiesField.get(context);

        return properties.get(key);
    }

    @Test
    public void defaultValues() throws Exception
    {
        final String key = "test";

        ExecutionContext context = new ExecutionContext();
        context.newProperty(key).declare();

        Assert.assertFalse(fetch(context, key).isFinal());
        Assert.assertEquals(key, fetch(context, key).getKey());
        Assert.assertNull(fetch(context, key).getValue());
        Assert.assertFalse(fetch(context, key).isInherited());

        Object o = new Object();
        context.setProperty(key, o);
        Assert.assertSame(fetch(context, key).getValue(), o);
        context.setProperty(key, null);
        Assert.assertNull(fetch(context, key).getValue());
    }

    @Test
    public void cloning() throws Exception
    {
        final String k1 = "test1";
        final String k2 = "test2";
        final String k3 = "test3";

        ExecutionContext context = new ExecutionContext();

        TestCloneable value = new TestCloneable();

        context.newProperty(k1).initial(value).declare();

        Assert.assertSame(value, fetch(context, k1).clone().getValue());

        context.newProperty(k2).initial(value).cloneValue().declare();

        TestCloneable clonedValue = (TestCloneable) fetch(context, k2).clone().getValue();

        Assert.assertTrue(value != clonedValue && clonedValue.value.equals("clone"));

        context.newProperty(k3).initial(value).cloneValue().makeFinal().inherited().declare();

        Assert.assertTrue(fetch(context, k3).clone().isClonedFrom(fetch(context, k3)));
    }

    @Test(expected = IllegalStateException.class)
    public void cloningNonPublicCloneMethod() throws Exception
    {
        ExecutionContext context = new ExecutionContext();

        final String key = "test";

        TestNonpublicClone value = new TestNonpublicClone();

        context.newProperty(key).cloneValue().initial(value).declare();

        fetch(context, key).clone();
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonNullCheck()
    {
        ExecutionContext context = new ExecutionContext();

        final String key = "test";

        context.newProperty(key).nonNull().initial(null).declare();
    }

    @Test
    public void typeChecking()
    {
        ExecutionContext context = new ExecutionContext();

        final String key = "test";

        context.newProperty(key).type(SomeClass.class).declare();

        context.setProperty(key, new SomeClass());
        context.setProperty(key, new SomeSubClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void typeCheckingMismatch()
    {
        ExecutionContext context = new ExecutionContext();

        final String key = "test";

        context.newProperty(key).type(SomeSubClass.class).declare();

        context.setProperty(key, new SomeClass());
    }

    public static class TestCloneable implements Cloneable
    {

        public String value = "original";

        @Override
        public Object clone() throws CloneNotSupportedException
        {
            TestCloneable clone = (TestCloneable) super.clone();
            clone.value = "clone";
            return clone;
        }
    }

    public static class TestNonpublicClone implements Cloneable
    {
        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            return super.clone();
        }
    }

    public static class SomeClass
    {
    }

    public static class SomeSubClass extends SomeClass
    {
    }
}
