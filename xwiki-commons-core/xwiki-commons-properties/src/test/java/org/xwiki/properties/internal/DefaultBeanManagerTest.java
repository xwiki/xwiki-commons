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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.PropertyException;
import org.xwiki.properties.PropertyMandatoryException;
import org.xwiki.properties.internal.DefaultBeanDescriptorTest.BeanTest;
import org.xwiki.properties.internal.converter.ConvertUtilsConverter;
import org.xwiki.properties.internal.converter.EnumConverter;
import org.xwiki.test.ComponentManagerRule;
import org.xwiki.test.annotation.ComponentList;

/**
 * Validate {@link DefaultBeanManager}.
 * 
 * @version $Id$
 */
@ComponentList({
    DefaultBeanManager.class,
    DefaultConverterManager.class,
    EnumConverter.class,
    ConvertUtilsConverter.class
})
public class DefaultBeanManagerTest
{
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
        BeanTest beanTest = new BeanTest();

        Map<String, String> values = new HashMap<String, String>();

        values.put("lowerprop", "lowerpropvalue");
        values.put("upperprop", "upperPropvalue");
        values.put("prop2", "42");
        values.put("prop3", "true");
        values.put("hiddenProperty", "hiddenPropertyvalue");
        values.put("publicField", "publicFieldvalue");

        this.defaultBeanManager.populate(beanTest, values);

        Assert.assertEquals("lowerpropvalue", beanTest.getLowerprop());
        Assert.assertEquals("upperPropvalue", beanTest.getUpperProp());
        Assert.assertEquals(42, beanTest.getProp2());
        Assert.assertEquals(true, beanTest.getProp3());
        Assert.assertEquals(null, beanTest.getHiddenProperty());
        Assert.assertEquals("publicFieldvalue", beanTest.publicField);
    }

    @Test(expected = PropertyMandatoryException.class)
    public void testPopulateWhenMissingMandatoryProperty() throws PropertyException
    {
        this.defaultBeanManager.populate(new BeanTest(), new HashMap<String, String>());
    }
}
