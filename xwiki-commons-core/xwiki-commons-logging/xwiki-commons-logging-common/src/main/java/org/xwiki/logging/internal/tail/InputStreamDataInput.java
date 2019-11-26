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
package org.xwiki.logging.internal.tail;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Convert an {@link OutputStream} instead a {@link DataOutput}.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
public class InputStreamDataInput extends InputStream
{
    private final DataInput input;

    /**
     * @param input the input to read from
     */
    public InputStreamDataInput(DataInput input)
    {
        this.input = input;
    }

    @Override
    public void close() throws IOException
    {
        // Nothing to do
    }

    @Override
    public int read() throws IOException
    {
        return this.input.readByte() & 0xFF;
    }
}
