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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.apache.commons.lang3.function.Failable;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Disposable;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreManager;

/**
 * Default implementation of {@link BlobStoreManager} that retrieves blob stores based on the name and the
 * configured store hint.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component
@Singleton
public class DefaultBlobStoreManager implements BlobStoreManager, Disposable
{
    @Inject
    private BlobStoreConfiguration configuration;

    @Inject
    private ComponentManager componentManager;

    private final Map<String, BlobStore> blobStores = new ConcurrentHashMap<>();

    @Override
    public BlobStore getBlobStore(String name) throws BlobStoreException
    {
        try {
            return this.blobStores.computeIfAbsent(name,
                key -> {
                    try {
                        return getAndMaybeMigrateStore(name);
                    } catch (ComponentLookupException | BlobStoreException e) {
                        throw Failable.rethrow(e);
                    }
                });
        } catch (UndeclaredThrowableException e) {
            if (e.getUndeclaredThrowable() instanceof BlobStoreException blobStoreException) {
                throw blobStoreException;
            }
            throw new BlobStoreException("Failed to get or create blob store with name [" + name + "]",
                e.getUndeclaredThrowable());
        }
    }

    private BlobStore getAndMaybeMigrateStore(String name) throws ComponentLookupException, BlobStoreException
    {
        String storeHint = this.configuration.getStoreHint();
        String migrationStoreHint = this.configuration.getMigrationStoreHint();
        BlobStore blobStore = getBlobStore(name, storeHint);

        if (migrationStoreHint != null && !migrationStoreHint.equals(storeHint)
            && blobStore.isEmptyDirectory(BlobPath.ROOT))
        {
            BlobStore migrationStore = getBlobStore(name, migrationStoreHint);
            blobStore.moveDirectory(migrationStore, BlobPath.ROOT, BlobPath.ROOT);
        }

        return blobStore;
    }

    private BlobStore getBlobStore(String name, String storeHint) throws ComponentLookupException, BlobStoreException
    {
        BlobStoreManager blobStoreManager;
        // TODO: re-consider this design.
        String specificHint = storeHint + "/" + name;
        if (this.componentManager.hasComponent(BlobStoreManager.class, specificHint)) {
            blobStoreManager = this.componentManager.getInstance(BlobStoreManager.class, specificHint);
        } else {
            blobStoreManager = this.componentManager.getInstance(BlobStoreManager.class, storeHint);
        }
        return blobStoreManager.getBlobStore(name);
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        for (BlobStore blobStore : this.blobStores.values()) {
            if (blobStore instanceof Disposable disposableStore) {
                disposableStore.dispose();
            }
        }
    }
}
