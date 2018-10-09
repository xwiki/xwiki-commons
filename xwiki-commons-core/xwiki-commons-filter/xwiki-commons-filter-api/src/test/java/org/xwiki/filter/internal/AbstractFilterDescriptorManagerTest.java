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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @version $Id$
 */
@ComponentTest
public abstract class AbstractFilterDescriptorManagerTest
{
    @MockComponent
    private ConverterManager converter;

    @InjectMockComponents
    private DefaultFilterDescriptorManager manager;

    private Object filter;

    private FilterDescriptor filterDescriptor;

    public AbstractFilterDescriptorManagerTest(Object filter)
    {
        this.filter = filter;
    }

    @BeforeEach
    public void beforeEach()
    {
        when(this.converter.convert(int.class, "42")).thenReturn(42);
        when(this.converter.convert(String.class, "default value")).thenReturn("default value");
        when(this.converter.convert(Color.class, "#ffffff")).thenReturn(Color.WHITE);
        when(this.converter
            .convert(new DefaultParameterizedType(null, Map.class, new Type[] { String.class, String.class }), ""))
                .thenThrow(ConversionException.class);

        this.filterDescriptor = this.manager.getFilterDescriptor(this.filter.getClass());
    }

    @Test
    public void containerWithParameters()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("containerwithparameters");

        assertNotNull(filterElement);

        FilterElementParameterDescriptor<?> parameter0 = filterElement.getParameters()[0];
        parameter0 = filterElement.getParameter("param0");

        assertEquals("param0", parameter0.getName());
        assertEquals(0, parameter0.getIndex());
        assertEquals(String.class, parameter0.getType());
    }

    @Test
    public void containerWithNamedParameters()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("containerwithnamedparameters");

        assertNotNull(filterElement);

        FilterElementParameterDescriptor<?> parameter0 = filterElement.getParameters()[0];
        parameter0 = filterElement.getParameter("namedParam");

        assertNotNull(parameter0);

        assertEquals("namedParam", parameter0.getName());
        assertEquals(0, parameter0.getIndex());
        assertEquals(String.class, parameter0.getType());
    }

    @Test
    public void nameInheritance()
    {
        FilterElementDescriptor filterElement =
            this.manager.getFilterDescriptor(TestFilterImplementation.class).getElement("containerwithnamedparameters");

        assertNotNull(filterElement);

        FilterElementParameterDescriptor<?> parameter0 = filterElement.getParameters()[0];
        parameter0 = filterElement.getParameter("namedParam");

        assertNotNull(parameter0);

        assertEquals("namedParam", parameter0.getName());
        assertEquals(0, parameter0.getIndex());
        assertEquals(String.class, parameter0.getType());
    }

    @Test
    public void withDefaultValue()
    {
        FilterElementDescriptor filterElement =
            this.manager.getFilterDescriptor(TestFilterImplementation.class).getElement("childwithdefaultvalue");

        assertNotNull(filterElement);

        FilterElementParameterDescriptor<Integer> parameter0 = filterElement.getParameter("int");

        assertEquals(Integer.valueOf(42), parameter0.getDefaultValue());

        FilterElementParameterDescriptor<String> parameter1 = filterElement.getParameter("string");

        assertEquals("default value", parameter1.getDefaultValue());

        FilterElementParameterDescriptor<Color> parameter2 = filterElement.getParameter("color");

        assertEquals(Color.WHITE, parameter2.getDefaultValue());

        FilterElementParameterDescriptor<Map<String, String>> parameter3 = filterElement.getParameter("map");

        assertEquals(Collections.EMPTY_MAP, parameter3.getDefaultValue());
    }

    @Test
    public void namedChild()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("childwithname");

        assertNotNull(filterElement);
        assertNull(filterElement.getBeginMethod());
        assertNull(filterElement.getEndMethod());
        assertNotNull(filterElement.getOnMethod());
    }

    @Test
    public void namedContainer()
    {
        FilterElementDescriptor filterElement = this.filterDescriptor.getElement("containerwithname");

        assertNotNull(filterElement);
        assertNotNull(filterElement.getBeginMethod());
        assertNotNull(filterElement.getEndMethod());
        assertNull(filterElement.getOnMethod());
    }

    @Test
    public void proxyFailing() throws FilterException
    {
        UnknownFilter filter = mock(UnknownFilter.class);
        doThrow(FilterException.class).when(filter).onUnknwon(any(), any());

        UnknownFilter proxyFilter =
            this.manager.createFilterProxy(filter, UnknownFilter.class, FilterDescriptorManager.class);

        assertNotSame(filter, proxyFilter);

        assertThrows(FilterException.class, () -> proxyFilter.onUnknwon(null, null));
    }
}
