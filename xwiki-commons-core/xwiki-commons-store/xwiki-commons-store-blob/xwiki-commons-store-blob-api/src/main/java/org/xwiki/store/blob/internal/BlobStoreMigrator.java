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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobWriteMode;

/**
 * Migrates blobs from one {@link BlobStore} to another.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component(roles = BlobStoreMigrator.class)
@Singleton
public class BlobStoreMigrator
{
    private static final BlobPath MIGRATION_MARKER_PATH = BlobPath.absolute("_migration.txt");

    @Inject
    private Logger logger;

    /**
     * Migrate the content of {@code sourceStore} into {@code targetStore}.
     *
     * @param targetStore the destination store that should receive the blobs
     * @param sourceStore the source store containing the existing blobs
     * @throws BlobStoreException if the migration fails; the migration can be resumed afterwards
     */
    public void migrate(BlobStore targetStore, BlobStore sourceStore) throws BlobStoreException
    {
        String storeName = targetStore.getName();
        BlobPath markerPath = MIGRATION_MARKER_PATH;

        Blob markerBlob = targetStore.getBlob(markerPath);

        try {
            writeMarker(markerBlob, storeName, sourceStore, targetStore);
            this.logger.info("Starting blob store migration for [{}] (source: [{}], target: [{}]).",
                storeName, sourceStore.getHint(), targetStore.getHint());
        } catch (BlobAlreadyExistsException e) {
            this.logger.info("Detected existing migration marker [{}]; resuming migration of blob store [{}].",
                markerPath, storeName);
        } catch (BlobStoreException e) {
            throw new BlobStoreException(
                "Failed to create migration marker for store [%s] at [%s]".formatted(storeName, markerPath), e);
        }

        migrateContent(storeName, targetStore, sourceStore);
        this.logger.info("Completed blob store migration for [{}].", storeName);

        try {
            targetStore.deleteBlob(markerPath);
        } catch (BlobStoreException e) {
            // Fail the migration as restarting it after the store has already been used would be dangerous.
            throw new BlobStoreException(("Failed to delete migration marker [%s] after migrating store [%s]. "
                + "Remove the marker manually once the migration is verified.")
                .formatted(markerPath, storeName), e);
        }
    }

    /**
     * Checks if a migration is in progress for the given store.
     *
     * @param targetStore the store potentially holding an ongoing migration marker
     * @return {@code true} if a migration marker is present for the given store
     * @throws BlobStoreException if checking the marker fails
     */
    public boolean isMigrationInProgress(BlobStore targetStore) throws BlobStoreException
    {
        return targetStore.getBlob(MIGRATION_MARKER_PATH).exists();
    }

    private void writeMarker(Blob markerBlob, String storeName, BlobStore sourceStore, BlobStore targetStore)
        throws BlobStoreException
    {
        String payload = """
            Store Name: %s
            Source Store: %s
            Target Store: %s
            Started At: %s
            """.formatted(storeName, sourceStore.getHint(), targetStore.getHint(), Instant.now());

        try (ByteArrayInputStream stream =
            new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8))) {
            markerBlob.writeFromStream(stream, BlobWriteMode.CREATE_NEW);
        } catch (IOException e) {
            throw new BlobStoreException("Failed to write marker for store [%s]".formatted(storeName), e);
        }
    }

    private void migrateContent(String storeName, BlobStore targetStore, BlobStore sourceStore)
        throws BlobStoreException
    {
        try (Stream<Blob> blobs = sourceStore.listDescendants(BlobPath.root())) {
            for (Blob sourceBlob : (Iterable<Blob>) blobs::iterator) {
                BlobPath path = sourceBlob.getPath();
                moveBlob(path, sourceStore, targetStore);
            }
        } catch (BlobStoreException e) {
            throw e;
        } catch (Exception e) {
            throw new BlobStoreException("Failed to list blobs from migration store [%s] during migration of [%s]"
                .formatted(sourceStore.getHint(), storeName), e);
        }
    }

    private void moveBlob(BlobPath path, BlobStore sourceStore, BlobStore targetStore) throws BlobStoreException
    {
        try {
            targetStore.moveBlob(sourceStore, path, path, BlobWriteMode.REPLACE_EXISTING);
        } catch (BlobNotFoundException e) {
            // The blob disappeared between listing and moving; skip it and continue the migration.
            this.logger.debug("Skipping blob [{}] during migration of [{}] since it no longer exists in source store.",
                path, targetStore.getName());
        } catch (BlobStoreException e) {
            throw new BlobStoreException(
                ("Failed to move blob [%s] while migrating [%s] from [%s] to [%s]. "
                    + "Fix the issue or delete the source blob to unblock the migration."
                    + "The migration will be resumed on the next attempt.")
                    .formatted(path, targetStore.getName(), sourceStore.getHint(), targetStore.getHint()), e);
        }
    }
}
