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

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.store.blob.internal.FileSystemBlobStoreFactory;
import org.xwiki.store.blob.internal.S3BlobStore;
import org.xwiki.store.blob.internal.S3BlobStoreFactory;
import org.xwiki.store.blob.internal.S3ClientManager;
import org.xwiki.store.blob.internal.S3CopyOperations;
import org.xwiki.store.blob.internal.S3DeleteOperations;
import org.xwiki.test.TestEnvironment;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.XWikiTempDir;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for cross-store operations between filesystem and S3 blob stores.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@ComponentTest
@ExtendWith(BlobStoreExtension.class)
@ComponentList({
    TestEnvironment.class,
    FileSystemBlobStoreFactory.class,
    S3BlobStoreFactory.class,
    S3BlobStore.class,
    S3ClientManager.class,
    S3CopyOperations.class,
    S3DeleteOperations.class
})
class CrossStoreBlobStoreIT
{
    private enum StoreType
    {
        FILESYSTEM, S3
    }

    @XWikiTempDir
    private File tmpDir;

    @InjectBlobStoreContainer
    private BlobStoreContainer container;

    @InjectComponentManager
    private ComponentManager componentManager;

    @Inject
    @Named("filesystem")
    private BlobStoreFactory filesystemBlobStoreFactory;

    private BlobStore filesystemStore;

    private BlobStore s3Store;

    @BeforeEach
    void setUp() throws Exception
    {
        // Setup filesystem store
        FileSystemBlobStoreProperties fsProps = new FileSystemBlobStoreProperties();
        fsProps.setRootDirectory(this.tmpDir.toPath().resolve("filesystem"));
        this.filesystemStore = this.filesystemBlobStoreFactory.create("filesystem", fsProps);

        // Setup S3 store
        S3BlobStoreProperties s3Props = new S3BlobStoreProperties();
        s3Props.setBucket(this.container.bucketName());
        s3Props.setKeyPrefix("crossstore");
        s3Props.setMultipartUploadPartSize(5L * 1024 * 1024);
        s3Props.setMultipartCopyPartSize(5L * 1024 * 1024);

        // Get the factory from the component manager as we cannot inject it directly because the S3 configuration is
        // initialized too late. Injecting a provider isn't supported by the test framework.
        S3BlobStoreFactory s3Factory = this.componentManager.getInstance(BlobStoreFactory.class, "s3");
        this.s3Store = s3Factory.create("s3store", s3Props);
    }

    @AfterEach
    void tearDown() throws Exception
    {
        // Cleanup filesystem
        if (this.filesystemStore != null) {
            this.filesystemStore.deleteBlobs(BlobPath.ROOT);
        }

        // Cleanup S3
        if (this.s3Store != null) {
            this.s3Store.deleteBlobs(BlobPath.ROOT);
        }
    }

    private BlobStore getStore(StoreType storeType)
    {
        return switch (storeType) {
            case FILESYSTEM -> this.filesystemStore;
            case S3 -> this.s3Store;
        };
    }

