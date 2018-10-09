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

import org.junit.jupiter.api.Test;
import org.xwiki.component.descriptor.ComponentRole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ReflectionUtils}.
 *
 * @version $Id$
 */
public class ReflectionUtilsTest
{
    private static interface TestInterfaceSimple
    {
    }

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

    private static class TestClass4<T> extends TestClass2<T> implements TestInterfaceSimple
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
    public void getField() throws Exception
    {
        Field field = ReflectionUtils.getField(TestFieldClass.class, "field");
        assertNotNull(field);
        assertEquals("field", field.getName());
    }

    @Test
    public void getFieldFromSuperClass() throws Exception
    {
        Field field = ReflectionUtils.getField(TestFieldClass.class, "superField");
        assertNotNull(field);
        assertEquals("superField", field.getName());
    }

    @Test
    public void getFieldWhenDoesntExist()
    {
        Throwable exception = assertThrows(NoSuchFieldException.class, () -> {
            ReflectionUtils.getField(TestFieldClass.class, "doesntexist");
        });
        assertEquals("No field named [doesntexist] in class [" + TestFieldClass.class.getName() + "] or superclasses",
            exception.getMessage());
    }

    @Test
    public void unserializeType() throws Exception
    {
        Type simpleType = ComponentRole.class;
        assertEquals(simpleType, ReflectionUtils.unserializeType("org.xwiki.component.descriptor.ComponentRole",
            Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void unserializeTypeWithGenerics() throws Exception
    {
        Type genericsType = new DefaultParameterizedType(null, ComponentRole.class, String.class);
        assertEquals(genericsType,
            ReflectionUtils.unserializeType("org.xwiki.component.descriptor.ComponentRole<java.lang.String>",
                Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void unserializeListType() throws Exception
    {
        Type listType = new DefaultParameterizedType(null, java.util.List.class, ComponentRole.class);
        assertEquals(listType,
            ReflectionUtils.unserializeType("java.util.List<org.xwiki.component.descriptor.ComponentRole>",
                Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void unserializeMapType() throws Exception
    {
        Type mapType = new DefaultParameterizedType(null, java.util.Map.class, String.class, ComponentRole.class);
        assertEquals(mapType,
            ReflectionUtils.unserializeType(
                "java.util.Map<java.lang.String, " + "org.xwiki.component.descriptor.ComponentRole>",
                Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void unserializeMapTypeWithGenerics() throws Exception
    {
        Type annotatedType = new DefaultParameterizedType(null, ComponentRole.class, String.class);
        Type mapType = new DefaultParameterizedType(null, java.util.Map.class, String.class, annotatedType);
        assertEquals(mapType,
            ReflectionUtils.unserializeType(
                "java.util.Map<java.lang.String, org.xwiki.component.descriptor.ComponentRole<java.lang.String>>",
                Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void testUnserializeMapInMapWithTypeWithGenerics() throws Exception
    {
        Type annotatedType = new DefaultParameterizedType(null, ComponentRole.class, String.class);
        Type mapType1 = new DefaultParameterizedType(null, java.util.Map.class, String.class, annotatedType);
        Type mapType2 = new DefaultParameterizedType(null, java.util.Map.class, String.class, mapType1);
        assertEquals(mapType2,
            ReflectionUtils.unserializeType(
                "java.util.Map<java.lang.String, java.util.Map<java.lang.String, "
                    + "org.xwiki.component.descriptor.ComponentRole<java.lang.String>>>",
                Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void getAllFields()
    {
        Collection<Field> fields = ReflectionUtils.getAllFields(TestFieldClass.class);

        assertEquals(2, fields.size());
    }

    @Test
    public void getTypeClass()
    {
        assertSame(TestFieldClass.class, ReflectionUtils.getTypeClass(TestFieldClass.class));
        assertSame(TestFieldClass.class, ReflectionUtils
            .getTypeClass(new DefaultParameterizedType(ReflectionUtilsTest.class, TestFieldClass.class)));
        // TODO: Missing test on GenericArrayType
    }

    @Test
    public void resolveType()
    {
        assertEquals(
            new DefaultParameterizedType(ReflectionUtilsTest.class, TestInterface.class,
                new DefaultParameterizedType(null, List.class, String.class), Integer.class),
            ReflectionUtils.resolveType(TestInterface.class, TestClass3.class));

        assertEquals(TestInterfaceSimple.class,
            ReflectionUtils.resolveType(TestInterfaceSimple.class, TestClass4.class));

        assertEquals(TestInterfaceSimple.class, ReflectionUtils.resolveType(TestInterfaceSimple.class,
            new DefaultParameterizedType(ReflectionUtilsTest.class, TestClass4.class, String.class)));
    }
}
