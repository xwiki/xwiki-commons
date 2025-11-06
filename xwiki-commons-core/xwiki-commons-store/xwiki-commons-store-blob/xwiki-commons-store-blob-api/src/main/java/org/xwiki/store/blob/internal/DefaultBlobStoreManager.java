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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.function.Failable;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Disposable;
import org.xwiki.properties.BeanManager;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreFactory;
import org.xwiki.store.blob.BlobStoreManager;
import org.xwiki.store.blob.BlobStoreProperties;
import org.xwiki.store.blob.BlobStorePropertiesBuilder;
import org.xwiki.store.blob.BlobStorePropertiesCustomizer;

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

    @Inject
    private BeanManager beanManager;

    @Inject
    private Logger logger;

    @Inject
    private BlobStoreMigrator blobStoreMigrator;

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

        if (migrationStoreHint != null && !migrationStoreHint.equals(storeHint)) {
            boolean migrationInProgress = this.blobStoreMigrator.isMigrationInProgress(blobStore);
            boolean needsMigration = migrationInProgress || !blobStore.hasDescendants(BlobPath.root());
            if (needsMigration) {
                BlobStore migrationStore = getBlobStore(name, migrationStoreHint);
                this.blobStoreMigrator.migrate(blobStore, migrationStore);
            }
        }

        return blobStore;
    }

    private BlobStore getBlobStore(String name, String storeHint) throws ComponentLookupException, BlobStoreException
    {
        BlobStoreFactory factory = this.componentManager.getInstance(BlobStoreFactory.class, storeHint);

        BlobStorePropertiesBuilder propertiesBuilder = factory.newPropertiesBuilder(name);

        // Apply customizers.
        for (BlobStorePropertiesCustomizer customizer : this.componentManager
            .<BlobStorePropertiesCustomizer>getInstanceList(BlobStorePropertiesCustomizer.class)) {
            customizer.customize(propertiesBuilder);
        }

        // Create properties bean and populate it using BeanManager.
        Class<? extends BlobStoreProperties> propertiesClass = factory.getPropertiesClass();
        BlobStoreProperties properties;
        try {
            properties = propertiesClass.getDeclaredConstructor().newInstance();
            this.beanManager.populate(properties, propertiesBuilder.getAllProperties());
        } catch (Exception e) {
            throw new BlobStoreException("Failed to populate blob store properties for store [" + name + "]", e);
        }

        return factory.create(name, properties);
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        for (BlobStore blobStore : this.blobStores.values()) {
            if (blobStore instanceof Disposable disposableStore) {
                try {
                    disposableStore.dispose();
                } catch (Exception e) {
                    this.logger.warn("Failed to dispose blob store [{}], root cause: [{}].",
                        blobStore.getName(), ExceptionUtils.getRootCauseMessage(e));
                }
            }
        }
    }
}
