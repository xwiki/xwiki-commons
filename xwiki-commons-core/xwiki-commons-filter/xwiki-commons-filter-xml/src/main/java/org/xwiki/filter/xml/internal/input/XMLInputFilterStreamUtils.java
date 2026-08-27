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
package org.xwiki.filter.xml.internal.input;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.InputSource;
import org.xwiki.filter.input.InputStreamInputSource;
import org.xwiki.filter.input.ReaderInputSource;
import org.xwiki.filter.xml.input.SourceInputSource;
import org.xwiki.filter.xml.input.XMLInputProperties;
import org.xwiki.xml.stax.StAXUtils;

/**
 * @version $Id$
 * @since 6.2M1
 */
public final class XMLInputFilterStreamUtils
{
    private static final XMLInputFactory XML_INPUT_FACTORY = XMLInputFactory.newInstance();

    private static final String UNKNOWN_SOURCE = "Unknown source type [";

    private XMLInputFilterStreamUtils()
    {
        // Utility class
    }

    /**
     * @param properties the properties containing the source to read
     * @return the XML event reader to read the source with
     * @throws XMLStreamException when failing to create the reader
     * @throws IOException when failing to access the source
     * @throws FilterException when the source type is not supported
     * @since 9.5.2
     * @since 9.6RC1
     */
    public static XMLEventReader createXMLEventReader(XMLInputProperties properties)
        throws XMLStreamException, IOException, FilterException
    {
        return createXMLEventReader(XML_INPUT_FACTORY, properties);
    }

    /**
     * @param factory the factory to use to create the reader (a default one is used when null)
     * @param properties the properties containing the source to read
     * @return the XML event reader to read the source with
     * @throws XMLStreamException when failing to create the reader
     * @throws IOException when failing to access the source
     * @throws FilterException when the source type is not supported
     */
    public static XMLEventReader createXMLEventReader(XMLInputFactory factory, XMLInputProperties properties)
        throws XMLStreamException, IOException, FilterException
    {
        InputSource source = properties.getSource();

        return switch (source) {
            case ReaderInputSource readerSource ->
                getXMLInputFactory(factory).createXMLEventReader(readerSource.getReader());
            case InputStreamInputSource streamSource ->
                getXMLInputFactory(factory).createXMLEventReader(streamSource.getInputStream());
            case SourceInputSource sourceInputSource -> StAXUtils.getXMLEventReader(sourceInputSource.getSource());
            default -> throw new FilterException(UNKNOWN_SOURCE + source.getClass() + "]");
        };
    }

    /**
     * @param properties the properties containing the source to read
     * @return the XML stream reader to read the source with
     * @throws XMLStreamException when failing to create the reader
     * @throws IOException when failing to access the source
     * @throws FilterException when the source type is not supported
     */
    public static XMLStreamReader createXMLStreamReader(XMLInputProperties properties)
        throws XMLStreamException, IOException, FilterException
    {
        InputSource source = properties.getSource();

        return switch (source) {
            case ReaderInputSource readerInputSource ->
                XML_INPUT_FACTORY.createXMLStreamReader(readerInputSource.getReader());
            case InputStreamInputSource inputStreamInputSource ->
                XML_INPUT_FACTORY.createXMLStreamReader(inputStreamInputSource.getInputStream());
            case SourceInputSource sourceInputSource -> StAXUtils.getXMLStreamReader(sourceInputSource.getSource());
            default -> throw new FilterException(UNKNOWN_SOURCE + source.getClass() + "]");
        };
    }

    static XMLEventReader newTestReaderDirect(String content) throws XMLStreamException
    {
        XMLInputFactory factory = null;

        return factory.createXMLEventReader(new StringReader(content));
    }

    private static XMLInputFactory nullTestFactory()
    {
        return null;
    }

    static XMLEventReader newTestReaderIndirect(String content) throws XMLStreamException
    {
        return nullTestFactory().createXMLEventReader(new StringReader(content));
    }

    private static XMLInputFactory getXMLInputFactory(XMLInputFactory factory)
    {
        return factory != null ? factory : XML_INPUT_FACTORY;
    }
}
