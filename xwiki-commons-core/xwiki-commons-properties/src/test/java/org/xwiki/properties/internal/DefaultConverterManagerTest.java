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

import java.awt.Color;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.properties.ConverterManager;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

/**
 * Validate {@link DefaultConverterManager}.
 *
 * @version $Id$
 */
@AllComponents
public class DefaultConverterManagerTest
{
    @Rule
    public MockitoComponentMockingRule<ConverterManager> mocker = new MockitoComponentMockingRule<>(
        DefaultConverterManager.class);

    public enum TestEnum
    {
        ENUMVALUE
    }

    @Test
    public void testConvert() throws ComponentLookupException
    {
        Assert.assertEquals(Integer.valueOf(42), this.mocker.getComponentUnderTest().convert(Integer.class, "42"));
    }

    @Test
    public void testConvertEnum() throws ComponentLookupException
    {
        Assert.assertEquals(TestEnum.ENUMVALUE, this.mocker.getComponentUnderTest()
            .convert(TestEnum.class, "ENUMVALUE"));
    }

    @Test
    public void testConvertColor() throws ComponentLookupException
    {
        Assert.assertEquals(Color.WHITE, this.mocker.getComponentUnderTest().convert(Color.class, "#ffffff"));
    }

    @Test(expected = ConversionException.class)
    public void testConvertUnsupportedType() throws ComponentLookupException
    {
        this.mocker.getComponentUnderTest().convert(DefaultConverterManagerTest.class, "#ffffff");
    }

    @Test
    public void testConvertSame() throws ComponentLookupException
    {
        Assert.assertSame(Color.WHITE, this.mocker.getComponentUnderTest().convert(Color.class, Color.WHITE));
    }
}
