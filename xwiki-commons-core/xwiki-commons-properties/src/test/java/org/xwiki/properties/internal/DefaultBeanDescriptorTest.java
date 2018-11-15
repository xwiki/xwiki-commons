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
package org.xwiki.properties.internal;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.properties.PropertyDescriptor;
import org.xwiki.properties.test.TestBean;

/**
 * Validate {@link DefaultBeanDescriptor}.
 *
 * @version $Id$
 */
public class DefaultBeanDescriptorTest
{
    private DefaultBeanDescriptor beanDescriptor;

    @Before
    public void setUp() throws Exception
    {
        this.beanDescriptor = new DefaultBeanDescriptor(TestBean.class);
    }

    @Test
    public void testPropertyDescriptor()
    {
        Assert.assertNull(this.beanDescriptor.getProperty("hiddenProperty"));

        PropertyDescriptor lowerPropertyDescriptor = this.beanDescriptor.getProperty("lowerprop");

        Assert.assertNotNull(lowerPropertyDescriptor);
        Assert.assertEquals("lowerprop", lowerPropertyDescriptor.getId());
        Assert.assertEquals("lowerprop", lowerPropertyDescriptor.getName());
        Assert.assertEquals("lowerprop", lowerPropertyDescriptor.getDescription());
        Assert.assertSame(String.class, lowerPropertyDescriptor.getPropertyClass());
        Assert.assertNull(lowerPropertyDescriptor.getDefaultValue());
        Assert.assertFalse(lowerPropertyDescriptor.isMandatory());
        Assert.assertNotNull(lowerPropertyDescriptor.getWriteMethod());
        Assert.assertNotNull(lowerPropertyDescriptor.getReadMethod());
        Assert.assertNull(lowerPropertyDescriptor.getField());

        PropertyDescriptor prop1Descriptor = this.beanDescriptor.getProperty("prop1");

        Assert.assertEquals("defaultprop1", prop1Descriptor.getDefaultValue());

        PropertyDescriptor deprecatedDescriptor = this.beanDescriptor.getProperty("deprecatedParameter");
        Assert.assertTrue(deprecatedDescriptor.isDeprecated());

        PropertyDescriptor advancedDescriptor = this.beanDescriptor.getProperty("advancedParameter");
        Assert.assertTrue(advancedDescriptor.isAdvanced());

        Assert.assertEquals("test1", advancedDescriptor.getGroups().get(0));
        Assert.assertEquals("test2", advancedDescriptor.getGroups().get(1));
    }

    @Test
    public void testPropertyDescriptorWithUpperCase()
    {
        PropertyDescriptor upperPropertyDescriptor = this.beanDescriptor.getProperty("upperProp");

        Assert.assertNotNull(upperPropertyDescriptor);
        Assert.assertEquals("upperProp", upperPropertyDescriptor.getId());
        Assert.assertEquals("upperProp", upperPropertyDescriptor.getName());
        Assert.assertEquals("upperProp", upperPropertyDescriptor.getDescription());
        Assert.assertSame(String.class, upperPropertyDescriptor.getPropertyClass());
        Assert.assertFalse(upperPropertyDescriptor.isMandatory());
        Assert.assertNotNull(upperPropertyDescriptor.getWriteMethod());
        Assert.assertNotNull(upperPropertyDescriptor.getReadMethod());
        Assert.assertNull(upperPropertyDescriptor.getField());
    }

    @Test
    public void testPropertyDescriptorPublicField()
    {
        PropertyDescriptor publicFieldPropertyDescriptor = this.beanDescriptor.getProperty("publicField");

        Assert.assertNotNull(publicFieldPropertyDescriptor);
        Assert.assertEquals("publicField", publicFieldPropertyDescriptor.getId());
        Assert.assertEquals("Public Field", publicFieldPropertyDescriptor.getName());
        Assert.assertEquals("a public field", publicFieldPropertyDescriptor.getDescription());
        Assert.assertSame(String.class, publicFieldPropertyDescriptor.getPropertyClass());
        Assert.assertFalse(publicFieldPropertyDescriptor.isMandatory());
        Assert.assertNull(publicFieldPropertyDescriptor.getWriteMethod());
        Assert.assertNull(publicFieldPropertyDescriptor.getReadMethod());
        Assert.assertNotNull(publicFieldPropertyDescriptor.getField());
    }

    @Test
    public void testPropertyDescriptorPublicStaticField()
    {
        Assert.assertNull(this.beanDescriptor.getProperty("STATICFIELD"));
    }

    @Test
    public void testPropertyDescriptorWithDescription()
    {
        PropertyDescriptor prop1Descriptor = this.beanDescriptor.getProperty("prop1");

        Assert.assertNotNull(prop1Descriptor);
        Assert.assertEquals("prop1", prop1Descriptor.getId());
        Assert.assertEquals("prop1 description", prop1Descriptor.getDescription());
        Assert.assertSame(String.class, prop1Descriptor.getPropertyClass());
        Assert.assertFalse(prop1Descriptor.isMandatory());
        Assert.assertNotNull(prop1Descriptor.getWriteMethod());
        Assert.assertNotNull(prop1Descriptor.getReadMethod());
        Assert.assertNull(prop1Descriptor.getField());
    }

