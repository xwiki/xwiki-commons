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

import java.lang.reflect.Type;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.descriptor.ComponentRole;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.jmock.AbstractComponentTestCase;

/**
 * Validate {@link LocaleConverter} component.
 *
 * @version $Id$
 */
public class TypeConverterTest extends AbstractComponentTestCase
{
    private ConverterManager converterManager;

    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.converterManager = getComponentManager().getInstance(ConverterManager.class);
    }

    @Test
    public void testConvertToType()
    {
        Type simpleType = ComponentRole.class;
        Assert.assertEquals(simpleType,
            this.converterManager.convert(Type.class, "org.xwiki.component.descriptor.ComponentRole"));

        Type genericsType = new DefaultParameterizedType(null, ComponentRole.class, String.class);
        Assert
            .assertEquals(genericsType, this.converterManager.convert(Type.class,
                "org.xwiki.component.descriptor.ComponentRole<java.lang.String>"));
    }

    @Test
    public void testConvertToString()
    {
        Type simpleType = ComponentRole.class;
        Assert.assertEquals("org.xwiki.component.descriptor.ComponentRole",
            this.converterManager.convert(String.class, simpleType));

        Type genericsType = new DefaultParameterizedType(null, ComponentRole.class, String.class);
        Assert.assertEquals("org.xwiki.component.descriptor.ComponentRole<java.lang.String>",
            this.converterManager.convert(String.class, genericsType));
    }
}
