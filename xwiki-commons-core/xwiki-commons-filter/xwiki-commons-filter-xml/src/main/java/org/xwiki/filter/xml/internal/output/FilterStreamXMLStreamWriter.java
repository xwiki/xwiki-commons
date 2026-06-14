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
package org.xwiki.filter.xml.internal.output;

import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xwiki.filter.FilterException;
import org.xwiki.filter.xml.output.XMLOutputProperties;

import javanet.staxutils.IndentingXMLStreamWriter;

/**
 * @version $Id$
 * @since 6.2M1
 */
public class FilterStreamXMLStreamWriter
{
    private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

    private static final String FAILED_CREATE_WRITER = "Failed to create XML writer";

    private static final String FAILED_START_DOCUMENT = "Failed to write start document";

    private final XMLStreamWriter writer;

    private final boolean printNullValue;

    /**
     * @param writer the wrapped XML writer
     * @param printNullValue true if null values should be written as empty elements
     */
    public FilterStreamXMLStreamWriter(XMLStreamWriter writer, boolean printNullValue)
    {
        this.writer = writer;
        this.printNullValue = printNullValue;
    }

    /**
     * @param outputStream the stream to write to
     * @param encoding the encoding to use to write
     * @param format true if the output should be indented
     * @param printNullValue true if null values should be written as empty elements
     * @throws FilterException when failing to create the writer
     */
    public FilterStreamXMLStreamWriter(OutputStream outputStream, String encoding, boolean format,
        boolean printNullValue) throws FilterException
    {
        try {
            XMLStreamWriter streamWriter = XML_OUTPUT_FACTORY.createXMLStreamWriter(outputStream, encoding);

            if (format) {
                this.writer = new IndentingXMLStreamWriter(streamWriter);
            } else {
                this.writer = streamWriter;
            }
        } catch (Exception e) {
            throw new FilterException(FAILED_CREATE_WRITER, e);
        }

        this.printNullValue = printNullValue;
    }

    /**
     * @param properties the properties containing the target to write to
     * @param printNullValue true if null values should be written as empty elements
     * @throws FilterException when failing to create the writer
     */
    public FilterStreamXMLStreamWriter(XMLOutputProperties properties, boolean printNullValue) throws FilterException
    {
        try {
            this.writer = XMLOutputFilterStreamUtils.createXMLStreamWriter(properties);
        } catch (Exception e) {
            throw new FilterException(FAILED_CREATE_WRITER, e);
        }

        this.printNullValue = printNullValue;
    }

    /**
     * @return the wrapped XML writer
     */
    public XMLStreamWriter getWriter()
    {
        return this.writer;
    }

    //

    /**
     * Write the XML Declaration. Defaults the XML version to 1.0, and the encoding to utf-8.
     *
     * @throws FilterException
     */
    public void writeStartDocument() throws FilterException
    {
        try {
            this.writer.writeStartDocument();
        } catch (XMLStreamException e) {
            throw new FilterException(FAILED_START_DOCUMENT, e);
        }
    }

    /**
     * Write the XML Declaration.
     *
     * @param encoding the XML version
     * @param version the XML encoding
     * @throws FilterException
     */
    public void writeStartDocument(String encoding, String version) throws FilterException
    {
        try {
            this.writer.writeStartDocument(encoding, version);
        } catch (XMLStreamException e) {
            throw new FilterException(FAILED_START_DOCUMENT, e);
        }
    }

    /**
     * Closes any start tags and writes corresponding end tags.
     *
     * @throws FilterException
     */
    public void writeEndDocument() throws FilterException
    {
        try {
            this.writer.writeEndDocument();
        } catch (XMLStreamException e) {
            throw new FilterException("Failed to write end document", e);
        }
    }

    /**
     * @param localName the local name of the empty element to write
     * @throws FilterException when failing to write the element
     */
    public void writeEmptyElement(String localName) throws FilterException
    {
        try {
            this.writer.writeEmptyElement(localName);
        } catch (XMLStreamException e) {
            throw new FilterException(String.format("Failed to write empty element [%s]", localName), e);
        }
    }

    /**
     * @param localName the local name of the element to write
     * @param value the text value of the element
     * @throws FilterException when failing to write the element
     */
    public void writeElement(String localName, String value) throws FilterException
    {
        if (value != null) {
            if (value.isEmpty()) {
                writeEmptyElement(localName);
            } else {
                writeStartElement(localName);
                writeCharacters(value);
                writeEndElement();
            }
        } else if (this.printNullValue) {
            writeEmptyElement(localName);
        }
    }

    /**
     * @param text the text to write
     * @throws FilterException when failing to write the text
     */
    public void writeCharacters(String text) throws FilterException
    {
        try {
            this.writer.writeCharacters(text);
        } catch (XMLStreamException e) {
            throw new FilterException(String.format("Failed to write characters [%s]", text), e);
        }
    }

    /**
     * @param localName the local name of the element to start
     * @throws FilterException when failing to write the element
     */
    public void writeStartElement(String localName) throws FilterException
    {
        try {
            this.writer.writeStartElement(localName);
        } catch (XMLStreamException e) {
            throw new FilterException(String.format("Failed to write start element [%s]", localName), e);
        }
    }

    /**
     * @throws FilterException when failing to write the end element
     */
    public void writeEndElement() throws FilterException
    {
        try {
            this.writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new FilterException("Failed to write end element", e);
        }
    }

    /**
     * @param localName the local name of the attribute to write
     * @param value the value of the attribute (nothing is written when null)
     * @throws FilterException when failing to write the attribute
     */
    public void writeAttribute(String localName, String value) throws FilterException
    {
        if (value != null) {
            try {
                this.writer.writeAttribute(localName, value);
            } catch (XMLStreamException e) {
                throw new FilterException(
                    String.format("Failed to write attribute [%s] with value [%s]", localName, value), e);
            }
        }
    }

    /**
     * @param text the characters to write
     * @param start the index of the first character to write
     * @param len the number of characters to write
     * @throws FilterException when failing to write the characters
     */
    public void writeCharacters(char[] text, int start, int len) throws FilterException
    {
        try {
            this.writer.writeCharacters(text, start, len);
        } catch (XMLStreamException e) {
            throw new FilterException("Failed to write characters", e);
        }
    }

    /**
     * Close this writer and free any resources associated with the writer. This must not close the underlying output
     * stream.
     *
     * @throws FilterException
     */
    public void close() throws FilterException
    {
        try {
            this.writer.close();
        } catch (XMLStreamException e) {
            throw new FilterException("Failed to close writer", e);
        }
    }

    /**
     * Write any cached data to the underlying output mechanism.
     *
     * @throws FilterException
     */
    public void flush() throws FilterException
    {
        try {
            this.writer.flush();
        } catch (XMLStreamException e) {
            throw new FilterException("Failed to flush writer", e);
        }
    }
}
