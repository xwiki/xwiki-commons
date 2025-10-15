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
package org.xwiki.store.blob.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.WriteCondition;

/**
 * Abstract base class for {@link Blob} implementations.
 *
 * @version $Id$
 * @since 17.9.0RC1
 */
public abstract class AbstractBlob implements Blob
{
    protected final BlobStore blobStore;

    protected final BlobPath blobPath;

    protected AbstractBlob(BlobStore store, BlobPath blobPath)
    {
        this.blobStore = store;
        this.blobPath = blobPath;
    }

    @Override
    public BlobStore getStore()
    {
        return this.blobStore;
    }

    @Override
    public BlobPath getPath()
    {
        return this.blobPath;
    }

    @Override
    public void writeFromStream(InputStream inputStream, WriteCondition... condition) throws BlobStoreException
    {
        try (OutputStream outputStream = this.getOutputStream(condition)) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            if (e.getCause() instanceof BlobStoreException blobStoreException) {
                throw blobStoreException;
            }

            throw new BlobStoreException("Error writing from InputStream to blob.", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
}
