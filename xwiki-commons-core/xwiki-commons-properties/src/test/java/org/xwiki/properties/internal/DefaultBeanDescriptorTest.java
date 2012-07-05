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
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyHidden;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.properties.annotation.PropertyName;

/**
 * Validate {@link DefaultBeanDescriptor}.
 *
 * @version $Id$
 */
public class DefaultBeanDescriptorTest
{
    public static class BeanTest
    {
        private String lowerprop;

        private String upperProp;

        private String prop1 = "defaultprop1";

        private int prop2;

        private boolean prop3;

        private String hiddenProperty;

        private List<Integer> genericProp;

        @PropertyName("Public Field")
        @PropertyDescription("a public field")
        public String publicField;

        public List<Integer> genericField;

        public void setLowerprop(String lowerprop)
        {
            this.lowerprop = lowerprop;
        }

        public String getLowerprop()
        {
            return this.lowerprop;
        }

        public void setUpperProp(String upperProp)
        {
            this.upperProp = upperProp;
        }

        public String getUpperProp()
        {
            return this.upperProp;
        }

        @PropertyDescription("prop1 description")
        public void setProp1(String prop1)
        {
            this.prop1 = prop1;
        }

        public String getProp1()
        {
            return this.prop1;
        }

        @PropertyMandatory
        @PropertyDescription("prop2 description")
        public void setProp2(int prop1)
        {
            this.prop2 = prop1;
        }

        public int getProp2()
        {
            return this.prop2;
        }

        public void setProp3(boolean prop1)
        {
            this.prop3 = prop1;
        }

        @PropertyMandatory
        @PropertyDescription("prop3 description")
        public boolean getProp3()
        {
            return this.prop3;
        }

        @PropertyHidden
        public void setHiddenProperty(String hiddenProperty)
        {
            this.hiddenProperty = hiddenProperty;
        }

        public String getHiddenProperty()
        {
            return hiddenProperty;
        }

        public List<Integer> getGenericProp()
        {
            return genericProp;
        }

        public void setGenericProp(List<Integer> genericProp)
        {
            this.genericProp = genericProp;
        }
    }

    private DefaultBeanDescriptor beanDescriptor;

    @Before
    public void setUp() throws Exception
    {
        this.beanDescriptor = new DefaultBeanDescriptor(BeanTest.class);
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
        Assert.assertEquals(null, lowerPropertyDescriptor.getDefaultValue());
        Assert.assertEquals(false, lowerPropertyDescriptor.isMandatory());
        Assert.assertNotNull(lowerPropertyDescriptor.getWriteMethod());
        Assert.assertNotNull(lowerPropertyDescriptor.getReadMethod());
        Assert.assertNull(lowerPropertyDescriptor.getField());

        PropertyDescriptor prop1Descriptor = this.beanDescriptor.getProperty("prop1");

        Assert.assertEquals("defaultprop1", prop1Descriptor.getDefaultValue());
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
        Assert.assertEquals(false, upperPropertyDescriptor.isMandatory());
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
        Assert.assertEquals(false, publicFieldPropertyDescriptor.isMandatory());
        Assert.assertNull(publicFieldPropertyDescriptor.getWriteMethod());
        Assert.assertNull(publicFieldPropertyDescriptor.getReadMethod());
        Assert.assertNotNull(publicFieldPropertyDescriptor.getField());
    }

    @Test
    public void testPropertyDescriptorWithDescription()
    {
        PropertyDescriptor prop1Descriptor = this.beanDescriptor.getProperty("prop1");

        Assert.assertNotNull(prop1Descriptor);
        Assert.assertEquals("prop1", prop1Descriptor.getId());
        Assert.assertEquals("prop1 description", prop1Descriptor.getDescription());
        Assert.assertSame(String.class, prop1Descriptor.getPropertyClass());
        Assert.assertEquals(false, prop1Descriptor.isMandatory());
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
        Assert.assertEquals(true, prop2Descriptor.isMandatory());
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
        Assert.assertEquals(true, prop3Descriptor.isMandatory());
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
        Assert.assertEquals(null, genericPropertyDescriptor.getDefaultValue());
        Assert.assertEquals(false, genericPropertyDescriptor.isMandatory());
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
        Assert.assertEquals(false, genericFieldPropertyDescriptor.isMandatory());
        Assert.assertNull(genericFieldPropertyDescriptor.getWriteMethod());
        Assert.assertNull(genericFieldPropertyDescriptor.getReadMethod());
        Assert.assertNotNull(genericFieldPropertyDescriptor.getField());
    }
}