    @Test
    public void testPropertyDescriptorWithDescriptionAndMandatory()
    {
        PropertyDescriptor prop2Descriptor = this.beanDescriptor.getProperty("prop2");

        Assert.assertNotNull(prop2Descriptor);
        Assert.assertEquals("prop2", prop2Descriptor.getId());
        Assert.assertEquals("prop2 description", prop2Descriptor.getDescription());
        Assert.assertSame(int.class, prop2Descriptor.getPropertyClass());
        Assert.assertTrue(prop2Descriptor.isMandatory());
        Assert.assertNotNull(prop2Descriptor.getWriteMethod());
        Assert.assertNotNull(prop2Descriptor.getReadMethod());
        Assert.assertNull(prop2Descriptor.getField());
    }

    @Test
    public void testPropertyDescriptorWithDescriptionAndMandatoryOnSetter()
    {
        PropertyDescriptor prop3Descriptor = this.beanDescriptor.getProperty("prop3");

        Assert.assertNotNull(prop3Descriptor);
        Assert.assertEquals("prop3", prop3Descriptor.getId());
        Assert.assertEquals("prop3 description", prop3Descriptor.getDescription());
        Assert.assertSame(boolean.class, prop3Descriptor.getPropertyClass());
        Assert.assertTrue(prop3Descriptor.isMandatory());
        Assert.assertNotNull(prop3Descriptor.getWriteMethod());
        Assert.assertNotNull(prop3Descriptor.getReadMethod());
        Assert.assertNull(prop3Descriptor.getField());
    }

    @Test
    public void testPropertyDescriptorGeneric()
    {
        PropertyDescriptor genericPropertyDescriptor = this.beanDescriptor.getProperty("genericProp");

        Assert.assertNotNull(genericPropertyDescriptor);
        Assert.assertEquals("genericProp", genericPropertyDescriptor.getId());
        Assert.assertEquals("genericProp", genericPropertyDescriptor.getName());
        Assert.assertEquals("genericProp", genericPropertyDescriptor.getDescription());
        Assert.assertSame(List.class, ((ParameterizedType) genericPropertyDescriptor.getPropertyType()).getRawType());
        Assert.assertSame(Integer.class,
            ((ParameterizedType) genericPropertyDescriptor.getPropertyType()).getActualTypeArguments()[0]);
        Assert.assertNull(genericPropertyDescriptor.getDefaultValue());
        Assert.assertFalse(genericPropertyDescriptor.isMandatory());
        Assert.assertNotNull(genericPropertyDescriptor.getWriteMethod());
        Assert.assertNotNull(genericPropertyDescriptor.getReadMethod());
        Assert.assertNull(genericPropertyDescriptor.getField());

        PropertyDescriptor prop1Descriptor = this.beanDescriptor.getProperty("prop1");

        Assert.assertEquals("defaultprop1", prop1Descriptor.getDefaultValue());
    }

    @Test
    public void testPropertyDescriptorGenericField()
    {
        PropertyDescriptor genericFieldPropertyDescriptor = this.beanDescriptor.getProperty("genericField");

        Assert.assertNotNull(genericFieldPropertyDescriptor);
        Assert.assertEquals("genericField", genericFieldPropertyDescriptor.getId());
        Assert.assertEquals("genericField", genericFieldPropertyDescriptor.getName());
        Assert.assertEquals("genericField", genericFieldPropertyDescriptor.getDescription());
        Assert.assertSame(List.class,
            ((ParameterizedType) genericFieldPropertyDescriptor.getPropertyType()).getRawType());
        Assert.assertSame(Integer.class,
            ((ParameterizedType) genericFieldPropertyDescriptor.getPropertyType()).getActualTypeArguments()[0]);
        Assert.assertFalse(genericFieldPropertyDescriptor.isMandatory());
        Assert.assertNull(genericFieldPropertyDescriptor.getWriteMethod());
        Assert.assertNull(genericFieldPropertyDescriptor.getReadMethod());
        Assert.assertNotNull(genericFieldPropertyDescriptor.getField());
    }

    @Test
    public void testPropertyDescriptorFieldWithDifferentId()
    {
        PropertyDescriptor propertyDescriptor = this.beanDescriptor.getProperty("impossible.field.name");

        Assert.assertNotNull(propertyDescriptor);
        Assert.assertEquals("impossible.field.name", propertyDescriptor.getId());
        Assert.assertEquals("impossible.field.name", propertyDescriptor.getName());
        Assert.assertEquals("impossible.field.name", propertyDescriptor.getDescription());
        Assert.assertSame(String.class, propertyDescriptor.getPropertyType());
        Assert.assertFalse(propertyDescriptor.isMandatory());
        Assert.assertNull(propertyDescriptor.getWriteMethod());
        Assert.assertNull(propertyDescriptor.getReadMethod());
        Assert.assertNotNull(propertyDescriptor.getField());
    }

    @Test
    public void testPropertyDescriptorMethodWithDifferentId()
    {
        PropertyDescriptor propertyDescriptor = this.beanDescriptor.getProperty("impossible.method.name");

        Assert.assertNotNull(propertyDescriptor);
        Assert.assertEquals("impossible.method.name", propertyDescriptor.getId());
        Assert.assertEquals("impossible.method.name", propertyDescriptor.getName());
        Assert.assertEquals("propertyWithDifferentId", propertyDescriptor.getDescription());
        Assert.assertSame(String.class, propertyDescriptor.getPropertyType());
        Assert.assertFalse(propertyDescriptor.isMandatory());
        Assert.assertNotNull(propertyDescriptor.getWriteMethod());
        Assert.assertNotNull(propertyDescriptor.getReadMethod());
        Assert.assertNull(propertyDescriptor.getField());
    }
}
