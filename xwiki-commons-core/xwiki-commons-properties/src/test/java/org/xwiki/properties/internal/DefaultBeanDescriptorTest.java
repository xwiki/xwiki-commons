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

import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.properties.PropertyDescriptor;
import org.xwiki.properties.test.TestBean;
import org.xwiki.properties.test.TestBeanError;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link DefaultBeanDescriptor}.
 *
 * @version $Id$
 */
public class DefaultBeanDescriptorTest
{
    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.ERROR);

    private DefaultBeanDescriptor beanDescriptor;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.beanDescriptor = new DefaultBeanDescriptor(TestBean.class);
    }

    @Test
    public void propertyDescriptorTest()
    {
        assertNull(this.beanDescriptor.getProperty("hiddenProperty"));

        PropertyDescriptor lowerPropertyDescriptor = this.beanDescriptor.getProperty("lowerprop");

        assertNotNull(lowerPropertyDescriptor);
        assertEquals("lowerprop", lowerPropertyDescriptor.getId());
        assertEquals("lowerprop", lowerPropertyDescriptor.getName());
        assertEquals("lowerprop", lowerPropertyDescriptor.getDescription());
        assertSame(String.class, lowerPropertyDescriptor.getPropertyClass());
        assertNull(lowerPropertyDescriptor.getDefaultValue());
        assertFalse(lowerPropertyDescriptor.isMandatory());
        assertNotNull(lowerPropertyDescriptor.getWriteMethod());
        assertNotNull(lowerPropertyDescriptor.getReadMethod());
        assertNull(lowerPropertyDescriptor.getField());

        PropertyDescriptor prop1Descriptor = this.beanDescriptor.getProperty("prop1");

        assertEquals("defaultprop1", prop1Descriptor.getDefaultValue());
        assertEquals("feature1", prop1Descriptor.getGroupDescriptor().getFeature());

        PropertyDescriptor deprecatedDescriptor = this.beanDescriptor.getProperty("deprecatedParameter");
        assertTrue(deprecatedDescriptor.isDeprecated());
        assertEquals("test1", deprecatedDescriptor.getGroupDescriptor().getGroup().get(0));
        assertEquals("test2", deprecatedDescriptor.getGroupDescriptor().getGroup().get(1));
        assertEquals("feature2", deprecatedDescriptor.getGroupDescriptor().getFeature());

        PropertyDescriptor advancedDescriptor = this.beanDescriptor.getProperty("advancedParameter");
        assertTrue(advancedDescriptor.isAdvanced());
        assertEquals("test1", advancedDescriptor.getGroupDescriptor().getGroup().get(0));
        assertEquals("test2", advancedDescriptor.getGroupDescriptor().getGroup().get(1));
        assertEquals("feature2", advancedDescriptor.getGroupDescriptor().getFeature());

        PropertyDescriptor displayTypeDescriptor = this.beanDescriptor.getProperty("displayTypeParameter");
        assertEquals(new DefaultParameterizedType(null, Triple.class, Boolean.class, String.class, Long.class),
                displayTypeDescriptor.getDisplayType());

        PropertyDescriptor displayTypeDescriptor2 = this.beanDescriptor.getProperty("displayTypeParameter2");
        assertEquals(Boolean.class, displayTypeDescriptor2.getDisplayType());
    }

    @Test
    public void propertyDescriptorWithUpperCaseTest()
    {
        PropertyDescriptor upperPropertyDescriptor = this.beanDescriptor.getProperty("upperProp");

        assertNotNull(upperPropertyDescriptor);
        assertEquals("upperProp", upperPropertyDescriptor.getId());
        assertEquals("upperProp", upperPropertyDescriptor.getName());
        assertEquals("upperProp", upperPropertyDescriptor.getDescription());
        assertSame(String.class, upperPropertyDescriptor.getPropertyClass());
        assertFalse(upperPropertyDescriptor.isMandatory());
        assertNotNull(upperPropertyDescriptor.getWriteMethod());
        assertNotNull(upperPropertyDescriptor.getReadMethod());
        assertNull(upperPropertyDescriptor.getField());
    }

    @Test
    public void propertyDescriptorPublicFieldTest()
    {
        PropertyDescriptor publicFieldPropertyDescriptor = this.beanDescriptor.getProperty("publicField");

        assertNotNull(publicFieldPropertyDescriptor);
        assertEquals("publicField", publicFieldPropertyDescriptor.getId());
        assertEquals("Public Field", publicFieldPropertyDescriptor.getName());
        assertEquals("a public field", publicFieldPropertyDescriptor.getDescription());
        assertSame(String.class, publicFieldPropertyDescriptor.getPropertyClass());
        assertFalse(publicFieldPropertyDescriptor.isMandatory());
        assertNull(publicFieldPropertyDescriptor.getWriteMethod());
        assertNull(publicFieldPropertyDescriptor.getReadMethod());
        assertNotNull(publicFieldPropertyDescriptor.getField());
    }

    @Test
    public void propertyDescriptorPublicStaticFieldTest()
    {
        assertNull(this.beanDescriptor.getProperty("STATICFIELD"));
    }

    @Test
    public void propertyDescriptorWithDescriptionTest()
    {
        PropertyDescriptor prop1Descriptor = this.beanDescriptor.getProperty("prop1");

        assertNotNull(prop1Descriptor);
        assertEquals("prop1", prop1Descriptor.getId());
        assertEquals("prop1 description", prop1Descriptor.getDescription());
        assertSame(String.class, prop1Descriptor.getPropertyClass());
        assertFalse(prop1Descriptor.isMandatory());
        assertNotNull(prop1Descriptor.getWriteMethod());
        assertNotNull(prop1Descriptor.getReadMethod());
        assertNull(prop1Descriptor.getField());
    }

    @Test
    public void propertyDescriptorWithDescriptionAndMandatoryTest()
    {
        PropertyDescriptor prop2Descriptor = this.beanDescriptor.getProperty("prop2");

        assertNotNull(prop2Descriptor);
        assertEquals("prop2", prop2Descriptor.getId());
        assertEquals("prop2 description", prop2Descriptor.getDescription());
        assertSame(int.class, prop2Descriptor.getPropertyClass());
        assertTrue(prop2Descriptor.isMandatory());
        assertNotNull(prop2Descriptor.getWriteMethod());
        assertNotNull(prop2Descriptor.getReadMethod());
        assertNull(prop2Descriptor.getField());
    }

    @Test
    public void propertyDescriptorWithDescriptionAndMandatoryOnSetterTest()
    {
        PropertyDescriptor prop3Descriptor = this.beanDescriptor.getProperty("prop3");

        assertNotNull(prop3Descriptor);
        assertEquals("prop3", prop3Descriptor.getId());
        assertEquals("prop3 description", prop3Descriptor.getDescription());
        assertSame(boolean.class, prop3Descriptor.getPropertyClass());
        assertTrue(prop3Descriptor.isMandatory());
        assertNotNull(prop3Descriptor.getWriteMethod());
        assertNotNull(prop3Descriptor.getReadMethod());
        assertNull(prop3Descriptor.getField());
    }

    @Test
    public void propertyDescriptorGenericTest()
    {
        PropertyDescriptor genericPropertyDescriptor = this.beanDescriptor.getProperty("genericProp");

        assertNotNull(genericPropertyDescriptor);
        assertEquals("genericProp", genericPropertyDescriptor.getId());
        assertEquals("genericProp", genericPropertyDescriptor.getName());
        assertEquals("genericProp", genericPropertyDescriptor.getDescription());
        assertSame(List.class, ((ParameterizedType) genericPropertyDescriptor.getPropertyType()).getRawType());
        assertSame(Integer.class,
            ((ParameterizedType) genericPropertyDescriptor.getPropertyType()).getActualTypeArguments()[0]);
        assertNull(genericPropertyDescriptor.getDefaultValue());
        assertFalse(genericPropertyDescriptor.isMandatory());
        assertNotNull(genericPropertyDescriptor.getWriteMethod());
        assertNotNull(genericPropertyDescriptor.getReadMethod());
        assertNull(genericPropertyDescriptor.getField());

        PropertyDescriptor prop1Descriptor = this.beanDescriptor.getProperty("prop1");

        assertEquals("defaultprop1", prop1Descriptor.getDefaultValue());
    }

    @Test
    public void propertyDescriptorGenericFieldTest()
    {
        PropertyDescriptor genericFieldPropertyDescriptor = this.beanDescriptor.getProperty("genericField");

        assertNotNull(genericFieldPropertyDescriptor);
        assertEquals("genericField", genericFieldPropertyDescriptor.getId());
        assertEquals("genericField", genericFieldPropertyDescriptor.getName());
        assertEquals("genericField", genericFieldPropertyDescriptor.getDescription());
        assertSame(List.class,
            ((ParameterizedType) genericFieldPropertyDescriptor.getPropertyType()).getRawType());
        assertSame(Integer.class,
            ((ParameterizedType) genericFieldPropertyDescriptor.getPropertyType()).getActualTypeArguments()[0]);
        assertFalse(genericFieldPropertyDescriptor.isMandatory());
        assertNull(genericFieldPropertyDescriptor.getWriteMethod());
        assertNull(genericFieldPropertyDescriptor.getReadMethod());
        assertNotNull(genericFieldPropertyDescriptor.getField());
    }

    @Test
    public void propertyDescriptorFieldWithDifferentIdTest()
    {
        PropertyDescriptor propertyDescriptor = this.beanDescriptor.getProperty("impossible.field.name");

        assertNotNull(propertyDescriptor);
        assertEquals("impossible.field.name", propertyDescriptor.getId());
        assertEquals("impossible.field.name", propertyDescriptor.getName());
        assertEquals("impossible.field.name", propertyDescriptor.getDescription());
        assertSame(String.class, propertyDescriptor.getPropertyType());
        assertFalse(propertyDescriptor.isMandatory());
        assertNull(propertyDescriptor.getWriteMethod());
        assertNull(propertyDescriptor.getReadMethod());
        assertNotNull(propertyDescriptor.getField());
    }

    @Test
    public void propertyDescriptorMethodWithDifferentIdTest()
    {
        PropertyDescriptor propertyDescriptor = this.beanDescriptor.getProperty("impossible.method.name");

        assertNotNull(propertyDescriptor);
        assertEquals("impossible.method.name", propertyDescriptor.getId());
        assertEquals("impossible.method.name", propertyDescriptor.getName());
        assertEquals("propertyWithDifferentId", propertyDescriptor.getDescription());
        assertSame(String.class, propertyDescriptor.getPropertyType());
        assertFalse(propertyDescriptor.isMandatory());
        assertNotNull(propertyDescriptor.getWriteMethod());
        assertNotNull(propertyDescriptor.getReadMethod());
        assertNull(propertyDescriptor.getField());
    }

    @Test
    public void propertyDescriptorErrorTest()
    {
        new DefaultBeanDescriptor(TestBeanError.class);
        assertEquals("Failed to load bean descriptor for class class org.xwiki.properties.test.TestBeanError",
                logCapture.getMessage(0));
    }

    @Test
    public void propertyDescriptorbackwardCompatible()
    {
        PropertyDescriptor propertyDescriptor =
                new BackwardCompatiblePropertyDescriptor(this.beanDescriptor.getProperty("lowerprop"));

        assertFalse(propertyDescriptor.isDeprecated());
        assertFalse(propertyDescriptor.isAdvanced());
        assertNull(propertyDescriptor.getGroupDescriptor().getGroup());
        assertNull(propertyDescriptor.getGroupDescriptor().getFeature());
    }
}
