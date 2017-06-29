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

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * FIXME: Workaround for XStream security rules warning.
 *
 * @version $Id$
 * @since 9.5.2
 * @since 9.6RC1
 */
public class NoWarningXStream extends XStream
{
    private MarshallingStrategy marshallingStrategy;

    /**
     * Constructs an XStream with a special {@link HierarchicalStreamDriver}.
     * <p>
     * The instance will tries to determine the best match for the {@link ReflectionProvider} on its own.
     * </p>
     *
     * @param hierarchicalStreamDriver the driver instance
     */
    public NoWarningXStream(HierarchicalStreamDriver hierarchicalStreamDriver)
    {
        super(hierarchicalStreamDriver);
    }

    @Override
    public void setMarshallingStrategy(MarshallingStrategy marshallingStrategy)
    {
        super.setMarshallingStrategy(marshallingStrategy);

        this.marshallingStrategy = marshallingStrategy;
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, Object root, DataHolder dataHolder)
    {
        try {
            return this.marshallingStrategy.unmarshal(root, reader, dataHolder, getConverterLookup(), getMapper());

        } catch (ConversionException e) {
            Package pkg = getClass().getPackage();
            String version = pkg != null ? pkg.getImplementationVersion() : null;
            e.add("version", version != null ? version : "not available");
            throw e;
        }
    }
}
