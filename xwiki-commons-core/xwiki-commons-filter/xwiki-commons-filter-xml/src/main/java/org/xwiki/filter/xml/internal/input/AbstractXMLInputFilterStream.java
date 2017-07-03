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
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;

import org.xwiki.filter.FilterException;
import org.xwiki.filter.input.InputFilterStream;
import org.xwiki.filter.xml.input.XMLInputProperties;

import javanet.staxutils.XMLStreamUtils;

/**
 * @param <P>
 * @version $Id$
 * @since 6.2M1
 */
public abstract class AbstractXMLInputFilterStream<P extends XMLInputProperties> implements InputFilterStream
{
    protected P parameters;

    protected XMLInputFactory xmlFactory;

    /**
     * @since 9.5
     * @since 9.6RC1
     */
    public AbstractXMLInputFilterStream(P parameters, XMLInputFactory xmlFactory)
    {
        this.parameters = parameters;
        this.xmlFactory = xmlFactory;
    }

    @Override
    public void read(Object listener) throws FilterException
    {
        try {
            XMLEventReader xmlEventReader =
                XMLInputFilterStreamUtils.createXMLEventReader(this.xmlFactory, this.parameters);

            XMLStreamUtils.copy(xmlEventReader, createXMLEventWriter(listener, this.parameters));
        } catch (Exception e) {
            throw new FilterException("Faild to parse XML source", e);
        }
    }

    protected abstract XMLEventWriter createXMLEventWriter(Object listener, P parameters);

    @Override
    public void close() throws IOException
    {
        this.parameters.getSource().close();
    }
}
