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

import com.thoughtworks.xstream.MarshallingStrategy;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.core.JVM;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

/**
 * A {@link XStream} that never fail whatever value is provided.
 *
 * @version $Id$
 * @since 5.4M1
 */
public class SafeXStream extends XStream
{
    // FIXME: Workaround for XStream security rules warning
    private MarshallingStrategy marshallingStrategy;

    /**
     * Default constructor.
     */
    public SafeXStream()
    {
        super(new SafeReflectionProvider(JVM.newReflectionProvider()));

        // Cleaner array serialization
        registerConverter(new SafeArrayConverter(this));

        // Cleaner messages
        registerConverter(new SafeMessageConverter(this));

        // Cleaner log
        registerConverter(new SafeLogEventConverter(this));

        // cleaner exceptions
        registerConverter(
            new SafeThrowableConverter(getMapper(), getConverterLookup().lookupConverterForType(Object.class)));

        // We don't care if some field from the XML does not exist anymore
        ignoreUnknownElements();

        // Protect reflection based marshalling/unmarshalling
        setMarshallingStrategy(new SafeTreeMarshallingStrategy());

        // TODO: see what to do about new XStream security rules, the default setup is to use a white list which is
        // totally unusable for job serialization use case where we don't know the types in advance (we don't even know
        // the ClassLoader in advance...).
        // setupDefaultSecurity(this);
    }

    ////////////////////////////////////////////////////////////////////
    // FIXME: Workaround for XStream security rules warning

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
            return marshallingStrategy.unmarshal(root, reader, dataHolder, getConverterLookup(), getMapper());

        } catch (ConversionException e) {
            Package pkg = getClass().getPackage();
            String version = pkg != null ? pkg.getImplementationVersion() : null;
            e.add("version", version != null ? version : "not available");
            throw e;
        }
    }
}
