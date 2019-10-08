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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Make sure to serialize on {@link Throwable} fields skip any custom field that might make XStream try to serialize the
 * world.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
public class SafeThrowableConverter implements Converter
{
    private final Mapper mapper;

    private final Converter objectConverter;

    /**
     * @param mapper the mapper used to convert other values
     * @param objectConverter the {@link Converter} for {@link Object} type
     */
    public SafeThrowableConverter(Mapper mapper, Converter objectConverter)
    {
        this.mapper = mapper;
        this.objectConverter = objectConverter;
    }

    @Override
    public boolean canConvert(final Class type)
    {
        return Throwable.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        Throwable throwable = (Throwable) source;

        // Message
        XStreamUtils.serializeField("detailMessage", String.class, throwable.getMessage(), writer, context,
            this.mapper);

        // Cause
        XStreamUtils.serializeField("cause", Throwable.class, throwable.getCause(), writer, context, this.mapper);

        // Stack trace
        XStreamUtils.serializeField("stackTrace", StackTraceElement[].class, throwable.getStackTrace(), writer, context,
            this.mapper);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        return this.objectConverter.unmarshal(reader, context);
    }
}
