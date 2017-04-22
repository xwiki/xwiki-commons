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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.filter.FilterDescriptor;
import org.xwiki.filter.FilterDescriptorManager;
import org.xwiki.filter.FilterElementDescriptor;
import org.xwiki.filter.FilterElementParameterDescriptor;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.UnknownFilter;
import org.xwiki.filter.test.TestFilterImplementation;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractFilterDescriptorManagerTest
{
    @Rule
    public MockitoComponentMockingRule<FilterDescriptorManager> mocker =
        new MockitoComponentMockingRule<FilterDescriptorManager>(DefaultFilterDescriptorManager.class);

    private Object filter;

    private FilterDescriptor filterDescriptor;

    public AbstractFilterDescriptorManagerTest(Object filter)
    {
        this.filter = filter;
    }

    @Before
    public void before() throws ComponentLookupException
    {
        ConverterManager converter = this.mocker.getInstance(ConverterManager.class);

        when(converter.convert(int.class, "42")).thenReturn(42);
        when(converter.convert(String.class, "default value")).thenReturn("default value");
        when(converter.convert(Color.class, "#ffffff")).thenReturn(Color.WHITE);
        when(converter.convert(new DefaultParameterizedType(null, Map.class, new Type[] { String.class, String.class }),
            "")).thenThrow(ConversionException.class);

        this.filterDescriptor = this.mocker.getComponentUnderTest().getFilterDescriptor(this.filter.getClass());
    }

    @Test
    public void containerWithParameters()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("containerwithparameters");

        Assert.assertNotNull(filterElement);

        FilterElementParameterDescriptor<?> parameter0 = filterElement.getParameters()[0];
        parameter0 = filterElement.getParameter("param0");

        Assert.assertEquals("param0", parameter0.getName());
        Assert.assertEquals(0, parameter0.getIndex());
        Assert.assertEquals(String.class, parameter0.getType());
    }

    @Test
    public void containerWithNamedParameters()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("containerwithnamedparameters");

        Assert.assertNotNull(filterElement);

        FilterElementParameterDescriptor<?> parameter0 = filterElement.getParameters()[0];
        parameter0 = filterElement.getParameter("namedParam");

        Assert.assertNotNull(parameter0);

        Assert.assertEquals("namedParam", parameter0.getName());
        Assert.assertEquals(0, parameter0.getIndex());
        Assert.assertEquals(String.class, parameter0.getType());
    }

    @Test
    public void nameInheritance() throws ComponentLookupException
    {
        FilterElementDescriptor filterElement = this.mocker.getComponentUnderTest()
            .getFilterDescriptor(TestFilterImplementation.class).getElement("containerwithnamedparameters");

        Assert.assertNotNull(filterElement);

        FilterElementParameterDescriptor<?> parameter0 = filterElement.getParameters()[0];
        parameter0 = filterElement.getParameter("namedParam");

        Assert.assertNotNull(parameter0);

        Assert.assertEquals("namedParam", parameter0.getName());
        Assert.assertEquals(0, parameter0.getIndex());
        Assert.assertEquals(String.class, parameter0.getType());
    }

    @Test
    public void withDefaultValue() throws ComponentLookupException
    {
        FilterElementDescriptor filterElement = this.mocker.getComponentUnderTest()
            .getFilterDescriptor(TestFilterImplementation.class).getElement("childwithdefaultvalue");

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

    @Test
    public void namedChild()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("childwithname");

        Assert.assertNotNull(filterElement);
        Assert.assertNull(filterElement.getBeginMethod());
        Assert.assertNull(filterElement.getEndMethod());
        Assert.assertNotNull(filterElement.getOnMethod());
    }

    @Test
    public void namedContainer()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("containerwithname");

        Assert.assertNotNull(filterElement);
        Assert.assertNotNull(filterElement.getBeginMethod());
        Assert.assertNotNull(filterElement.getEndMethod());
        Assert.assertNull(filterElement.getOnMethod());
    }

    @Test(expected = FilterException.class)
    public void proxyFailing() throws FilterException, ComponentLookupException
    {
        UnknownFilter filter = mock(UnknownFilter.class);
        doThrow(FilterException.class).when(filter).onUnknwon(any(), any());

        UnknownFilter proxyFilter = this.mocker.getComponentUnderTest().createFilterProxy(filter, UnknownFilter.class,
            FilterDescriptorManager.class);

        assertNotSame(filter, proxyFilter);

        proxyFilter.onUnknwon(null, null);
    }
}
