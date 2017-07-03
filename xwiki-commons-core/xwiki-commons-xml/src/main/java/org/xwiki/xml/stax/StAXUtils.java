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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

import javanet.staxutils.XMLEventStreamWriter;
import javanet.staxutils.XMLStreamEventReader;

/**
 * Various tools related to StAX API.
 *
 * @version $Id$
 * @since 5.2M1
 */
public final class StAXUtils
{
    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    /**
     * Utility class.
     */
    private StAXUtils()
    {
    }

    /**
     * Extract or create an instance of {@link XMLStreamReader} from the provided {@link Source}.
     *
     * @param source the source
     * @return the {@link XMLStreamReader}
     * @throws XMLStreamException when failing to extract xml stream reader
     */
    public static XMLStreamReader getXMLStreamReader(Source source) throws XMLStreamException
    {
        return getXMLStreamReader(XML_INPUT_FACTORY, source);
    }

    /**
     * Extract or create an instance of {@link XMLStreamReader} from the provided {@link Source}.
     *
     * @param factory the {@link XMLStreamReader} to use (if needed)
     * @param source the source
     * @return the {@link XMLStreamReader}
     * @throws XMLStreamException when failing to extract xml stream reader
     * @since 9.5
     * @since 9.6RC1
     */
    public static XMLStreamReader getXMLStreamReader(XMLInputFactory factory, Source source) throws XMLStreamException
    {
        XMLStreamReader xmlStreamReader;

        if (source instanceof StAXSource) {
            // StAXSource is not supported by standard XMLInputFactory
            StAXSource staxSource = (StAXSource) source;
            if (staxSource.getXMLStreamReader() != null) {
                xmlStreamReader = staxSource.getXMLStreamReader();
            } else {
                // TODO: add support for XMLStreamReader -> XMLEventReader
                throw new XMLStreamException("XMLEventReader is not supported as source");
            }
        } else {
            xmlStreamReader = factory.createXMLStreamReader(source);
        }

        return xmlStreamReader;
    }

    /**
     * Extract or create an instance of {@link XMLEventReader} from the provided {@link Source}.
     *
     * @param source the source
     * @return the {@link XMLEventReader}
     * @throws XMLStreamException when failing to extract xml event reader
     */
    public static XMLEventReader getXMLEventReader(Source source) throws XMLStreamException
    {
        XMLEventReader xmlEventReader;

        if (source instanceof StAXSource) {
            // StAXSource is not supported by standard XMLInputFactory
            StAXSource staxSource = (StAXSource) source;
            if (staxSource.getXMLEventReader() != null) {
                xmlEventReader = staxSource.getXMLEventReader();
            } else {
                xmlEventReader = new XMLStreamEventReader(staxSource.getXMLStreamReader());
            }
        } else {
            xmlEventReader = XML_INPUT_FACTORY.createXMLEventReader(source);
        }

        return xmlEventReader;
    }

    /**
     * Extract or create an instance of {@link XMLStreamWriter} from the provided {@link Result}.
     *
     * @param result the result
     * @return the {@link XMLStreamWriter}
     * @throws XMLStreamException when failing to extract xml stream writer
     */
    public static XMLStreamWriter getXMLStreamWriter(Result result) throws XMLStreamException
    {
        return getXMLStreamWriter(XML_OUTPUT_FACTORY, result);
    }

    /**
     * Extract or create an instance of {@link XMLStreamWriter} from the provided {@link Result}.
     *
     * @param factory the {@link XMLOutputFactory} to use (if needed)
     * @param result the result
     * @return the {@link XMLStreamWriter}
     * @throws XMLStreamException when failing to extract xml stream writer
     * @since 9.5.2
     * @since 9.6RC1
     */
    public static XMLStreamWriter getXMLStreamWriter(XMLOutputFactory factory, Result result) throws XMLStreamException
    {
        XMLStreamWriter xmlStreamWriter;

        if (result instanceof SAXResult) {
            // SAXResult is not supported by the standard XMLOutputFactory
            xmlStreamWriter = new XMLEventStreamWriter(new SAXEventWriter(((SAXResult) result).getHandler()));
        } else if (result instanceof StAXResult) {
            // XMLEventWriter is not supported as result of XMLOutputFactory#createXMLStreamWriter
            StAXResult staxResult = (StAXResult) result;
            if (staxResult.getXMLStreamWriter() != null) {
                xmlStreamWriter = staxResult.getXMLStreamWriter();
            } else {
                xmlStreamWriter = new XMLEventStreamWriter(staxResult.getXMLEventWriter());
            }
        } else {
            xmlStreamWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(result);
        }

        return xmlStreamWriter;
    }

    /**
     * Go to the end of the current element. This include skipping any children element.
     *
     * @param xmlReader the XML stream reader
     * @return the type of the new current event
     * @throws XMLStreamException if there is an error processing the underlying XML source
     * @since 5.3M1
     */
    public static int skipElement(XMLStreamReader xmlReader) throws XMLStreamException
    {
        if (!xmlReader.isStartElement()) {
            throw new XMLStreamException("Current node is not start element");
        }

        if (!xmlReader.isEndElement()) {
            for (xmlReader.next(); !xmlReader.isEndElement(); xmlReader.next()) {
                if (xmlReader.isStartElement()) {
                    skipElement(xmlReader);
                }
            }
        }

        return xmlReader.getEventType();
    }
}
