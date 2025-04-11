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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link AbstractInputStreamInputSource}.
 * 
 * @version $Id$
 */
class AbstractInputStreamInputSourceTest
{
    private AbstractInputStreamInputSource source = new AbstractInputStreamInputSource()
    {
        @Override
        protected InputStream openStream() throws IOException
        {
            return new ByteArrayInputStream("content".getBytes());
        }
    };

    @Test
    void restartSupported()
    {
        assertTrue(this.source.restartSupported());
    }

    @Test
    void closeInputStream() throws IOException
    {
        InputStream stream1 = this.source.getInputStream();

        assertSame(stream1, this.source.getInputStream());

        this.source.close();

        InputStream stream2 = this.source.getInputStream();

        assertNotSame(stream1, stream2);

        stream2.close();

        InputStream stream3 = this.source.getInputStream();

        assertNotSame(stream2, stream3);
    }
}
