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

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.ReferenceByXPathMarshaller;
import com.thoughtworks.xstream.core.util.ObjectIdDictionary;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * A {@link ReferenceByXPathMarshaller} which never fail whatever value is provided.
 *
 * @version $Id$
 */
public class SafeTreeMarshaller extends ReferenceByXPathMarshaller
{
    /**
     * @see ReferenceByXPathMarshaller#ReferenceByXPathMarshaller(HierarchicalStreamWriter, ConverterLookup, Mapper,
     *      int)
     * @param writer the writer
     * @param converterLookup the converter lookup
     * @param mapper the mapper
     * @param mode the marshalling mode
     */
    public SafeTreeMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper, int mode)
    {
        super(writer, converterLookup, mapper, mode);
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
    public void convert(Object item, Converter converter)
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
}
