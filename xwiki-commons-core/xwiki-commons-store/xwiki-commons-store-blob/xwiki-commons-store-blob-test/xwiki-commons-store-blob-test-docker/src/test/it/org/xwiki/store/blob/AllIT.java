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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xwiki.test.junit5.mockito.ComponentTest;

/**
 * All integration tests for the Blob Store feature.
 *
 * @version $Id$
 */
@ComponentTest
@ExtendWith(BlobStoreExtension.class)
public class AllIT
{
    @Nested
    class NestedFileSystemBlobStoreIT extends FileSystemBlobStoreIT
    {
    }

    @Nested
    class NestedS3BlobStoreIT extends S3BlobStoreIT
    {
    }

    @Nested
    class NestedCrossStoreBlobStoreIT extends CrossStoreBlobStoreIT
    {
    }
}
