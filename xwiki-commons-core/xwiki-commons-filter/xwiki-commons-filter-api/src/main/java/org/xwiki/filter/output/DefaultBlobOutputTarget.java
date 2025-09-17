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
package org.xwiki.filter.output;

import java.io.IOException;
import java.io.OutputStream;

import org.xwiki.stability.Unstable;
import org.xwiki.store.blob.Blob;

/**
 * An implementation of {@link OutputStreamOutputTarget} that writes to a {@link Blob}.
 *
 * @version $Id$
 * @since 17.8.0RC1
 */
@Unstable
public class DefaultBlobOutputTarget extends AbstractOutputStreamOutputTarget
{
    private final Blob blob;

    /**
     * Create an instance of {@link OutputStreamOutputTarget} returning the passed {@link Blob}'s output stream.
     *
     * @param blob the {@link Blob}
     */
    public DefaultBlobOutputTarget(Blob blob)
    {
        this.blob = blob;
    }

    @Override
    protected OutputStream openStream() throws IOException
    {
        try {
            return this.blob.getOutputStream();
        } catch (Exception e) {
            throw new IOException("Failed to open blob output stream", e);
        }
    }
}
