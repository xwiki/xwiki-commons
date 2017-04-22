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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.PropertyException;
import org.xwiki.properties.PropertyMandatoryException;
import org.xwiki.properties.RawProperties;
import org.xwiki.properties.internal.converter.ConvertUtilsConverter;
import org.xwiki.properties.internal.converter.EnumConverter;
import org.xwiki.properties.test.GenericTestConverter;
import org.xwiki.properties.test.TestBean;
import org.xwiki.test.ComponentManagerRule;
import org.xwiki.test.annotation.ComponentList;

/**
 * Validate {@link DefaultBeanManager}.
 *
 * @version $Id$
 */
@ComponentList({ DefaultBeanManager.class, DefaultConverterManager.class, EnumConverter.class,
    ConvertUtilsConverter.class, ContextComponentManagerProvider.class, GenericTestConverter.class })
public class DefaultBeanManagerTest
{
    public static class RawPropertiesTest extends HashMap<String, Object> implements RawProperties
    {
        @Override
        public void set(String propertyName, Object value)
        {
            put(propertyName, value);
        }
    }

    @Rule
    public final ComponentManagerRule componentManager = new ComponentManagerRule();

    private BeanManager defaultBeanManager;

    @Before
    public void setUp() throws Exception
    {
        this.defaultBeanManager = this.componentManager.getInstance(BeanManager.class);
    }

    @Test
    public void testPopulate() throws PropertyException
    {
        TestBean beanTest = new TestBean();

        Map<String, String> values = new HashMap<String, String>();

        values.put("lowerprop", "lowerpropvalue");
        values.put("upperprop", "upperPropvalue");
        values.put("prop2", "42");
        values.put("prop3", "true");
        values.put("hiddenProperty", "hiddenPropertyvalue");
        values.put("publicField", "publicFieldvalue");
        values.put("genericProp", "1,2");

        this.defaultBeanManager.populate(beanTest, values);

        Assert.assertEquals("lowerpropvalue", beanTest.getLowerprop());
        Assert.assertEquals("upperPropvalue", beanTest.getUpperProp());
        Assert.assertEquals(42, beanTest.getProp2());
        Assert.assertTrue(beanTest.getProp3());
        Assert.assertNull(beanTest.getHiddenProperty());
        Assert.assertEquals("publicFieldvalue", beanTest.publicField);
        Assert.assertEquals(Arrays.asList(1, 2), beanTest.getGenericProp());
    }

    @Test(expected = PropertyMandatoryException.class)
    public void testPopulateWhenMissingMandatoryProperty() throws PropertyException
    {
        this.defaultBeanManager.populate(new TestBean(), new HashMap<String, String>());
    }

    @Test
    public void testPopulateRawProperties() throws PropertyException
    {
        Map<String, Object> values = new HashMap<String, Object>();

        values.put("pro1", "value1");
        values.put("prop2", 2);

        RawPropertiesTest bean = new RawPropertiesTest();

        this.defaultBeanManager.populate(bean, values);

        Assert.assertEquals(values, bean);
    }
}
