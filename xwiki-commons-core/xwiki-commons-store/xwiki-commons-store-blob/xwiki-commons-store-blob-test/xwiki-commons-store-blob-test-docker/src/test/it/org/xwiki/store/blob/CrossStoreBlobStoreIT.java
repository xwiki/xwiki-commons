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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.store.blob.internal.BlobStoreMigrator;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;

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
    BlobStoreMigrator.class,
    S3BlobStoreFactory.class,
    S3BlobStore.class,
    S3ClientManager.class,
    S3CopyOperations.class,
    S3DeleteOperations.class
})
class CrossStoreBlobStoreIT
{
    private static final BlobPath MIGRATION_BLOB_PATH = BlobPath.absolute("_migration.txt");

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

    private BlobStoreMigrator blobStoreMigrator;

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

        // We can't directly inject the migrator because @Inject only supports interfaces in tests.
        this.blobStoreMigrator = this.componentManager.getInstance(BlobStoreMigrator.class);
    }

    @AfterEach
    void tearDown() throws Exception
    {
        // Cleanup filesystem
        if (this.filesystemStore != null) {
            this.filesystemStore.deleteBlobs(BlobPath.root());
        }

        // Cleanup S3
        if (this.s3Store != null) {
            this.s3Store.deleteBlobs(BlobPath.root());
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
    void copyLargeBlob(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.absolute("large.dat");
        BlobPath targetPath = BlobPath.absolute("large-copy.dat");
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
    void copyBlobWithReplaceExistingMode(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.absolute("shared", "document.dat");
        BlobPath targetPath = BlobPath.absolute("shared", "document.dat");
        byte[] sourceData = BlobStoreTestUtils.createTestData(2048, 111L);
        byte[] targetData = BlobStoreTestUtils.createTestData(1024, 222L);

        BlobStoreTestUtils.writeBlob(sourceStore, sourcePath, sourceData);
        BlobStoreTestUtils.writeBlob(targetStore, targetPath, targetData);

        Blob copiedBlob = targetStore.copyBlob(sourceStore, sourcePath, targetPath, BlobWriteMode.REPLACE_EXISTING);

        assertNotNull(copiedBlob);
        BlobStoreTestUtils.assertBlobEquals(targetStore, targetPath, sourceData);
        BlobStoreTestUtils.assertBlobEquals(sourceStore, sourcePath, sourceData);
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void copyBlobCreateNewFailsWhenTargetExists(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.absolute("shared", "existing.dat");
        BlobPath targetPath = BlobPath.absolute("shared", "existing.dat");
        byte[] sourceData = BlobStoreTestUtils.createTestData(128, 333L);
        byte[] targetData = BlobStoreTestUtils.createTestData(128, 444L);

        BlobStoreTestUtils.writeBlob(sourceStore, sourcePath, sourceData);
        BlobStoreTestUtils.writeBlob(targetStore, targetPath, targetData);

        assertThrows(BlobAlreadyExistsException.class,
            () -> targetStore.copyBlob(sourceStore, sourcePath, targetPath, BlobWriteMode.CREATE_NEW));

        BlobStoreTestUtils.assertBlobEquals(targetStore, targetPath, targetData);
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void moveBlobWhenTargetExists(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.absolute("source.dat");
        BlobPath targetPath = BlobPath.absolute("target.dat");
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

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void moveBlobWithReplaceExistingMode(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.absolute("shared", "move.dat");
        BlobPath targetPath = BlobPath.absolute("shared", "move.dat");
        byte[] sourceData = BlobStoreTestUtils.createTestData(1024, 555L);
        byte[] targetData = BlobStoreTestUtils.createTestData(512, 666L);

        BlobStoreTestUtils.writeBlob(sourceStore, sourcePath, sourceData);
        BlobStoreTestUtils.writeBlob(targetStore, targetPath, targetData);

        Blob movedBlob = targetStore.moveBlob(sourceStore, sourcePath, targetPath, BlobWriteMode.REPLACE_EXISTING);

        assertNotNull(movedBlob);
        BlobStoreTestUtils.assertBlobEquals(targetStore, targetPath, sourceData);
        assertFalse(sourceStore.getBlob(sourcePath).exists());
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void moveBlobCreateNewFailsWhenTargetExists(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath sourcePath = BlobPath.absolute("shared", "existingMove.dat");
        BlobPath targetPath = BlobPath.absolute("shared", "existingMove.dat");
        byte[] sourceData = BlobStoreTestUtils.createTestData(256, 777L);
        byte[] targetData = BlobStoreTestUtils.createTestData(256, 888L);

        BlobStoreTestUtils.writeBlob(sourceStore, sourcePath, sourceData);
        BlobStoreTestUtils.writeBlob(targetStore, targetPath, targetData);

        assertThrows(BlobAlreadyExistsException.class,
            () -> targetStore.moveBlob(sourceStore, sourcePath, targetPath, BlobWriteMode.CREATE_NEW));

        BlobStoreTestUtils.assertBlobEquals(targetStore, targetPath, targetData);
        BlobStoreTestUtils.assertBlobEquals(sourceStore, sourcePath, sourceData);
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void migrateCopiesBlobsAndCleansMarker(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath blobOne = BlobPath.absolute("docs", "report.pdf");
        BlobPath blobTwo = BlobPath.absolute("images", "logo.png");
        byte[] blobOneData = BlobStoreTestUtils.createTestData(1024, 999L);
        byte[] blobTwoData = BlobStoreTestUtils.createTestData(2048, 1001L);

        BlobStoreTestUtils.writeBlob(sourceStore, blobOne, blobOneData);
        BlobStoreTestUtils.writeBlob(sourceStore, blobTwo, blobTwoData);

        this.blobStoreMigrator.migrate(targetStore, sourceStore);

        BlobStoreTestUtils.assertBlobEquals(targetStore, blobOne, blobOneData);
        BlobStoreTestUtils.assertBlobEquals(targetStore, blobTwo, blobTwoData);

        // Verify that the source store is empty now. For better assertion messages, we collect the remaining blobs.
        try (Stream<Blob> remainingBlobs = sourceStore.listBlobs(BlobPath.root())) {
            List<BlobPath> remainingPaths = remainingBlobs.map(Blob::getPath).toList();
            assertThat("The source store should be emtpy after migration", remainingPaths, is(empty()));
        }

        // List all blobs in the target store and compare to the expected two blobs to ensure no migration marker
        // remains.
        try (Stream<Blob> blobs = targetStore.listBlobs(BlobPath.root())) {
            List<BlobPath> actualPaths = blobs.map(Blob::getPath).toList();
            assertThat(actualPaths, containsInAnyOrder(blobOne, blobTwo));
        }
    }

    @ParameterizedTest
    @MethodSource("getStorePairs")
    void migrateResumesAfterFailure(StoreType sourceStoreType, StoreType targetStoreType) throws Exception
    {
        BlobStore sourceStore = getStore(sourceStoreType);
        BlobStore targetStore = getStore(targetStoreType);

        BlobPath blobPath = BlobPath.absolute("resume", "item.bin");
        byte[] blobData = BlobStoreTestUtils.createTestData(1536, 2024L);
        BlobStoreTestUtils.writeBlob(sourceStore, blobPath, blobData);

        BlobStore spyTargetStore = spy(targetStore);
        AtomicBoolean shouldFail = new AtomicBoolean(true);
        Mockito.doAnswer(invocation -> {
            if (shouldFail.getAndSet(false)) {
                throw new BlobStoreException("Simulated move failure");
            }
            return invocation.callRealMethod();
        })
            .when(spyTargetStore)
            .moveBlob(eq(sourceStore), any(BlobPath.class), any(BlobPath.class), any());

        assertThrows(BlobStoreException.class,
            () -> this.blobStoreMigrator.migrate(spyTargetStore, sourceStore));

        assertTrue(this.blobStoreMigrator.isMigrationInProgress(spyTargetStore));
        assertTrue(spyTargetStore.getBlob(MIGRATION_BLOB_PATH).exists());
        // Data should still be only in source store at this point.
        BlobStoreTestUtils.assertBlobEquals(sourceStore, blobPath, blobData);
        assertFalse(targetStore.getBlob(blobPath).exists());

        this.blobStoreMigrator.migrate(spyTargetStore, sourceStore);

        BlobStoreTestUtils.assertBlobEquals(targetStore, blobPath, blobData);
        assertFalse(sourceStore.getBlob(blobPath).exists());
        assertFalse(spyTargetStore.getBlob(MIGRATION_BLOB_PATH).exists());
    }
}
