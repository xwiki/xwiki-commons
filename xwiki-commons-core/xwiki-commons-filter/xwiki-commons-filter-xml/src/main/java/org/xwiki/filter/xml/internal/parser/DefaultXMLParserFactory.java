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
package org.xwiki.filter.xml.internal.parser;

import javanet.staxutils.ContentHandlerToXMLEventWriter;
import javanet.staxutils.StAXResult;
import javanet.staxutils.XMLEventStreamWriter;
import javanet.staxutils.XMLStreamUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.util.XMLEventConsumer;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;

import org.xml.sax.ContentHandler;
import org.xwiki.component.annotation.Component;
import org.xwiki.filter.FilterDescriptorManager;
import org.xwiki.filter.xml.XMLConfiguration;
import org.xwiki.filter.xml.internal.parameter.ParameterManager;
import org.xwiki.filter.xml.parser.XMLParserFactory;
import org.xwiki.properties.ConverterManager;
import org.xwiki.xml.stax.SAXEventConsumer;
import org.xwiki.xml.stax.SAXEventWriter;

/**
 * Default implementation of {@link XMLParserFactory}.
 * 
 * @version $Id$
 * @since 5.2M1
 */
@Component
@Singleton
public class DefaultXMLParserFactory implements XMLParserFactory
{
    /**
     * The parameter converter.
     */
    @Inject
    private ParameterManager parameterManager;

    /**
     * The events supported by the listener.
     */
    @Inject
    private FilterDescriptorManager descriptorManager;

    /**
     * Used to convert simple types.
     */
    @Inject
    private ConverterManager converter;

    /**
     * @param filter the filter to send events to
     * @param configuration the configuration of the parser
     * @return the parser as a {@link ContentHandler}.
     */
    private ContentHandler createParser(Object filter, XMLConfiguration configuration)
    {
        return new DefaultXMLParser(filter, this.descriptorManager.getFilterDescriptor(filter.getClass()),
            this.converter, this.parameterManager, configuration);
    }

    /**
     * @param filter the filter to send events to
     * @param configuration the configuration of the parser
     * @return the parser as a {@link XMLEventConsumer}.
     */
    private XMLEventConsumer createXMLEventConsumer(Object filter, XMLConfiguration configuration)
    {
        return new SAXEventConsumer(createParser(filter, configuration));
    }

    /**
     * @param filter the filter to send events to
     * @param configuration the configuration of the parser
     * @return the parser as a {@link XMLEventWriter}.
     */
    private XMLEventWriter createXMLEventWriter(Object filter, XMLConfiguration configuration)
    {
        return new SAXEventWriter(createParser(filter, configuration));
    }

    @Override
    public void parse(Source source, Object filter, XMLConfiguration configuration) throws XMLStreamException
    {
        if (source instanceof StAXSource) {
            // StAXSource is not supported by standard XMLInputFactory
            StAXSource staxSource = (StAXSource) source;
            if (staxSource.getXMLEventReader() != null) {
                XMLStreamUtils.copy(staxSource.getXMLEventReader(), createXMLEventConsumer(filter, configuration));
            } else {
                XMLStreamUtils.copy(staxSource.getXMLStreamReader(),
                    new XMLEventStreamWriter(createXMLEventWriter(filter, configuration)));
            }
        } else {
            XMLStreamUtils.copy(source, createXMLEventWriter(filter, configuration));
        }
    }

    @Override
    public Result createResult(Object filter, XMLConfiguration configuration)
    {
        return new StAXResult(createXMLEventWriter(filter, configuration));
    }

    @Override
    public ContentHandler createContentHandler(Object filter, XMLConfiguration configuration)
    {
        return new ContentHandlerToXMLEventWriter(createXMLEventWriter(filter, configuration));
    }
}
