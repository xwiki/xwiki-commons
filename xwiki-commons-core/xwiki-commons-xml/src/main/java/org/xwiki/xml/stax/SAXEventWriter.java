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

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.util.XMLEventConsumer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import javanet.staxutils.BaseXMLEventWriter;
import javanet.staxutils.DummyLocator;
import javanet.staxutils.helpers.XMLFilterImplEx;

/**
 * Receive and convert StAX events to SAX events.
 * <p>
 * Extends {@link XMLEventConsumer} with {@link javax.xml.stream.XMLEventWriter} methods.
 *
 * @version $Id$
 * @since 5.2M1
 */
public class SAXEventWriter extends BaseXMLEventWriter
{
    /**
     * The SAX filter.
     */
    private XMLFilterImplEx filter;

    /**
     * The depth of XML elements.
     */
    private int depth;

    /**
     * @param handler the content handler
     */
    public SAXEventWriter(ContentHandler handler)
    {
        this.filter = new XMLFilterImplEx();
        this.filter.setContentHandler(handler);

        if (handler instanceof LexicalHandler) {
            this.filter.setLexicalHandler((LexicalHandler) handler);
        }
        if (handler instanceof ErrorHandler) {
            this.filter.setErrorHandler((ErrorHandler) handler);
        }
    }

    @Override
    protected void sendEvent(XMLEvent event) throws XMLStreamException
    {
        convertEvent(event);
    }

    /**
     * @param event the XML event to convert
     * @throws XMLStreamException
     */
    private void convertEvent(XMLEvent event) throws XMLStreamException
    {
        try {
            if (event.isStartDocument()) {
                this.handleStartDocument(event);
            } else if (event.isEndDocument()) {
                this.handleEndDocument();
            } else {
                // These are all of the events listed in the javadoc for
                // XMLEvent.
                // The spec only really describes 11 of them.
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        this.depth++;
                        this.handleStartElement(event.asStartElement());
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        this.handleEndElement(event.asEndElement());
                        this.depth--;
                        if (this.depth == 0) {
                            break;
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        this.handleCharacters(event.asCharacters());
                        break;
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        this.handlePI((ProcessingInstruction) event);
                        break;
                    case XMLStreamConstants.COMMENT:
                        this.handleComment((Comment) event);
                        break;
                    case XMLStreamConstants.CDATA:
                        this.handleCDATA((Characters) event);
                        break;
                    default:
                        break;
                }
            }
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleEndDocument() throws SAXException
    {
        this.filter.endDocument();
    }

    private void handleStartDocument(final XMLEvent event) throws SAXException
    {
        final Location location = event.getLocation();
        if (location != null) {
            this.filter.setDocumentLocator(new Locator()
            {
                @Override
                public int getColumnNumber()
                {
                    return location.getColumnNumber();
                }

                @Override
                public int getLineNumber()
                {
                    return location.getLineNumber();
                }

                @Override
                public String getPublicId()
                {
                    return location.getPublicId();
                }

                @Override
                public String getSystemId()
                {
                    return location.getSystemId();
                }
            });
        } else {
            this.filter.setDocumentLocator(new DummyLocator());
        }
        this.filter.startDocument();
    }

    private void handlePI(ProcessingInstruction event) throws SAXException
    {
        this.filter.processingInstruction(event.getTarget(), event.getData());
    }

    private void handleCharacters(Characters event) throws SAXException
    {
        this.filter.characters(event.getData().toCharArray(), 0, event.getData().length());
    }

    private void handleEndElement(EndElement event) throws XMLStreamException
    {
        QName qName = event.getName();

        try {
            // fire endElement
            String prefix = qName.getPrefix();
            String rawname;
            if (prefix == null || prefix.length() == 0) {
                rawname = qName.getLocalPart();
            } else {
                rawname = prefix + ':' + qName.getLocalPart();
            }

            this.filter.endElement(qName.getNamespaceURI(), qName.getLocalPart(), rawname);

            // end namespace bindings
            for (@SuppressWarnings("unchecked") Iterator<Namespace> i = event.getNamespaces(); i.hasNext();) {
                String nsprefix = i.next().getPrefix();
                // true for default namespace
                if (nsprefix == null) {
                    nsprefix = "";
                }
                this.filter.endPrefixMapping(nsprefix);
            }
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleStartElement(StartElement event) throws XMLStreamException
    {
        try {
            // start namespace bindings
            for (@SuppressWarnings("unchecked") Iterator<Namespace> i = event.getNamespaces(); i.hasNext();) {
                String prefix = i.next().getPrefix();
                // true for default namespace
                if (prefix == null) {
                    prefix = "";
                }
                this.filter.startPrefixMapping(prefix, event.getNamespaceURI(prefix));
            }

            // fire startElement
            QName qName = event.getName();
            String prefix = qName.getPrefix();
            String rawname;
            if (prefix == null || prefix.length() == 0) {
                rawname = qName.getLocalPart();
            } else {
                rawname = prefix + ':' + qName.getLocalPart();
            }
            Attributes saxAttrs = this.getAttributes(event);
            this.filter.startElement(qName.getNamespaceURI(), qName.getLocalPart(), rawname, saxAttrs);
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * Get the attributes associated with the given START_ELEMENT StAXevent.
     *
     * @param event the StAX start element event
     * @return the StAX attributes converted to an org.xml.sax.Attributes
     */
    private Attributes getAttributes(StartElement event)
    {
        AttributesImpl attrs = new AttributesImpl();

        if (!event.isStartElement()) {
            throw new InternalError("getAttributes() attempting to process: " + event);
        }

        // Add namspace declarations if required
        if (this.filter.getNamespacePrefixes()) {
            for (@SuppressWarnings("unchecked") Iterator<Namespace> i = event.getNamespaces(); i.hasNext();) {
                Namespace staxNamespace = i.next();
                String uri = staxNamespace.getNamespaceURI();
                if (uri == null) {
                    uri = "";
                }

                String prefix = staxNamespace.getPrefix();
                if (prefix == null) {
                    prefix = "";
                }

                String qName = "xmlns";
                if (prefix.length() == 0) {
                    prefix = qName;
                } else {
                    qName = qName + ':' + prefix;
                }
                attrs.addAttribute("http://www.w3.org/2000/xmlns/", prefix, qName, "CDATA", uri);
            }
        }

        // gather non-namespace attrs
        for (@SuppressWarnings("unchecked") Iterator<Attribute> i = event.getAttributes(); i.hasNext();) {
            Attribute staxAttr = i.next();

            String uri = staxAttr.getName().getNamespaceURI();
            if (uri == null) {
                uri = "";
            }
            String localName = staxAttr.getName().getLocalPart();
            String prefix = staxAttr.getName().getPrefix();
            String qName;
            if (prefix == null || prefix.length() == 0) {
                qName = localName;
            } else {
                qName = prefix + ':' + localName;
            }
            String type = staxAttr.getDTDType();
            String value = staxAttr.getValue();

            attrs.addAttribute(uri, localName, qName, type, value);
        }

        return attrs;
    }

    private void handleComment(Comment comment) throws XMLStreamException
    {
        try {
            String text = comment.getText();
            this.filter.comment(text.toCharArray(), 0, text.length());
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleCDATA(Characters event) throws SAXException
    {
        this.filter.startCDATA();
        this.filter.characters(event.getData().toCharArray(), 0, event.getData().length());
        this.filter.endCDATA();
    }
}
