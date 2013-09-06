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
package org.xwiki.filter.internal;

import java.awt.Color;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterDescriptorManager;
import org.xwiki.filter.FilterElementDescriptor;
import org.xwiki.filter.FilterElementParameterDescriptor;
import org.xwiki.filter.test.TestFilter;
import org.xwiki.filter.test.TestFilterImplementation;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

public class FilterDescriptorManagerTest
{
    @Rule
    public MockitoComponentMockingRule<FilterDescriptorManager> mocker =
        new MockitoComponentMockingRule<FilterDescriptorManager>(DefaultFilterDescriptorManager.class);

    private FilterDescriptor filterDescriptor;

    @Before
    public void before() throws ComponentLookupException
    {
        // Make sure we also support filters through proxies
        Object filter =
            Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {TestFilter.class}, new InvocationHandler()
            {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    // Do nothing
                    return null;
                }
            });

        this.filterDescriptor = this.mocker.getComponentUnderTest().getFilterDescriptor(filter.getClass());
    }

    @Test
    public void testContainerWithParameters()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("containerwithparameters");

        Assert.assertNotNull(filterElement);

        FilterElementParameterDescriptor parameter0 = filterElement.getParameters()[0];

        Assert.assertNull(parameter0.getName());
        Assert.assertEquals(0, parameter0.getIndex());
        Assert.assertEquals(String.class, parameter0.getType());
    }

    @Test
    public void testContainerWithNamedParameters()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("containerwithnamedparameters");

        Assert.assertNotNull(filterElement);

        FilterElementParameterDescriptor parameter0 = filterElement.getParameters()[0];
        parameter0 = filterElement.getParameter("namedParam");

        Assert.assertNotNull(parameter0);

        Assert.assertEquals("namedParam", parameter0.getName());
        Assert.assertEquals(0, parameter0.getIndex());
        Assert.assertEquals(String.class, parameter0.getType());
    }

    @Test
    public void testNameInheritance() throws ComponentLookupException
    {
        FilterElementDescriptor filterElement =
            this.mocker.getComponentUnderTest().getFilterDescriptor(TestFilterImplementation.class)
                .getElement("containerwithnamedparameters");

        Assert.assertNotNull(filterElement);

        FilterElementParameterDescriptor parameter0 = filterElement.getParameters()[0];
        parameter0 = filterElement.getParameter("namedParam");

        Assert.assertNotNull(parameter0);

        Assert.assertEquals("namedParam", parameter0.getName());
        Assert.assertEquals(0, parameter0.getIndex());
        Assert.assertEquals(String.class, parameter0.getType());
    }

    @Test
    public void testWithDefaultValue() throws ComponentLookupException
    {
        ConverterManager converter = mocker.getInstance(ConverterManager.class);

        Mockito.when(converter.convert(int.class, "42")).thenReturn(42);
        Mockito.when(converter.convert(String.class, "default value")).thenReturn("default value");
        Mockito.when(converter.convert(Color.class, "#ffffff")).thenReturn(Color.WHITE);
        Mockito.when(
            converter.convert(new DefaultParameterizedType(null, Map.class, new Type[] {String.class, String.class}),
                "")).thenThrow(ConversionException.class);

        FilterElementDescriptor filterElement =
            this.mocker.getComponentUnderTest().getFilterDescriptor(TestFilterImplementation.class)
                .getElement("childwithdefaultvalue");

        Assert.assertNotNull(filterElement);

        FilterElementParameterDescriptor<Integer> parameter0 = filterElement.getParameter("int");

        Assert.assertEquals(Integer.valueOf(42), parameter0.getDefaultValue());

        FilterElementParameterDescriptor<String> parameter1 = filterElement.getParameter("string");

        Assert.assertEquals("default value", parameter1.getDefaultValue());

        FilterElementParameterDescriptor<Color> parameter2 = filterElement.getParameter("color");

        Assert.assertEquals(Color.WHITE, parameter2.getDefaultValue());

        FilterElementParameterDescriptor<Map<String, String>> parameter3 = filterElement.getParameter("map");

        Assert.assertEquals(Collections.EMPTY_MAP, parameter3.getDefaultValue());
    }
}
