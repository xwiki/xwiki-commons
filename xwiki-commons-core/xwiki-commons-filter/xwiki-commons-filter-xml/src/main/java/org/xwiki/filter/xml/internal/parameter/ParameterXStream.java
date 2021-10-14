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
package org.xwiki.filter.xml.internal.parameter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.filter.FilterEventParameters;
import org.xwiki.filter.input.InputSource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;

/**
 * A special {@link XStream} instance dedicated to serializing filter parameters.
 * 
 * @version $Id$
 * @since 13.9RC1
 */
public class ParameterXStream extends NoWarningXStream
{
    /**
     * Constructs an XStream with a special {@link HierarchicalStreamDriver}.
     * <p>
     * The instance will tries to determine the best match for the {@link ReflectionProvider} on its own.
     * </p>
     *
     * @param hierarchicalStreamDriver the driver instance
     */
    public ParameterXStream(HierarchicalStreamDriver hierarchicalStreamDriver)
    {
        super(hierarchicalStreamDriver);

        this.setMarshallingStrategy(new XMLTreeMarshallingStrategy());

        this.addDefaultImplementation(LinkedHashMap.class, Map.class);
        this.addDefaultImplementation(ArrayList.class, Collection.class);
        this.addDefaultImplementation(ArrayList.class, List.class);

        this.registerConverter(new XMLFilterElementParametersConverter(this.getMapper()));
        this.registerConverter(new InputStreamConverter());
        this.registerConverter(new InputSourceConverter());

        // XStream does not register converter for their associated interface
        this.registerConverter(new MapConverter(this.getMapper()));
        this.registerConverter(new CollectionConverter(this.getMapper()));
        this.registerConverter(new ListConverter(this.getMapper()));

        this.alias("parameters", FilterEventParameters.class);
        this.alias("map", LinkedHashMap.class);
        this.alias("input-stream", InputStream.class);
        this.alias("input-source", InputSource.class);
    }
}
