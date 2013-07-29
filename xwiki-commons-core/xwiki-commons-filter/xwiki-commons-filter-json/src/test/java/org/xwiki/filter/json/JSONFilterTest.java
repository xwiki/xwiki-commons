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
package org.xwiki.filter.json;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.filter.json.parser.JSONParserFactory;
import org.xwiki.filter.json.serializer.JSONSerializerFactory;
import org.xwiki.filter.test.ExtendedTestData;
import org.xwiki.filter.test.TestFilter;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

@AllComponents
public class JSONFilterTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private StringWriter stringWriter;

    private TestFilter createFilter() throws ComponentLookupException, XMLStreamException, FactoryConfigurationError, IOException
    {
        this.stringWriter = new StringWriter();

        JSONSerializerFactory serializerFactory = this.componentManager.getInstance(JSONSerializerFactory.class);
        return serializerFactory.createSerializer(TestFilter.class, this.stringWriter, null);
    }

    private void assertParseAndSerialize(String inputexpect) throws Exception
    {
        assertParseAndSerialize(inputexpect, inputexpect);
    }

    private void assertParseAndSerialize(String expect, String input) throws Exception
    {
        TestFilter testFilter = createFilter();

        JSONParserFactory parserFactory = this.componentManager.getInstance(JSONParserFactory.class);
        parserFactory.parse(new StringReader(input), testFilter, null);

        assertSerialized(expect);
    }

    private void assertSerialized(String expect)
    {
        Assert.assertEquals(expect, this.stringWriter.toString());
    }

    // Tests

    @Test
    public void testContainer() throws Exception
    {
        assertParseAndSerialize("{\"t\":\"container\"}");
    }

    @Test
    public void testContainerAndChild() throws Exception
    {
        assertParseAndSerialize("<container><child></child></container>");
    }

    @Test
    public void testContainerWithParameters() throws Exception
    {
        assertParseAndSerialize("<containerWithParameters p0=\"value0\" p1=\"1\"></containerWithParameters>");
    }

    @Test
    public void testContainerWithNamedParameters() throws Exception
    {
        assertParseAndSerialize("<containerWithNamedParameters namedParam=\"value0\" p1=\"1\"></containerWithNamedParameters>");
    }

    @Test
    public void testContainerWithMap() throws Exception
    {
        assertParseAndSerialize("<containerWithMap><p><map><entry><string>key</string><int>1</int></entry></map></p></containerWithMap>");
        assertParseAndSerialize(
            "<containerWithMap><p><map><entry><string>key</string><int>1</int></entry></map></p></containerWithMap>",
            "<containerWithMap><p><p0><entry><string>key</string><int>1</int></entry></p0></p></containerWithMap>");
    }

    // Serialize

    @Test
    public void testSerializeExtendedTestData() throws ComponentLookupException, XMLStreamException,
        FactoryConfigurationError, IOException
    {
        TestFilter testFilter = createFilter();

        ExtendedTestData extendedTestData = new ExtendedTestData();

        testFilter.beginCustomData(extendedTestData);
        testFilter.endCustomData(extendedTestData);

        assertSerialized("<customData><p><custom><field1>1</field1></custom></p></customData>");
    }
}
