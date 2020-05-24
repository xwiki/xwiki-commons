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
import java.io.StringWriter;

import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link StAXUtils}.
 *
 * @version $Id$
 * @since 5.2M1
 */
public class StAXUtilsTest
{
    @Test
    void getXMLStreamReader() throws XMLStreamException
    {
        // TODO: add support for XMLEventReader
        // assertNotNull(StAXUtils.getXMLStreamReader(new StAXSource(mockXMLEventReader())));
        assertNotNull(StAXUtils.getXMLStreamReader(new StAXSource(mockXMLStreamReader())));
        assertNotNull(StAXUtils.getXMLStreamReader(new StreamSource(new StringReader("<element/>"))));
    }

    @Test
    void getXMLEventReader() throws XMLStreamException
    {
        assertNotNull(StAXUtils.getXMLEventReader(new StAXSource(mockXMLEventReader())));
        assertNotNull(StAXUtils.getXMLEventReader(new StAXSource(mockXMLStreamReader())));
        assertNotNull(StAXUtils.getXMLEventReader(new StreamSource(new StringReader("<element/>"))));
    }

    @Test
    void getXMLStreamWriter() throws XMLStreamException
    {
        assertNotNull(StAXUtils.getXMLStreamWriter(new StAXResult(Mockito.mock(XMLEventWriter.class))));
        assertNotNull(StAXUtils.getXMLStreamWriter(new StAXResult(Mockito.mock(XMLStreamWriter.class))));
        assertNotNull(StAXUtils.getXMLStreamWriter(new StreamResult(new StringWriter())));
    }

    @Test
    void getXMLStreamWriterWithSAXREsult() throws TransformerConfigurationException, XMLStreamException
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        if (!tf.getFeature(SAXTransformerFactory.FEATURE)) {
            throw new RuntimeException("Did not find a SAX-compatible TransformerFactory.");
        }

        StringWriter output = new StringWriter();

        SAXTransformerFactory stf = (SAXTransformerFactory) tf;
        TransformerHandler th = stf.newTransformerHandler();
        th.setResult(new StreamResult(output));

        XMLStreamWriter writer = StAXUtils.getXMLStreamWriter(new SAXResult(th));

        writer.writeStartDocument();
        writer.writeStartElement("element");
        writer.writeAttribute("attribute", "value");
        writer.writeCharacters("characters");
        writer.writeCData("cdata");
        writer.writeEndElement();
        writer.writeEndDocument();

        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<element attribute=\"value\">"
            + "characters" + "<![CDATA[cdata]]>" + "</element>", output.getBuffer().toString());
    }

    private XMLStreamReader mockXMLStreamReader()
    {
        XMLStreamReader xmlStreamReader = Mockito.mock(XMLStreamReader.class);

        Mockito.when(xmlStreamReader.getEventType()).thenReturn(XMLStreamConstants.START_DOCUMENT);

        Location location = Mockito.mock(Location.class);

        Mockito.when(xmlStreamReader.getLocation()).thenReturn(location);

        return xmlStreamReader;
    }

    private XMLEventReader mockXMLEventReader() throws XMLStreamException
    {
        XMLEventReader xmlEventReader = Mockito.mock(XMLEventReader.class);

        XMLEvent event = Mockito.mock(XMLEvent.class);
        Mockito.when(event.getEventType()).thenReturn(XMLStreamConstants.START_DOCUMENT);

        Mockito.when(xmlEventReader.peek()).thenReturn(event);

        Location location = Mockito.mock(Location.class);

        Mockito.when(event.getLocation()).thenReturn(location);

        return xmlEventReader;
    }
}
