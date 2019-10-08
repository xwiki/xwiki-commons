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
package org.xwiki.xstream.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.ReferenceByXPathMarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * A {@link ReferenceByXPathMarshaller} which never fail whatever value is provided.
 *
 * @version $Id$
 * @since 11.9RC1
 */
public class SafeTreeMarshaller extends ReferenceByXPathMarshaller
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeTreeMarshaller.class);

    protected SafeWriter safeWriter;

    private final XStreamUtils utils;

    /**
     * @see ReferenceByXPathMarshaller#ReferenceByXPathMarshaller(HierarchicalStreamWriter, ConverterLookup, Mapper,
     *      int)
     * @param writer the writer
     * @param converterLookup the converter lookup
     * @param mapper the mapper
     * @param mode the marshalling mode
     * @param utils the utils
     */
    public SafeTreeMarshaller(HierarchicalStreamWriter writer, ConverterLookup converterLookup, Mapper mapper, int mode,
        XStreamUtils utils)
    {
        super(writer, converterLookup, mapper, mode);

        this.utils = utils;
    }

    @Override
    public void convert(Object item, Converter converter)
    {
        if (this.utils.isSerializable(item)) {
            HierarchicalStreamWriter currentWriter = this.writer;

            try {
                this.safeWriter = new SafeWriter(this.writer);
                this.writer = this.safeWriter;

                super.convert(item, converter);
            } catch (Throwable e) {
                LOGGER.debug("Failed to serialize item [{}]",
                    item.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(item)), e);

                // Make sure to close any forgotten end tag
                this.safeWriter.fix();
            } finally {
                this.writer = currentWriter;
            }
        }
    }
}
