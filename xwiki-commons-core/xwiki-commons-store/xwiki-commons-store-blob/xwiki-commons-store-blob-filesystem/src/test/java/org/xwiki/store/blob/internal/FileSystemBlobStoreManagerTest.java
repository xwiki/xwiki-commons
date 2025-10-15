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

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.xwiki.environment.Environment;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FileSystemBlobStoreManager}.
 *
 * @version $Id$
 */
@ComponentTest
class FileSystemBlobStoreManagerTest
{
    @InjectMockComponents
    private FileSystemBlobStoreManager blobStoreManager;

    @MockComponent
    private Environment environment;

    @ParameterizedTest
    @CsvSource({
        "testStore, /tmp/xwiki/testStore",
        "test-store_with.special/chars, /tmp/xwiki/test-store_with.special/chars"
    })
    void getBlobStore(String storeName, String expectedPath) throws Exception
    {
        File permanentDirectory = new File("/tmp/xwiki");
        when(this.environment.getPermanentDirectory()).thenReturn(permanentDirectory);

        BlobStore blobStore = this.blobStoreManager.getBlobStore(storeName);

        assertNotNull(blobStore);
        assertEquals(storeName, blobStore.getName());
        assertInstanceOf(FileSystemBlobStore.class, blobStore);
        assertEquals(Path.of(expectedPath),
            ((FileSystemBlobStore) blobStore).getBlobFilePath(BlobPath.ROOT));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void getBlobStoreWithNullName(String name)
    {
        File permanentDirectory = new File("/tmp/xwiki");
        when(this.environment.getPermanentDirectory()).thenReturn(permanentDirectory);

        assertThrows(BlobStoreException.class, () -> this.blobStoreManager.getBlobStore(name));
    }
}
