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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.jmock.AbstractComponentTestCase;

/**
 * Validate {@link ListConverter} component.
 *
 * @version $Id$
 */
public class ListConverterTest extends AbstractComponentTestCase
{
    private ConverterManager converterManager;

    public List<Integer> listField1;

    public List<List<Integer>> listField2;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.converterManager = getComponentManager().getInstance(ConverterManager.class);
    }

    @Test
    public void testConvert() throws SecurityException, NoSuchFieldException
    {
        Assert.assertEquals(Arrays.asList("1", "2", "3"), this.converterManager.convert(List.class, "1, 2, 3"));

        Assert.assertEquals(Arrays.asList("1", "\n", "2", "\n", "3"),
            this.converterManager.convert(List.class, "1,\n 2,\n 3"));

        Assert.assertEquals(Arrays.asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)),
            this.converterManager.convert(ListConverterTest.class.getField("listField1").getGenericType(), "1, 2, 3"));

        Assert.assertEquals(Arrays.asList(Arrays.asList(1, 2, 3), Arrays.asList(4, 5, 6)), this.converterManager
            .convert(ListConverterTest.class.getField("listField2").getGenericType(), "'\\'1\\', 2, 3', \"4, 5, 6\""));

        Assert.assertEquals(Arrays.asList("1:2"), this.converterManager.convert(List.class, "1:2"));
    }

    @Test
    public void testConvertFromList()
    {
        List<String> expect = Arrays.asList("1", "2", "3");

        Assert.assertSame(expect, this.converterManager.convert(List.class, expect));
    }
}
