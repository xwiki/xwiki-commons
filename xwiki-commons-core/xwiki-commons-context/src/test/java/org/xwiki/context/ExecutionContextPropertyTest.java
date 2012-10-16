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

import org.junit.Test;
import org.junit.Assert;

/**
 * @version $Id$ 
 * @since 4.3M1
 */
public class ExecutionContextPropertyTest
{

    @Test
    public void defaultValues() throws Exception
    {
        ExecutionContextProperty property = new ExecutionContextProperty("test");

        Assert.assertFalse(property.isReadonly());
        Assert.assertTrue("test".equals(property.getKey()));
        Assert.assertTrue(null == property.getValue());
        Assert.assertFalse(property.isInherited());

        Object o = new Object();
        property.setValue(o);
        Assert.assertTrue(property.getValue() == o);
        property.setValue(null);
        Assert.assertTrue(property.getValue() == null);
    }

    @Test
    public void cloning() throws Exception
    {
        ExecutionContextProperty property = new ExecutionContextProperty("test");

        TestCloneable value = new TestCloneable();

        property.setValue(value);

        ExecutionContextProperty clone = property.clone();

        Assert.assertTrue(value == clone.getValue());

        property.setCloneValue(true);

        clone = property.clone();

        Assert.assertTrue(value != clone.getValue() && ((TestCloneable) clone.getValue()).value.equals("clone"));

        property.setReadonly(true);
        property.setInherited(true);

        clone = property.clone();

        Assert.assertTrue(clone.isClonedFrom(property));
    }

    @Test(expected=IllegalStateException.class)
    public void cloningNonPublicCloneMethod()
    {
        ExecutionContextProperty property = new ExecutionContextProperty("test");

        TestNonpublicClone value = new TestNonpublicClone();

        property.setCloneValue(true);
        property.setValue(value);

        property.clone();
    }

    @Test
    public void assertClonedFrom()
    {
        ExecutionContextProperty property = new ExecutionContextProperty("test");

        property.setReadonly(true);
        property.setInherited(true);

        ExecutionContextProperty clone = property.clone();

        Assert.assertFalse(clone.isClonedFrom(clone));
    }

    @Test(expected=IllegalArgumentException.class)
    public void nonNullCheck()
    {
        ExecutionContextProperty property = new ExecutionContextProperty("test");

        property.setNonNull(true);

        property.setValue(null);
    }

    @Test
    public void typeChecking()
    {
        ExecutionContextProperty property = new ExecutionContextProperty("test");

        property.setType(SomeClass.class);

        property.setValue(new SomeClass());
        property.setValue(new SomeSubClass());
    }

    @Test(expected=IllegalArgumentException.class)
    public void typeCheckingMismatch()
    {
        ExecutionContextProperty property = new ExecutionContextProperty("test");

        property.setType(SomeSubClass.class);

        property.setValue(new SomeClass());
    }

    private static class TestCloneable implements Cloneable
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

    private static class TestNonpublicClone implements Cloneable
    {
        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            return super.clone();
        }
    }

    private static class SomeClass
    {
    }

    private static class SomeSubClass extends SomeClass
    {
    }

}
