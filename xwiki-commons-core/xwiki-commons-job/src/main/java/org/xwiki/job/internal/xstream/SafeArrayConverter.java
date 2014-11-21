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
package org.xwiki.job.internal.xstream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;
import com.thoughtworks.xstream.io.xml.DomReader;
import com.thoughtworks.xstream.io.xml.DomWriter;

/**
 * A {@link ArrayConverter} which never fail whatever value is provided.
 *
 * @version $Id$
 * @since 4.3M1
 */
public class SafeArrayConverter extends ArrayConverter
{
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeArrayConverter.class);

    private final DocumentBuilderFactory docFactory;

    private DocumentBuilder docBuilder;

    private final HierarchicalStreamCopier copier;

    private SafeXStream xstream;

    /**
     * @param xstream the {@link com.thoughtworks.xstream.XStream} instance to use to isolate array element marshaling
     */
    public SafeArrayConverter(SafeXStream xstream)
    {
        super(xstream.getMapper());

        this.docFactory = DocumentBuilderFactory.newInstance();
        try {
            this.docBuilder = this.docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // Should never happen
            LOGGER.error("Failed to create a DocumentBuilder");
        }
        this.copier = new HierarchicalStreamCopier();
        this.xstream = xstream;
    }

    @Override
    public boolean canConvert(Class type)
    {
        return type == Object[].class;
    }

    @Override
    protected Object readItem(HierarchicalStreamReader reader, UnmarshallingContext context, Object current)
    {
        Object value;
        try {
            value = super.readItem(reader, context, current);
        } catch (Throwable e) {
            LOGGER.debug("Failed to read field", e);

            value = null;
        }

        return value;
    }

    @Override
    protected void writeItem(Object item, MarshallingContext context, HierarchicalStreamWriter writer)
    {
        if (XStreamUtils.isSafeType(item) || this.docBuilder == null) {
            super.writeItem(item, context, writer);
        } else if (!XStreamUtils.isSerializable(item)) {
            super.writeItem(item.toString(), context, writer);
        } else {
            try {
                Document doc = this.docBuilder.newDocument();

                DomWriter domWriter = new DomWriter(doc);

                this.xstream.marshal(item, domWriter, new DataHolderWrapper(context));

                DomReader domReader = new DomReader(doc);

                this.copier.copy(domReader, writer);
            } catch (Throwable e) {
                LOGGER.debug("Failed to write field", e);

                super.writeItem(item.toString(), context, writer);
            }
        }
    }
}
