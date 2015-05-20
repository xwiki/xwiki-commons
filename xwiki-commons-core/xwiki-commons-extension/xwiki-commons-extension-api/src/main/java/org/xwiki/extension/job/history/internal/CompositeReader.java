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
package org.xwiki.extension.job.history.internal;

import java.io.IOException;
import java.io.Reader;

/**
 * A composite {@link Reader} (because there is {@code SequenceInputStream} but not {@code SequenceReader}).
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public class CompositeReader extends Reader
{
    private final Reader[] readers;

    private int index;

    /**
     * Creates a new composite reader that includes the given readers.
     * 
     * @param readers the readers to include
     */
    public CompositeReader(Reader... readers)
    {
        this.readers = readers;
    }

    @Override
    public void close() throws IOException
    {
        for (Reader reader : this.readers) {
            reader.close();
        }
    }

    @Override
    public int read(char[] buffer, int offset, int length) throws IOException
    {
        if (this.index >= this.readers.length) {
            // No more readers.
            return -1;
        }

        int readCount = this.readers[this.index].read(buffer, offset, length);
        if (readCount < 0) {
            // End of the current reader. Move to the next reader.
            this.index++;
            return read(buffer, offset, length);
        }

        return readCount;
    }
}
