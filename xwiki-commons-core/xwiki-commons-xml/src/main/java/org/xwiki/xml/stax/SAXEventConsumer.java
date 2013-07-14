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

import javanet.staxutils.DummyLocator;
import javanet.staxutils.helpers.XMLFilterImplEx;

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

/**
 * Receive and convert StAX events to SAX events.
 * <p>
 * It's a modification of the code from javanet.staxutils.XMLEventReaderToContentHandler that can be used as
 * {@link XMLEventConsumer} since SAX result is not supported by standard {@link javax.xml.stream.XMLOutputFactory}.
 * 
 * @version $Id$
 * @since 5.2M1
 */
public class SAXEventConsumer implements XMLEventConsumer
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
    public SAXEventConsumer(ContentHandler handler)
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
    public void add(XMLEvent event) throws XMLStreamException
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
                    case XMLStreamConstants.ENTITY_REFERENCE:
                        this.handleEntityReference();
                        break;
                    case XMLStreamConstants.PROCESSING_INSTRUCTION:
                        this.handlePI((ProcessingInstruction) event);
                        break;
                    case XMLStreamConstants.COMMENT:
                        this.handleComment((Comment) event);
                        break;
                    case XMLStreamConstants.DTD:
                        this.handleDTD();
                        break;
                    case XMLStreamConstants.ATTRIBUTE:
                        this.handleAttribute();
                        break;
                    case XMLStreamConstants.NAMESPACE:
                        this.handleNamespace();
                        break;
                    case XMLStreamConstants.CDATA:
                        this.handleCDATA();
                        break;
                    case XMLStreamConstants.ENTITY_DECLARATION:
                        this.handleEntityDecl();
                        break;
                    case XMLStreamConstants.NOTATION_DECLARATION:
                        this.handleNotationDecl();
                        break;
                    case XMLStreamConstants.SPACE:
                        this.handleSpace();
                        break;
                    default:
                        throw new InternalError("processing event: " + event);
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
                public int getColumnNumber()
                {
                    return location.getColumnNumber();
                }

                public int getLineNumber()
                {
                    return location.getLineNumber();
                }

                public String getPublicId()
                {
                    return location.getPublicId();
                }

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

    private void handlePI(ProcessingInstruction event) throws XMLStreamException
    {
        try {
            this.filter.processingInstruction(event.getTarget(), event.getData());
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
    }

    private void handleCharacters(Characters event) throws XMLStreamException
    {
        try {
            this.filter.characters(event.getData().toCharArray(), 0, event.getData().length());
        } catch (SAXException e) {
            throw new XMLStreamException(e);
        }
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
            for (Iterator i = event.getNamespaces(); i.hasNext();) {
                String nsprefix = ((Namespace) i.next()).getPrefix();
                if (nsprefix == null) { // true for default namespace
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
            for (Iterator i = event.getNamespaces(); i.hasNext();) {
                String prefix = ((Namespace) i.next()).getPrefix();
                if (prefix == null) { // true for default namespace
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
            for (Iterator i = event.getNamespaces(); i.hasNext();) {
                Namespace staxNamespace = (javax.xml.stream.events.Namespace) i.next();
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
        for (Iterator i = event.getAttributes(); i.hasNext();) {
            Attribute staxAttr = (javax.xml.stream.events.Attribute) i.next();

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

    private void handleNamespace()
    {
        // no-op ???
        // namespace events don't normally occur outside of a startElement
        // or endElement
    }

    private void handleAttribute()
    {
        // no-op ???
        // attribute events don't normally occur outside of a startElement
        // or endElement
    }

    private void handleDTD()
    {
        // no-op ???
        // it seems like we need to pass this info along, but how?
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

    private void handleEntityReference()
    {
        // no-op ???
    }

    private void handleSpace()
    {
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    private void handleNotationDecl()
    {
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    private void handleEntityDecl()
    {
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }

    private void handleCDATA()
    {
        // no-op ???
        // this event is listed in the javadoc, but not in the spec.
    }
}