    private static Stream<StoreType[]> getStorePairs()
    {
        return Stream.of(
            new StoreType[]{StoreType.FILESYSTEM, StoreType.S3},
            new StoreType[]{StoreType.S3, StoreType.FILESYSTEM}
        );
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void copyBlob(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.of(List.of("source.dat"));
        BlobPath targetPath = BlobPath.of(List.of("target.dat"));
        byte[] data = BlobStoreTestUtils.createTestData(1024);

        // Write to source store
        BlobStoreTestUtils.writeBlob(sourceStore, sourcePath, data);

        // Copy from source to target
        Blob copiedBlob = targetStore.copyBlob(sourceStore, sourcePath, targetPath);
        assertNotNull(copiedBlob);
        assertTrue(copiedBlob.exists());

        // Verify data in target store
        BlobStoreTestUtils.assertBlobEquals(targetStore, targetPath, data);

        // Verify source still exists
        assertTrue(sourceStore.getBlob(sourcePath).exists());
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void moveBlob(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.of(List.of("source.dat"));
        BlobPath targetPath = BlobPath.of(List.of("target.dat"));
        byte[] data = BlobStoreTestUtils.createTestData(1024);

        // Write to source store
        BlobStoreTestUtils.writeBlob(sourceStore, sourcePath, data);

        // Move from source to target
        Blob movedBlob = targetStore.moveBlob(sourceStore, sourcePath, targetPath);
        assertNotNull(movedBlob);
        assertTrue(movedBlob.exists());

        // Verify data in target store
        BlobStoreTestUtils.assertBlobEquals(targetStore, targetPath, data);

        // Verify source no longer exists
        assertFalse(sourceStore.getBlob(sourcePath).exists());
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void moveDirectory(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        byte[] data = BlobStoreTestUtils.createTestData(100);

        // Create directory structure in source store
        BlobStoreTestUtils.writeBlob(sourceStore, BlobPath.of(List.of("sourceDir", "file1.dat")), data);
        BlobStoreTestUtils.writeBlob(sourceStore, BlobPath.of(List.of("sourceDir", "file2.dat")), data);
        BlobStoreTestUtils.writeBlob(sourceStore, BlobPath.of(List.of("sourceDir", "subdir", "file3.dat")), data);

        BlobPath sourcePath = BlobPath.of(List.of("sourceDir"));
        BlobPath targetPath = BlobPath.of(List.of("targetDir"));

        // Move directory from source to target
        targetStore.moveDirectory(sourceStore, sourcePath, targetPath);

        // Verify files are in target store
        assertTrue(targetStore.getBlob(targetPath.resolve("file1.dat")).exists());
        assertTrue(targetStore.getBlob(targetPath.resolve("file2.dat")).exists());
        assertTrue(targetStore.getBlob(targetPath.resolve("subdir", "file3.dat")).exists());

        // Verify source directory is empty
        assertFalse(sourceStore.getBlob(BlobPath.of(List.of("sourceDir", "file1.dat"))).exists());
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void copyLargeBlob(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.of(List.of("large.dat"));
        BlobPath targetPath = BlobPath.of(List.of("large-copy.dat"));
        // 6 MB - should trigger multipart for S3
        byte[] data = BlobStoreTestUtils.createTestData(6 * 1024 * 1024, 99999L);

        // Write to source store
        BlobStoreTestUtils.writeBlob(sourceStore, sourcePath, data);

        // Copy from source to target
        Blob copiedBlob = targetStore.copyBlob(sourceStore, sourcePath, targetPath);
        assertNotNull(copiedBlob);
        assertTrue(copiedBlob.exists());

        // Verify data in target store
        BlobStoreTestUtils.assertBlobEquals(targetStore, targetPath, data);
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void moveBlobWhenTargetExists(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.of(List.of("source.dat"));
        BlobPath targetPath = BlobPath.of(List.of("target.dat"));
        byte[] sourceData = BlobStoreTestUtils.createTestData(1024, 12345L);
        byte[] targetData = BlobStoreTestUtils.createTestData(512, 54321L);

        // Write to source store
        BlobStoreTestUtils.writeBlob(sourceStore, sourcePath, sourceData);

        // Write existing blob to target store
        BlobStoreTestUtils.writeBlob(targetStore, targetPath, targetData);

        // Move from source to target (should throw exception)
        BlobAlreadyExistsException exception = assertThrows(BlobAlreadyExistsException.class, () -> {
            targetStore.moveBlob(sourceStore, sourcePath, targetPath);
        });

        assertTrue(exception.getMessage().contains("already exists"));
        // Verify source still exists.
        BlobStoreTestUtils.assertBlobEquals(sourceStore, sourcePath, sourceData);
        // Verify target data is unchanged.
        BlobStoreTestUtils.assertBlobEquals(targetStore, targetPath, targetData);
    }
}
