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

import java.io.IOException;
import java.io.InputStream;

import org.xwiki.stability.Unstable;
import org.xwiki.store.StreamProvider;

/**
 * An input source based on a {@link StreamProvider}.
 *
 * @version $Id$
 * @since 17.8.0RC1
 */
@Unstable
public class DefaultStreamProviderInputSource extends AbstractInputStreamInputSource
{
    private final StreamProvider streamProvider;

    /**
     * Create a new input source based on the passed stream provider.
     *
     * @param streamProvider the stream provider to use
     */
    public DefaultStreamProviderInputSource(StreamProvider streamProvider)
    {
        this.streamProvider = streamProvider;
    }

    @Override
    protected InputStream openStream() throws IOException
    {
        try {
            return this.streamProvider.getStream();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to open stream", e);
        }
    }

}
