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

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A {@link ArrayConverter} which never fail whatever value is provided.
 *
 * @version $Id$
 * @since 11.9RC1
 */
public class SafeArrayConverter extends ArrayConverter
{
    /**
     * The logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SafeArrayConverter.class);

    private final XStreamUtils utils;

    /**
     * @param xstream the {@link com.thoughtworks.xstream.XStream} instance to use to isolate array element marshaling
     */
    public SafeArrayConverter(SafeXStream xstream)
    {
        super(xstream.getMapper());

        this.utils = xstream.getUtils();
    }

    @Override
    public boolean canConvert(Class type)
    {
        return type == Object[].class;
    }

    @Override
    protected Object readBareItem(HierarchicalStreamReader reader, UnmarshallingContext context, Object current)
    {
        Object value;
        try {
            value = super.readBareItem(reader, context, current);
        } catch (Throwable e) {
            LOGGER.debug("Failed to read field", e);

            value = null;
        }

        return value;
    }

    @Override
    protected void writeCompleteItem(Object item, MarshallingContext context, HierarchicalStreamWriter writer)
    {
        if (this.utils.isSerializable(item)) {
            super.writeCompleteItem(item, context, writer);
        } else {
            String str;
            try {
                str = item.toString();
            } catch (Throwable e) {
                LOGGER.debug("Failed to convert item [{}] to String",
                    item.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(item)), e);

                str = null;
            }

            super.writeCompleteItem(str, context, writer);
        }
    }
}
