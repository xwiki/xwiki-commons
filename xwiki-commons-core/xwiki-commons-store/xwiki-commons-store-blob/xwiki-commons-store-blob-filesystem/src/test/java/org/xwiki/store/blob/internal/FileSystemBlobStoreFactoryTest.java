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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.environment.Environment;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStorePropertiesBuilder;
import org.xwiki.store.blob.FileSystemBlobStoreProperties;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FileSystemBlobStoreFactory} via created stores.
 *
 * @version $Id$
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@ComponentTest
class FileSystemBlobStoreFactoryTest
{
    @InjectMockComponents
    private FileSystemBlobStoreFactory factory;

    @MockComponent
    private Environment environment;

    @Test
    void getPropertiesClass()
    {
        assertEquals(FileSystemBlobStoreProperties.class, this.factory.getPropertiesClass());
    }

    @ParameterizedTest
    @CsvSource({
        "testStore, /tmp/xwiki/testStore",
        "test-store_with.special/chars, /tmp/xwiki/test-store_with.special/chars",
        "'', /tmp/xwiki"
    })
    void newPropertiesBuilder(String storeName, String expectedPath) throws Exception
    {
        File permanentDirectory = new File("/tmp/xwiki");
        when(this.environment.getPermanentDirectory()).thenReturn(permanentDirectory);

        BlobStorePropertiesBuilder builder = this.factory.newPropertiesBuilder(storeName);

        assertEquals(storeName, builder.getName());
        assertEquals(this.factory.getType(), builder.getType());
        Path rootDir = (Path) builder.get(FileSystemBlobStoreProperties.ROOT_DIRECTORY).orElseThrow();
        assertEquals(Path.of(expectedPath), rootDir);
    }

    @Test
    void newPropertiesBuilderWithNullName()
    {
        File permanentDirectory = new File("/tmp/xwiki");
        when(this.environment.getPermanentDirectory()).thenReturn(permanentDirectory);

        assertThrows(NullPointerException.class, () -> this.factory.newPropertiesBuilder(null));
    }

    @Test
    void create() throws Exception
    {
        // Create a populated properties bean
        FileSystemBlobStoreProperties properties = new FileSystemBlobStoreProperties();
        Path rootDir = Path.of("/var/blobstore");
        properties.setRootDirectory(rootDir);

        BlobStore blobStore = this.factory.create("test-store", properties);

        assertNotNull(blobStore);
        assertInstanceOf(FileSystemBlobStore.class, blobStore);
        assertEquals("test-store", blobStore.getName());
        assertEquals("filesystem", blobStore.getType());
        assertEquals(rootDir, ((FileSystemBlobStore) blobStore).getBlobFilePath(BlobPath.root()));
    }
}
