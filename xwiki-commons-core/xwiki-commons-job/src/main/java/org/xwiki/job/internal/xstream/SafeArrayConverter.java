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

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.ArrayConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
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
    private Logger logger;

    /**
     * @param mapper the XStream mapper
     * @param logger the logger
     */
    public SafeArrayConverter(Mapper mapper, Logger logger)
    {
        super(mapper);

        this.logger = logger;
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
        } catch (Exception e) {
            this.logger.debug("Failed to read field", e);

            value = null;
        }

        return value;
    }
}
