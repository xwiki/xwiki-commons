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
package org.xwiki.cache.infinispan;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.SingleFileStoreConfiguration;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.config.LRUCacheConfiguration;
import org.xwiki.cache.infinispan.internal.InfinispanCacheFactory;
import org.xwiki.cache.internal.DefaultCacheFactory;
import org.xwiki.cache.internal.DefaultCacheManager;
import org.xwiki.cache.internal.DefaultCacheManagerConfiguration;
import org.xwiki.cache.test.AbstractTestCache;
import org.xwiki.environment.Environment;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Verify that defining an Infinispan config file is taken into account.
 *
 * @version $Id$
 * @since 3.4M1
 */
@ComponentTest
// @formatter:off
@ComponentList({
    InfinispanCacheFactory.class,
    DefaultCacheManager.class,
    DefaultCacheFactory.class,
    DefaultCacheManagerConfiguration.class
})
// @formatter:on
public class InfinispanConfigTest extends AbstractTestCache
{
    private Environment environment;

    public InfinispanConfigTest()
    {
        super("infinispan");
    }

    @BeforeEach
    @Override
    public void before() throws Exception
    {
        this.environment = this.componentManager.registerMockComponent(Environment.class);

        File testDirectory = new File("target/test-" + new Date().getTime()).getAbsoluteFile();

        File temporaryDirectory = new File(testDirectory, "temporary");
        File permanentDirectory = new File(testDirectory, "permanent");

        when(this.environment.getTemporaryDirectory()).thenReturn(temporaryDirectory);
        when(this.environment.getPermanentDirectory()).thenReturn(permanentDirectory);

        when(this.environment.getResourceAsStream("/WEB-INF/cache/infinispan/config.xml"))
            .thenReturn(getClass().getResourceAsStream("/infinispan/test-config.xml"));

        super.before();
    }

    private org.infinispan.Cache<String, String> createCache(CacheConfiguration configuration) throws Exception
    {
        org.xwiki.cache.Cache<String> cache = getCacheFactory().newCache(configuration);

        return (Cache<String, String>) FieldUtils.readField(cache, "cache", true);
    }

    @Test
    public void createCacheWhenUnnamed() throws Exception
    {
        CacheConfiguration configuration = new CacheConfiguration("noname");

        Cache<String, String> cache = createCache(configuration);

        assertEquals("noname", cache.getName());
    }

    @Test
    public void createCacheWhenFileCacheNoPath() throws Exception
    {
        CacheConfiguration configuration = new LRUCacheConfiguration("file-cache-nopath", 42);

        Cache<String, String> cache = createCache(configuration);

        List<StoreConfiguration> stores = cache.getCacheConfiguration().persistence().stores();
        assertEquals(1, stores.size());

        String location = ((SingleFileStoreConfiguration) stores.get(0)).location();
        assertEquals(this.environment.getTemporaryDirectory().toString() + "/cache", location);
    }

    @Test
    public void createCacheWhenFileCachePath() throws Exception
    {
        CacheConfiguration configuration = new LRUCacheConfiguration("file-cache-path", 42);

        Cache<String, String> cache = createCache(configuration);

        List<StoreConfiguration> stores = cache.getCacheConfiguration().persistence().stores();
        assertEquals(1, stores.size());

        String location = ((SingleFileStoreConfiguration) stores.get(0)).location();
        assertEquals(System.getProperty("java.io.tmpdir"), location);
    }
}
