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

import org.junit.jupiter.api.Test;
import org.xwiki.context.internal.ExecutionContextProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @version $Id$
 * @since 4.3M1
 */
class ExecutionContextPropertyTest
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
    void defaultValues() throws Exception
    {
        final String key = "test";

        ExecutionContext context = new ExecutionContext();
        context.newProperty(key).declare();

        assertFalse(fetch(context, key).isFinal());
        assertEquals(key, fetch(context, key).getKey());
        assertNull(fetch(context, key).getValue());
        assertFalse(fetch(context, key).isInherited());

        Object o = new Object();
        context.setProperty(key, o);
        assertSame(fetch(context, key).getValue(), o);
        context.setProperty(key, null);
        assertNull(fetch(context, key).getValue());
    }

    @Test
    void cloning() throws Exception
    {
        final String k1 = "test1";
        final String k2 = "test2";
        final String k3 = "test3";

        ExecutionContext context = new ExecutionContext();

        TestCloneable value = new TestCloneable();

        context.newProperty(k1).initial(value).declare();

        assertSame(value, fetch(context, k1).clone().getValue());

        context.newProperty(k2).initial(value).cloneValue().declare();

        TestCloneable clonedValue = (TestCloneable) fetch(context, k2).clone().getValue();

        assertTrue(value != clonedValue && clonedValue.value.equals("clone"));

        context.newProperty(k3).initial(value).cloneValue().makeFinal().inherited().declare();

        assertTrue(fetch(context, k3).clone().isClonedFrom(fetch(context, k3)));
    }

    @Test
    void cloningNonPublicCloneMethod() throws Exception
    {
        ExecutionContext context = new ExecutionContext();

        String key = "test";

        TestNonpublicClone value = new TestNonpublicClone();

        context.newProperty(key).cloneValue().initial(value).declare();

        ExecutionContextProperty ecp = fetch(context, key);
        Throwable exception = assertThrows(IllegalStateException.class, () -> ecp.clone());
        assertEquals("cloneValue attribute was set on property [test], but the value had class "
            + "[org.xwiki.context.ExecutionContextPropertyTest$TestNonpublicClone] which has no public clone method",
            exception.getMessage());
    }

    @Test
    void nonNullCheck()
    {
        ExecutionContext context = new ExecutionContext();

        final String key = "test";

        ExecutionContext.DeclarationBuilder db = context.newProperty(key).nonNull().initial(null);
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> db.declare());
        assertEquals("The property [test] may not be null!", exception.getMessage());
    }

    @Test
    void typeCheckingOk()
    {
        ExecutionContext context = new ExecutionContext();

        final String key = "test";

        context.newProperty(key).type(SomeClass.class).declare();

        context.setProperty(key, new SomeClass());
        context.setProperty(key, new SomeSubClass());
    }

    @Test
    void typeCheckingMismatch()
    {
        ExecutionContext context = new ExecutionContext();

        final String key = "test";

        context.newProperty(key).type(SomeSubClass.class).declare();

        Object value = new SomeClass();
        Throwable exception = assertThrows(IllegalArgumentException.class,
            () ->  context.setProperty(key, value));
        assertEquals("The value of property [test] must be of type "
            + "[class org.xwiki.context.ExecutionContextPropertyTest$SomeSubClass], but was "
            + "[class org.xwiki.context.ExecutionContextPropertyTest$SomeClass]", exception.getMessage());
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
