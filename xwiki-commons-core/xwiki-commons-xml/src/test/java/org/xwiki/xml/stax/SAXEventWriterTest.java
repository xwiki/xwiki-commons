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
package org.xwiki.xml.stax;

import java.io.StringReader;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

import javanet.staxutils.helpers.XMLFilterImplEx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SAXEventWriter}.
 *
 * @version $Id$
 */
class SAXEventWriterTest
{
    private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    private final ContentHandler handler = mock();

    private final SAXEventWriter writer = new SAXEventWriter(this.handler);

    @Test
    void addPrefixedAndUnprefixedElementsAndAttributes() throws Exception
    {
        XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new StringReader(
            "<root xmlns=\"urn:default\" xmlns:p=\"urn:p\" attr=\"value\" p:pattr=\"pvalue\"><p:child/></root>"));

        this.writer.add(reader);

        verify(this.handler).startPrefixMapping("", "urn:default");
        verify(this.handler).startPrefixMapping("p", "urn:p");

        ArgumentCaptor<Attributes> rootAttributes = ArgumentCaptor.forClass(Attributes.class);
        verify(this.handler).startElement(eq("urn:default"), eq("root"), eq("root"), rootAttributes.capture());
        Attributes attributes = rootAttributes.getValue();
        // Namespace declarations are not reported as attributes by default.
        assertEquals(2, attributes.getLength());
        assertEquals("value", attributes.getValue("", "attr"));
        assertEquals("attr", attributes.getQName(attributes.getIndex("", "attr")));
        assertEquals("pvalue", attributes.getValue("urn:p", "pattr"));
        assertEquals("p:pattr", attributes.getQName(attributes.getIndex("urn:p", "pattr")));

        verify(this.handler).startElement(eq("urn:p"), eq("child"), eq("p:child"), any());
        verify(this.handler).endElement("urn:p", "child", "p:child");
        verify(this.handler).endElement("urn:default", "root", "root");
        verify(this.handler).endPrefixMapping("");
        verify(this.handler).endPrefixMapping("p");
    }

    @Test
    void addElementWithNamespacePrefixesAndNullValues() throws Exception
    {
        // There's no public API to ask for namespace declarations to be reported as attributes.
        XMLFilterImplEx filter = (XMLFilterImplEx) FieldUtils.readField(this.writer, "filter", true);
        filter.setNamespacePrefixes(true);

        Namespace defaultNamespace = mockNamespace(null, null);
        Namespace prefixedNamespace = mockNamespace("p", "urn:p");

        Attribute attribute = mock();
        when(attribute.getName()).thenReturn(new QName("attr"));
        when(attribute.getDTDType()).thenReturn("CDATA");
        when(attribute.getValue()).thenReturn("value");

        StartElement startElement = mock();
        when(startElement.getEventType()).thenReturn(XMLEvent.START_ELEMENT);
        when(startElement.isStartElement()).thenReturn(true);
        when(startElement.asStartElement()).thenReturn(startElement);
        when(startElement.getName()).thenReturn(new QName("urn:p", "element", "p"));
        when(startElement.getNamespaces()).thenAnswer(invocation -> List.of(defaultNamespace, prefixedNamespace)
            .iterator());
        when(startElement.getAttributes()).thenAnswer(invocation -> List.of(attribute).iterator());

        EndElement endElement = mock();
        when(endElement.getEventType()).thenReturn(XMLEvent.END_ELEMENT);
        when(endElement.asEndElement()).thenReturn(endElement);
        when(endElement.getName()).thenReturn(new QName("urn:p", "element", "p"));
        when(endElement.getNamespaces()).thenAnswer(invocation -> List.of(defaultNamespace).iterator());

        this.writer.add(startElement);
        this.writer.add(endElement);

        verify(this.handler).startPrefixMapping("", null);
        verify(this.handler).startPrefixMapping("p", "urn:p");

        ArgumentCaptor<Attributes> captor = ArgumentCaptor.forClass(Attributes.class);
        verify(this.handler).startElement(eq("urn:p"), eq("element"), eq("p:element"), captor.capture());
        Attributes attributes = captor.getValue();
        assertEquals(3, attributes.getLength());
        assertAttribute(attributes, 0, XMLNS_URI, "xmlns", "xmlns", "");
        assertAttribute(attributes, 1, XMLNS_URI, "p", "xmlns:p", "urn:p");
        assertAttribute(attributes, 2, "", "attr", "attr", "value");
        assertEquals("CDATA", attributes.getType(2));

        verify(this.handler).endElement("urn:p", "element", "p:element");
        verify(this.handler).endPrefixMapping("");
    }

    private Namespace mockNamespace(String prefix, String uri)
    {
        Namespace namespace = mock();
        when(namespace.getPrefix()).thenReturn(prefix);
        when(namespace.getNamespaceURI()).thenReturn(uri);
        return namespace;
    }

    private void assertAttribute(Attributes attributes, int index, String uri, String localName, String qName,
        String value)
    {
        assertEquals(uri, attributes.getURI(index));
        assertEquals(localName, attributes.getLocalName(index));
        assertEquals(qName, attributes.getQName(index));
        assertEquals(value, attributes.getValue(index));
    }
}
