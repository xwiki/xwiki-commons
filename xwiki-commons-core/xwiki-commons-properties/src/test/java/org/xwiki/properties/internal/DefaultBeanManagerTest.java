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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.PropertyException;
import org.xwiki.properties.PropertyMandatoryException;
import org.xwiki.properties.RawProperties;
import org.xwiki.properties.internal.converter.ConvertUtilsConverter;
import org.xwiki.properties.internal.converter.EnumConverter;
import org.xwiki.properties.test.GenericTestConverter;
import org.xwiki.properties.test.TestBean;
import org.xwiki.properties.test.TestBeanFeatures;
import org.xwiki.properties.test.TestBeanValidation;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link DefaultBeanManager}.
 *
 * @version $Id$
 */
@ComponentTest
// @formatter:off
@ComponentList({
    DefaultBeanManager.class,
    DefaultConverterManager.class,
    EnumConverter.class,
    ConvertUtilsConverter.class,
    ContextComponentManagerProvider.class,
    GenericTestConverter.class
})
// @formatter:on
class DefaultBeanManagerTest
{
    public static class RawPropertiesTest extends HashMap<String, Object> implements RawProperties
    {
        @Override
        public void set(String propertyName, Object value)
        {
            put(propertyName, value);
        }
    }

    @InjectComponentManager
    private ComponentManager componentManager;

    private BeanManager defaultBeanManager;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.defaultBeanManager = this.componentManager.getInstance(BeanManager.class);
    }

    @Test
    void populate() throws PropertyException
    {
        TestBean beanTest = new TestBean();

        Map<String, String> values = new HashMap<>();

        values.put("lowerprop", "lowerpropvalue");
        values.put("upperprop", "upperPropvalue");
        values.put("prop2", "42");
        values.put("prop3", "true");
        values.put("hiddenProperty", "hiddenPropertyvalue");
        values.put("publicField", "publicFieldvalue");
        values.put("genericProp", "1,2");

        this.defaultBeanManager.populate(beanTest, values);

        assertEquals("lowerpropvalue", beanTest.getLowerprop());
        assertEquals("upperPropvalue", beanTest.getUpperProp());
        assertEquals(42, beanTest.getProp2());
        assertTrue(beanTest.getProp3());
        assertNull(beanTest.getHiddenProperty());
        assertEquals("publicFieldvalue", beanTest.publicField);
        assertEquals(Arrays.asList(1, 2), beanTest.getGenericProp());
    }

    @Test
    void populateWithValidation() throws PropertyException
    {
        TestBeanValidation beanTest = new TestBeanValidation();

        Map<String, String> values = new HashMap<>();

        values.put("working", "true");

        this.defaultBeanManager.populate(beanTest, values);

        assertTrue(beanTest.isWorking());

        values.put("working", "false");

        assertThrows(PropertyException.class, () -> this.defaultBeanManager.populate(beanTest, values),
            "No Bean Validation was executed");
    }

    @Test
    void populateWhenMissingMandatoryProperty()
    {
        Throwable exception = assertThrows(PropertyMandatoryException.class,
            () -> this.defaultBeanManager.populate(new TestBean(), new HashMap<String, String>()));
        assertEquals("Property [prop2] mandatory", exception.getMessage());
    }

    @Test
    void populateRawProperties() throws PropertyException
    {
        Map<String, Object> values = new HashMap<>();

        values.put("pro1", "value1");
        values.put("prop2", 2);

        RawPropertiesTest bean = new RawPropertiesTest();

        this.defaultBeanManager.populate(bean, values);

        assertEquals(values, bean);
    }
    
    @Test
    void populateMandatoryFeatures() throws PropertyException
    {
        // The following tries should not throw any exception
        // We test when a property defined before the annotated one is filled up
        Map<String, Object> values1 = new HashMap<>();
        values1.put("propertyFeatureMandatory1", "value");
        this.defaultBeanManager.populate(new TestBeanFeatures(), values1);

        // We test when the annotated property is the one filled up
        Map<String, Object> values2 = new HashMap<>();
        values2.put("propertyFeatureMandatory2", "value");
        this.defaultBeanManager.populate(new TestBeanFeatures(), values2);

        // We test when a property defined after the annotated one is filled up
        Map<String, Object> values3 = new HashMap<>();
        values3.put("propertyFeatureMandatory3", "value");
        this.defaultBeanManager.populate(new TestBeanFeatures(), values3);

        // We test when all properties are filled up for the feature.
        Map<String, Object> values4 = new HashMap<>();
        values4.put("propertyFeatureMandatory1", "value");
        values4.put("propertyFeatureMandatory2", "value");
        values4.put("propertyFeatureMandatory3", "value");
        this.defaultBeanManager.populate(new TestBeanFeatures(), values4);

        // We test when no property is filled up for the feature.
        Throwable exception = assertThrows(PropertyException.class,
            () -> this.defaultBeanManager.populate(new TestBeanFeatures(), new HashMap<>()));
        assertEquals("Property [propertyFeatureMandatory2:mandatoryFeature] mandatory", 
            exception.getMessage());
    }
}
