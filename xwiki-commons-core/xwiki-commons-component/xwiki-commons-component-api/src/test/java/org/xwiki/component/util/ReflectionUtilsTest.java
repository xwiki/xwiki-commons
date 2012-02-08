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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link ReflectionUtils}.
 *
 * @version $Id$ 
 * @since 3.5M1
 */
public class ReflectionUtilsTest
{
    private class AbstractTestFieldClass
    {
        private Object superField;
    }
    
    private class TestFieldClass extends AbstractTestFieldClass
    {
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
            Assert.assertEquals("No field named [doesntexist] in class ["
                + TestFieldClass.class.getName() + "] or superclasses", expected.getMessage());
        }
    }
}
