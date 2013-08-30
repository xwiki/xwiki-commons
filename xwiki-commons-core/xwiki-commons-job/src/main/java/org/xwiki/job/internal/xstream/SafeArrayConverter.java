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

import java.util.List;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Role;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * Make sure to unzerialize as much as possible from the job status without failing.
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

    /**
     * @param mapper the XStream mapper
     */
    public SafeArrayConverter(Mapper mapper)
    {
        super(mapper);
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
        try {
            super.writeItem(isComponent(item) ? item.toString() : item, context, writer);
        } catch (Throwable e) {
            LOGGER.debug("Failed to write field", e);
        }
    }

    /**
     * @param item the item to serialize
     * @return true of the item looks like a component
     */
    private boolean isComponent(Object item)
    {
        if (item != null) {
            List<Class< ? >> interfaces = ClassUtils.getAllInterfaces(item.getClass());

            for (Class< ? > iface : interfaces) {
                if (iface.isAnnotationPresent(Role.class)) {
                    return true;
                }
            }
        }

        return false;
    }
}
