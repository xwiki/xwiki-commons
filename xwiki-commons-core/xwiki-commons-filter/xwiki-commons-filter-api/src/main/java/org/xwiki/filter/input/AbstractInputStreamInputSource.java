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
package org.xwiki.filter.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @version $Id$
 * @since 6.2M1
 */
public abstract class AbstractInputStreamInputSource implements InputStreamInputSource
{
    protected InputStream inputStream;

    @Override
    public boolean restartSupported()
    {
        return true;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        if (this.inputStream == null) {
            this.inputStream = new FilterInputStream(openStream()) {
                @Override
                public void close() throws IOException
                {
                    super.close();

                    // Since the stream was closed, we need the InputStreamInputSourceto know about it
                    AbstractInputStreamInputSource.this.inputStream = null;
                }
            };
        }

        return this.inputStream;
    }

    protected abstract InputStream openStream() throws IOException;

    @Override
    public void close() throws IOException
    {
        if (this.inputStream != null) {
            this.inputStream.close();
        }
        this.inputStream = null;
    }

    @Override
    public String toString()
    {
        return this.inputStream != null ? this.inputStream.toString() : super.toString();
    }
}
