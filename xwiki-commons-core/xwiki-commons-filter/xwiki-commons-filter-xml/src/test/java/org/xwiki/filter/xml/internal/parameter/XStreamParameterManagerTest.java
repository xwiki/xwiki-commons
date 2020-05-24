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
package org.xwiki.filter.xml.internal.parameter;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link XStreamParameterManager}.
 *
 * @version $Id$
 */
@ComponentTest()
public class XStreamParameterManagerTest
{
    @InjectMockComponents
    private XStreamParameterManager parameters;

    private void assertSerialize(String expect, Type type, Object object)
        throws XMLStreamException, FactoryConfigurationError
    {
        StringWriter sw = new StringWriter();
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
        this.parameters.serialize(type, object, writer);
        writer.flush();
        writer.close();

        assertEquals(expect, sw.toString());
    }

    @Test
    void testSerializeUnknownMapImplementation()
        throws ComponentLookupException, XMLStreamException, FactoryConfigurationError, IOException
    {
        Map<String, String> customMap = new HashMap()
        {
        };
        customMap.put("key", "value");

        assertSerialize("<entry><string>key</string><string>value</string></entry>", Map.class, customMap);
    }

    @Test
    void testSerializeUnknowncollectionImplementation()
        throws ComponentLookupException, XMLStreamException, FactoryConfigurationError, IOException
    {
        Collection<String> customMap = new ArrayList()
        {
        };
        customMap.add("value");

        assertSerialize("<string>value</string>", Collection.class, customMap);
    }

    @Test
    void testSerializeUnknownListImplementation()
        throws ComponentLookupException, XMLStreamException, FactoryConfigurationError, IOException
    {
        List<String> customMap = new ArrayList()
        {
        };
        customMap.add("value");

        assertSerialize("<string>value</string>", List.class, customMap);
    }
}
