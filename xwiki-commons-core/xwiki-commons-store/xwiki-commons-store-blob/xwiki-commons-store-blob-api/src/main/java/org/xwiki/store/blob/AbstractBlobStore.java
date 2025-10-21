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
package org.xwiki.store.blob;

import java.util.List;
import java.util.stream.Stream;

import org.xwiki.stability.Unstable;

/**
 * Abstract base class for {@link BlobStore} implementations.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Unstable
public abstract class AbstractBlobStore implements BlobStore
{
    protected String name;

    /**
     * Create a new blob store with the given name.
     *
     * @param name the name of this blob store
     */
    protected AbstractBlobStore(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public void moveDirectory(BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        moveDirectory(this, sourcePath, targetPath);
    }

    @Override
    public void moveDirectory(BlobStore sourceStore, BlobPath sourcePath, BlobPath targetPath) throws BlobStoreException
    {
        if (sourceStore.equals(this)) {
            if (sourcePath.isAncestorOfOrEquals(targetPath)) {
                throw new BlobStoreException("Cannot move a directory to inside itself");
            } else if (targetPath.isAncestorOfOrEquals(sourcePath)) {
                throw new BlobStoreException("Cannot move a directory to one of its ancestors");
            }
        }
        try (Stream<Blob> blobs = sourceStore.listBlobs(sourcePath)) {
            int numSourceSegments = sourcePath.getSegments().size();
            for (Blob blob : (Iterable<Blob>) blobs::iterator) {
                List<String> sourceSegments = blob.getPath().getSegments();
                List<String> relativeSourceSegments = sourceSegments.subList(numSourceSegments, sourceSegments.size());
                BlobPath resolvedTargetPath = targetPath.resolve(relativeSourceSegments.toArray(new String[0]));
                if (sourceStore == this) {
                    moveBlob(blob.getPath(), resolvedTargetPath);
                } else {
                    moveBlob(sourceStore, blob.getPath(), resolvedTargetPath);
                }
            }
        }
    }
}
