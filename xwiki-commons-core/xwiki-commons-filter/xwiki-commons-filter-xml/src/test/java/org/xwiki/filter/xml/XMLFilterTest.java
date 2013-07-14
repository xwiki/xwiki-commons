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
package org.xwiki.filter.xml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.filter.test.TestFilter;
import org.xwiki.filter.xml.parser.XMLParserFactory;
import org.xwiki.filter.xml.serializer.XMLSerializerFactory;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

@AllComponents
public class XMLFilterTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private void assertParseAndSerialize(String inputexpect) throws Exception
    {
        assertXML(inputexpect, inputexpect);
    }

    private void assertXML(String expect, String input) throws Exception
    {
        StringWriter stringWriter = new StringWriter();

        XMLSerializerFactory serializerFactory = this.componentManager.getInstance(XMLSerializerFactory.class);
        TestFilter testFilter =
            serializerFactory.createSerializer(TestFilter.class, new StreamResult(stringWriter), null);

        XMLParserFactory parserFactory = this.componentManager.getInstance(XMLParserFactory.class);
        parserFactory.parse(new StreamSource(new StringReader(input)), testFilter, null);

        Assert.assertEquals(expect, stringWriter.toString());
    }

    // Tests

    @Test
    public void testContainer() throws Exception
    {
        assertParseAndSerialize("<container></container>");
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
    }
}
