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
package org.xwiki.cache.infinispan.internal;

import org.apache.commons.lang3.StringUtils;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.ExpirationConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfiguration;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.configuration.cache.SingleFileStoreConfiguration;
import org.infinispan.configuration.cache.SingleFileStoreConfigurationBuilder;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.configuration.cache.StoreConfigurationBuilder;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionType;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.EntryEvictionConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.cache.util.AbstractCacheConfigurationLoader;
import org.xwiki.environment.Environment;

/**
 * Customize Infinispan configuration based on XWiki Cache configuration.
 * 
 * @version $Id$
 */
public class InfinispanConfigurationLoader extends AbstractCacheConfigurationLoader
{
    /**
     * The name of the field containing the wakeup interval used for expiration to set in the
     * {@link ExpirationConfigurationBuilder}.
     */
    public static final String CONFX_EXPIRATION_WAKEUPINTERVAL = "infinispan.expiration.wakeupinterval";

    /**
     * The default location of a filesystem based cache loader when not provided in the xml configuration file.
     */
    private static final String DEFAULT_SINGLEFILESTORE_LOCATION = "Infinispan-SingleFileStore";

    /**
     * @param configuration the XWiki cache configuration
     * @param environment teh environment, can be null
     */
    public InfinispanConfigurationLoader(CacheConfiguration configuration, Environment environment)
    {
        super(configuration, environment, null);
    }

    /**
     * Customize the eviction configuration.
     * 
     * @param buil the configuration builder
     * @param configuration the configuration
     * @return the configuration builder
     */
    private void customizeEviction(ConfigurationBuilder builder)
    {
        EntryEvictionConfiguration eec =
            (EntryEvictionConfiguration) getCacheConfiguration().get(EntryEvictionConfiguration.CONFIGURATIONID);

        if (eec != null && eec.getAlgorithm() == EntryEvictionConfiguration.Algorithm.LRU) {
            ////////////////////
            // Eviction
            // Max entries
            customizeEvictionMaxEntries(builder, eec);

            ////////////////////
            // Expiration
            // Wakeup interval
            customizeExpirationWakeUpInterval(builder, eec);

            // Max idle
            customizeExpirationMaxIdle(builder, eec);

            // Lifespan
            customizeExpirationLifespan(builder, eec);
        }
    }

    private void customizeEvictionMaxEntries(ConfigurationBuilder builder, EntryEvictionConfiguration eec)
    {
        Object maxEntries = eec.get(LRUEvictionConfiguration.MAXENTRIES_ID);
        if (maxEntries instanceof Number) {
            builder.memory().evictionStrategy(EvictionStrategy.REMOVE);
            builder.memory().evictionType(EvictionType.COUNT).size(((Number) maxEntries).longValue());
        }

    }

    private void customizeExpirationWakeUpInterval(ConfigurationBuilder builder, EntryEvictionConfiguration eec)
    {
        if (eec.get(CONFX_EXPIRATION_WAKEUPINTERVAL) instanceof Number) {
            builder.expiration().wakeUpInterval(((Number) eec.get(CONFX_EXPIRATION_WAKEUPINTERVAL)).longValue());
        }
    }

    private void customizeExpirationMaxIdle(ConfigurationBuilder builder, EntryEvictionConfiguration eec)
    {
        if (eec.getTimeToLive() > 0) {
            builder.expiration().maxIdle(eec.getTimeToLive() * 1000L);
        }
    }

    private void customizeExpirationLifespan(ConfigurationBuilder builder, EntryEvictionConfiguration eec)
    {
        if (eec.containsKey(LRUEvictionConfiguration.LIFESPAN_ID)) {
            long lifespan = (int) eec.get(LRUEvictionConfiguration.LIFESPAN_ID) * 1000L;
            builder.expiration().lifespan(lifespan);
        }
    }

    /**
     * @param configuration the configuration to check
     * @return true if one of the loader is an incomplete {@link FileCacheStoreConfiguration}
     */
    private boolean containsIncompleteFileLoader(Configuration configuration)
    {
        PersistenceConfiguration persistenceConfiguration = configuration.persistence();

        for (StoreConfiguration storeConfiguration : persistenceConfiguration.stores()) {
            if (storeConfiguration instanceof SingleFileStoreConfiguration) {
                SingleFileStoreConfiguration singleFileStoreConfiguration =
                    (SingleFileStoreConfiguration) storeConfiguration;

                String location = singleFileStoreConfiguration.location();

                // "Infinispan-SingleFileStore" is the default location...
                if (StringUtils.isBlank(location) || location.equals(DEFAULT_SINGLEFILESTORE_LOCATION)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Add missing location for filesystem based cache.
     * 
     * @param currentBuilder the configuration builder
     * @param configuration the configuration
     * @return the configuration builder
     */
    private void completeFilesystem(ConfigurationBuilder builder, Configuration configuration)
    {
        PersistenceConfigurationBuilder persistence = builder.persistence();

        if (containsIncompleteFileLoader(configuration)) {
            for (StoreConfigurationBuilder<?, ?> store : persistence.stores()) {
                if (store instanceof SingleFileStoreConfigurationBuilder) {
                    SingleFileStoreConfigurationBuilder singleFileStore = (SingleFileStoreConfigurationBuilder) store;

                    singleFileStore.location(createTempDir());
                }
            }
        }
    }

    /**
     * Customize provided configuration based on XWiki cache configuration.
     * 
     * @param defaultConfiguration the default Infinispan configuration
     * @param namedConfiguration the named default Infinispan configuration
     * @return the new configuration or the passed one if nothing changed
     */
    public Configuration customize(Configuration defaultConfiguration, Configuration namedConfiguration)
    {
        // Set custom configuration

        ConfigurationBuilder builder = new ConfigurationBuilder();

        // Named configuration have priority
        if (namedConfiguration != null) {
            read(builder, namedConfiguration);
        } else {
            // Add default configuration
            if (defaultConfiguration != null) {
                read(builder, defaultConfiguration);
            }

            customizeEviction(builder);
        }

        return builder.build();
    }

    private void read(ConfigurationBuilder builder, Configuration configuration)
    {
        builder.read(configuration);

        // Make sure filesystem based caches have a proper location
        completeFilesystem(builder, configuration);        
    }
}
