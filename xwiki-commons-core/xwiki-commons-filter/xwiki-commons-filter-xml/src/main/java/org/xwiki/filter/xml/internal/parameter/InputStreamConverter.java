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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.ArrayUtils;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Converts an {@link InputStream} to XML.
 *
 * @version $Id$
 * @since 5.2RC1
 */
public class InputStreamConverter implements Converter
{
    private static final Base64Encoder BASE64 = new Base64Encoder();

    @Override
    public boolean canConvert(Class type)
    {
        return InputStream.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        write((InputStream) source, writer);
    }

    /**
     * @param inpuStream the stream to write
     * @param writer the writer in which to serialize the stream
     * @since 12.0RC1
     */
    protected void write(InputStream inpuStream, HierarchicalStreamWriter writer)
    {
        byte[] buffer = new byte[4096];
        int readSize;
        do {
            try {
                readSize = inpuStream.read(buffer);
            } catch (IOException e) {
                throw new ConversionException("Failed to read input stream", e);
            }
            if (readSize > 0) {
                if (readSize == 4096) {
                    writer.setValue(BASE64.encode(buffer));
                } else {
                    writer.setValue(BASE64.encode(ArrayUtils.subarray(buffer, 0, readSize)));
                }
            }
        } while (readSize == 4096);
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        return read(reader);
    }

    /**
     * @param reader the reader used to access the data
     * @return the new {@link InputStream} instance
     */
    protected InputStream read(HierarchicalStreamReader reader)
    {
        return new ByteArrayInputStream(BASE64.decode(reader.getValue()));
    }
}
