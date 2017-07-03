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

    /**
     * @since 9.5.2
     * @since 9.6RC1
     */
    public static XMLEventReader createXMLEventReader(XMLInputProperties properties)
        throws XMLStreamException, IOException, FilterException
    {
        return createXMLEventReader(XML_INPUT_FACTORY, properties);
    }

    public static XMLEventReader createXMLEventReader(XMLInputFactory factory, XMLInputProperties properties)
        throws XMLStreamException, IOException, FilterException
    {
        XMLEventReader xmlEventReader;

        InputSource source = properties.getSource();

        if (source instanceof ReaderInputSource) {
            xmlEventReader = getXMLInputFactory(factory).createXMLEventReader(((ReaderInputSource) source).getReader());
        } else if (source instanceof InputStreamInputSource) {
            xmlEventReader =
                getXMLInputFactory(factory).createXMLEventReader(((InputStreamInputSource) source).getInputStream());
        } else if (source instanceof SourceInputSource) {
            xmlEventReader = StAXUtils.getXMLEventReader(((SourceInputSource) source).getSource());
        } else {
            throw new FilterException("Unknown source type [" + source.getClass() + "]");
        }

        return xmlEventReader;
    }

    public static XMLStreamReader createXMLStreamReader(XMLInputProperties properties)
        throws XMLStreamException, IOException, FilterException
    {
        XMLStreamReader xmlStreamReader;

        InputSource source = properties.getSource();

        if (source instanceof ReaderInputSource) {
            xmlStreamReader = XML_INPUT_FACTORY.createXMLStreamReader(((ReaderInputSource) source).getReader());
        } else if (source instanceof InputStreamInputSource) {
            xmlStreamReader =
                XML_INPUT_FACTORY.createXMLStreamReader(((InputStreamInputSource) source).getInputStream());
        } else if (source instanceof SourceInputSource) {
            xmlStreamReader = StAXUtils.getXMLStreamReader(((SourceInputSource) source).getSource());
        } else {
            throw new FilterException("Unknown source type [" + source.getClass() + "]");
        }

        return xmlStreamReader;
    }

    private static XMLInputFactory getXMLInputFactory(XMLInputFactory factory)
    {
        return factory != null ? factory : XML_INPUT_FACTORY;
    }
}
