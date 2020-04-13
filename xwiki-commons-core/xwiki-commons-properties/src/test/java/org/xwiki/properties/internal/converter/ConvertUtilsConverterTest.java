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
package org.xwiki.properties.internal.converter;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

/**
 * Validate {@link ConvertUtilsConverter} component.
 *
 * @version $Id$
 */
@AllComponents
public class ConvertUtilsConverterTest
{
    @Rule
    public MockitoComponentMockingRule<ConverterManager> mocker = new MockitoComponentMockingRule<>(
        DefaultConverterManager.class);

    public Integer[] field;

    @Test
    public void testConvert() throws SecurityException, ComponentLookupException
    {
        Assert.assertEquals(Integer.valueOf(42), this.mocker.getComponentUnderTest().convert(Integer.class, "42"));
    }

    @Test
    public void testConvertArrays() throws SecurityException, NoSuchFieldException, ComponentLookupException
    {
        Assert.assertArrayEquals(new int[] { 1, 2, 3 }, this.mocker.getComponentUnderTest().<int[]>convert(int[].class, "1, 2, 3"));

        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 },
            this.mocker.getComponentUnderTest().<Integer[]>convert(Integer[].class, "1, 2, 3"));

        Assert.assertArrayEquals(new Integer[] { 1, 2, 3 }, this.mocker.getComponentUnderTest().<Integer[]>convert(
            ConvertUtilsConverterTest.class.getField("field").getGenericType(), "1, 2, 3"));
    }

    @Test
    public void testConvertWhenNoConverterAvailable() throws ComponentLookupException
    {
        try {
            this.mocker.getComponentUnderTest().convert(ConvertUtilsConverter.class, "");
            Assert.fail("Should have thrown an exception here");
        } catch (ConversionException expected) {
            Assert.assertEquals("Failed to find a Converter to convert from [java.lang.String] to " + "["
                + ConvertUtilsConverter.class.getName() + "]", expected.getMessage());
        }
    }
}
