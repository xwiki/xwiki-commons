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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.ContentHandler;

/**
 * Receive and convert StAX events to SAX events.
 * <p>
 * Extends {@link SAXEventConsumer} with {@link XMLEventWriter} methods.
 * 
 * @version $Id$
 * @since 5.2M1
 */
public class SAXEventWriter extends SAXEventConsumer implements XMLEventWriter
{
    /**
     * @param handler the content handler
     */
    public SAXEventWriter(ContentHandler handler)
    {
        super(handler);
    }

    @Override
    public void add(XMLEventReader reader) throws XMLStreamException
    {
        while (reader.hasNext()) {
            add(reader.nextEvent());
        }
    }

    // Not supported by SAX

    @Override
    public void flush() throws XMLStreamException
    {
        // Not supported by ContentHandler
    }

    @Override
    public void close() throws XMLStreamException
    {
        // Not supported by ContentHandler
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException
    {
        // Not supported by ContentHandler

        return null;
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException
    {
        // Not supported by ContentHandler
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException
    {
        // Not supported by ContentHandler
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException
    {
        // Not supported by ContentHandler
    }

    @Override
    public NamespaceContext getNamespaceContext()
    {
        // Not supported by ContentHandler

        return null;
    }
}
