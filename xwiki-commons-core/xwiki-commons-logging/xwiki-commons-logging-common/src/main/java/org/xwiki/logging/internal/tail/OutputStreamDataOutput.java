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

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Convert an {@link OutputStream} instead a {@link DataOutput}.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
public class OutputStreamDataOutput extends OutputStream
{
    private final DataOutput output;

    /**
     * @param output the output to write to
     */
    public OutputStreamDataOutput(DataOutput output)
    {
        this.output = output;
    }

    @Override
    public void write(int b) throws IOException
    {
        this.output.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        this.output.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        this.output.write(b, off, len);
    }
}
