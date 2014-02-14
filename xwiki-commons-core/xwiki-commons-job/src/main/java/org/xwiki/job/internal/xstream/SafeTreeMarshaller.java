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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.core.util.ObjectIdDictionary;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * A {@link TreeMarshaller} which never fail whatever value is provided.
 * 
 * @version $Id$
 */
public class SafeTreeMarshaller extends TreeMarshaller
{
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeTreeMarshaller.class);

    private XStream xstream;

    /**
     * @see TreeMarshaller#TreeMarshaller(HierarchicalStreamWriter, ConverterLookup,
     *      com.thoughtworks.xstream.mapper.Mapper)
     * @param writer the writer
     * @param converterLookup the converter lookup
     * @param mapper the mapper
     * @param xstream the {@link XStream} instance to use to isolate array element marshaling
     */
    public SafeTreeMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper,
        XStream xstream)
    {
        super(writer, converterLookup, mapper);

        this.xstream = xstream;
    }

    private ObjectIdDictionary getParentObjects()
    {
        ObjectIdDictionary parentObjects = (ObjectIdDictionary) get(ObjectIdDictionary.class);

        if (parentObjects == null) {
            parentObjects = new ObjectIdDictionary();
            put(ObjectIdDictionary.class, parentObjects);
        }

        return parentObjects;
    }

    @Override
    protected void convert(Object item, Converter converter)
    {
        ObjectIdDictionary parentObjects = getParentObjects();

        if (parentObjects.containsId(item)) {
            ConversionException e = new CircularReferenceException("Recursive reference to parent object");
            e.add("item-type", item.getClass().getName());
            e.add("converter-type", converter.getClass().getName());
            throw e;
        }

        parentObjects.associateId(item, "");
        try {
            super.convert(item, converter);
        } finally {
            parentObjects.removeId(item);
        }
    }

    /*
    @Override
    protected void convert(Object item, Converter converter)
    {
        if (!XStreamUtils.isSafeType(item)) {
            // Test the serialization
            try {
                this.xstream.marshal(item, VoidWriter.WRITER);
            } catch (Throwable e) {
                LOGGER.debug("Failed to write field. Ingoring it.", e);

                // If anything goes wrong ignore it
                return;
            }
        }

        super.convert(item, converter);
    }
    */
}
