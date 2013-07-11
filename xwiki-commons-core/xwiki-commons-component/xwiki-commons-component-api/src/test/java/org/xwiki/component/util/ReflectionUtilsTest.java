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
package org.xwiki.component.util;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentRole;

/**
 * Unit tests for {@link ReflectionUtils}.
 * 
 * @version $Id$
 * @since 3.5M1
 */
public class ReflectionUtilsTest
{
    private static interface TestInterface<A, B>
    {

    }

    private static interface TestInterface2<A, B> extends TestInterface<A, B>
    {

    }

    private static class TestClass<A, B> implements TestInterface2<A, B>
    {

    }

    private static class TestClass2<A> extends TestClass<A, Integer>
    {

    }

    private static class TestClass3 extends TestClass2<List<String>>
    {

    }

    private class AbstractTestFieldClass
    {
        @SuppressWarnings("unused")
        private Object superField;
    }

    private class TestFieldClass extends AbstractTestFieldClass
    {
        @SuppressWarnings("unused")
        private Object field;
    }

    @Test
    public void testGetField() throws Exception
    {
        Field field = ReflectionUtils.getField(TestFieldClass.class, "field");
        Assert.assertNotNull(field);
        Assert.assertEquals("field", field.getName());
    }

    @Test
    public void testGetFieldFromSuperClass() throws Exception
    {
        Field field = ReflectionUtils.getField(TestFieldClass.class, "superField");
        Assert.assertNotNull(field);
        Assert.assertEquals("superField", field.getName());
    }

    @Test
    public void testGetFieldWhenDoesntExist()
    {
        try {
            ReflectionUtils.getField(TestFieldClass.class, "doesntexist");
            Assert.fail();
        } catch (NoSuchFieldException expected) {
            Assert.assertEquals("No field named [doesntexist] in class [" + TestFieldClass.class.getName()
                + "] or superclasses", expected.getMessage());
        }
    }

    @Test
    public void testUnserializeType() throws Exception
    {
        Type simpleType = ComponentRole.class;
        Assert.assertEquals(simpleType, ReflectionUtils.unserializeType("org.xwiki.component.descriptor.ComponentRole",
            Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void testUnserializeTypeWithGenerics() throws Exception
    {
        Type genericsType = new DefaultParameterizedType(null, ComponentRole.class, String.class);
        Assert.assertEquals(genericsType, ReflectionUtils.unserializeType(
            "org.xwiki.component.descriptor.ComponentRole<java.lang.String>", Thread.currentThread()
                .getContextClassLoader()));
    }

    @Test
    public void testUnserializeListType() throws Exception
    {
        Type listType = new DefaultParameterizedType(null, java.util.List.class, ComponentRole.class);
        Assert.assertEquals(listType, ReflectionUtils.unserializeType(
            "java.util.List<org.xwiki.component.descriptor.ComponentRole>", Thread.currentThread()
                .getContextClassLoader()));
    }

    @Test
    public void testUnserializeMapType() throws Exception
    {
        Type mapType = new DefaultParameterizedType(null, java.util.Map.class, String.class, ComponentRole.class);
        Assert.assertEquals(mapType, ReflectionUtils.unserializeType("java.util.Map<java.lang.String, "
            + "org.xwiki.component.descriptor.ComponentRole>", Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void testUnserializeMapTypeWithGenerics() throws Exception
    {
        Type annotatedType = new DefaultParameterizedType(null, ComponentRole.class, String.class);
        Type mapType = new DefaultParameterizedType(null, java.util.Map.class, String.class, annotatedType);
        Assert.assertEquals(mapType, ReflectionUtils.unserializeType(
            "java.util.Map<java.lang.String, org.xwiki.component.descriptor.ComponentRole<java.lang.String>>", Thread
                .currentThread().getContextClassLoader()));
    }

    @Test
    public void testUnserializeMapInMapWithTypeWithGenerics() throws Exception
    {
        Type annotatedType = new DefaultParameterizedType(null, ComponentRole.class, String.class);
        Type mapType1 = new DefaultParameterizedType(null, java.util.Map.class, String.class, annotatedType);
        Type mapType2 = new DefaultParameterizedType(null, java.util.Map.class, String.class, mapType1);
        Assert.assertEquals(mapType2, ReflectionUtils.unserializeType(
            "java.util.Map<java.lang.String, java.util.Map<java.lang.String, "
                + "org.xwiki.component.descriptor.ComponentRole<java.lang.String>>>", Thread.currentThread()
                .getContextClassLoader()));
    }

    @Test
    public void testGetAllFields()
    {
        Collection<Field> fields = ReflectionUtils.getAllFields(TestFieldClass.class);

        Assert.assertEquals(2, fields.size());
    }

    @Test
    public void testGetTypeClass()
    {
        Assert.assertSame(TestFieldClass.class, ReflectionUtils.getTypeClass(TestFieldClass.class));
        Assert
            .assertSame(TestFieldClass.class, ReflectionUtils.getTypeClass(new DefaultParameterizedType(
                ReflectionUtilsTest.class, TestFieldClass.class)));
        // TODO: Missing test on GenericArrayType
    }

    @Test
    public void testResolveType()
    {
        Assert.assertEquals(new DefaultParameterizedType(ReflectionUtilsTest.class, TestInterface.class,
            new DefaultParameterizedType(null, List.class, String.class), Integer.class), ReflectionUtils.resolveType(
            TestInterface.class, TestClass3.class));
    }
}
