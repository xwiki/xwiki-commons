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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.ReferenceByXPathUnmarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * A {@link ReferenceByXPathUnmarshaller} which never fail whatever value is provided.
 *
 * @version $Id$
 * @since 5.0M2
 */
public class SafeTreeUnmarshaller extends ReferenceByXPathUnmarshaller
{
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeTreeUnmarshaller.class);

    /**
     * @see TreeUnmarshaller#TreeUnmarshaller(Object, HierarchicalStreamReader, ConverterLookup, Mapper).
     * @param root the root object
     * @param reader the reader
     * @param converterLookup the converter lookup
     * @param mapper the mapper
     */
    public SafeTreeUnmarshaller(Object root, HierarchicalStreamReader reader, ConverterLookup converterLookup,
        Mapper mapper)
    {
        super(root, reader, converterLookup, mapper);
    }

    @Override
    protected Object convert(Object parent, Class type, Converter converter)
    {
        try {
            return super.convert(parent, type, converter);
        } catch (Throwable e) {
            LOGGER.debug("Got unknown exception when converting object of type [{}]", type, e);
        }

        return null;
    }
}
