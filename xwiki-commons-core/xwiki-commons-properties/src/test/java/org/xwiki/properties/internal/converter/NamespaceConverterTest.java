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
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.namespace.Namespace;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.jmock.AbstractComponentTestCase;

/**
 * Validate {@link LocaleConverter} component.
 *
 * @version $Id$
 */
public class NamespaceConverterTest extends AbstractComponentTestCase
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
    public void testConvertToNamespace()
    {
        Assert.assertEquals(new Namespace("wiki", "wikiid"), this.converterManager.convert(Namespace.class, "wiki:wikiid"));
    }

    @Test
    public void testConvertToString()
    {
        Assert.assertEquals("wiki:wikiid", this.converterManager.convert(String.class, new Namespace("wiki", "wikiid")));
    }
}
