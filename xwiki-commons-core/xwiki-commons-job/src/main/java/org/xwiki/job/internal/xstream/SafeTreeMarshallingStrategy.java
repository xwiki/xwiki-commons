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

import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.core.ReferenceByXPathMarshallingStrategy;
import com.thoughtworks.xstream.core.TreeMarshaller;
import com.thoughtworks.xstream.core.TreeUnmarshaller;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * A {@link ReferenceByXPathMarshallingStrategy} which never fail whatever value is provided.
 *
 * @version $Id$
 * @since 5.4M1
 */
public class SafeTreeMarshallingStrategy extends ReferenceByXPathMarshallingStrategy
{
    private SafeXStream xstream;

    /**
     * @param xstream the {@link com.thoughtworks.xstream.XStream} instance to use to isolate array element marshaling
     */
    public SafeTreeMarshallingStrategy(SafeXStream xstream)
    {
        super(RELATIVE);

        this.xstream = xstream;
    }

    // If anything goes wrong with an element, replace it with null
    @Override
    protected TreeUnmarshaller createUnmarshallingContext(Object root, HierarchicalStreamReader reader,
        ConverterLookup converterLookup, Mapper mapper)
    {
        return new SafeTreeUnmarshaller(root, reader, converterLookup, mapper);
    }

    // If anything goes wrong with an element, don't serialize it
    @Override
    protected TreeMarshaller createMarshallingContext(HierarchicalStreamWriter writer, ConverterLookup converterLookup,
        Mapper mapper)
    {
        return new SafeTreeMarshaller(writer, converterLookup, mapper, RELATIVE, this.xstream);
    }
}
