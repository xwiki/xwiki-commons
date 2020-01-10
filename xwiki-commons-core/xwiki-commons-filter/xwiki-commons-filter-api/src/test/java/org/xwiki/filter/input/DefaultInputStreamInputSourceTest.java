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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validate {@link DefaultInputStreamInputSource}.
 * 
 * @version $Id$
 */
public class DefaultInputStreamInputSourceTest
{
    private static class TestInputStream extends InputStream
    {
        ByteArrayInputStream stream;

        TestInputStream(byte[] array)
        {
            this.stream = new ByteArrayInputStream(array);
        }

        @Override
        public int read() throws IOException
        {
            if (this.stream == null) {
                throw new IOException("Stream closed");
            }

            return this.stream.read();
        }

        @Override
        public void close() throws IOException
        {
            this.stream = null;
        }

        boolean isClosed()
        {
            return this.stream == null;
        }
    }

    private TestInputStream stream = new TestInputStream(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

    @Test
    void closeEnabled() throws IOException
    {
        DefaultInputStreamInputSource source = new DefaultInputStreamInputSource(this.stream, true);

        assertFalse(source.restartSupported());

        assertEquals(1, source.getInputStream().read());
        assertEquals(2, source.getInputStream().read());

        source.close();

        assertThrows(IOException.class, () -> source.getInputStream().read(), "Stream closed");
    }

    @Test
    void closeDisabled() throws IOException
    {
        DefaultInputStreamInputSource source = new DefaultInputStreamInputSource(this.stream);

        assertFalse(source.restartSupported());

        assertEquals(1, source.getInputStream().read());
        assertEquals(2, source.getInputStream().read());

        source.close();

        assertEquals(3, source.getInputStream().read());

        source.getInputStream().close();

        assertEquals(4, source.getInputStream().read());
    }
}
