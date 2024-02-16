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

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.cache.eviction.EntryEvictionConfiguration;
import org.xwiki.cache.infinispan.internal.InfinispanCacheFactory;
import org.xwiki.cache.infinispan.internal.InfinispanConfigurationLoader;
import org.xwiki.cache.internal.DefaultCacheFactory;
import org.xwiki.cache.internal.DefaultCacheManager;
import org.xwiki.cache.internal.DefaultCacheManagerConfiguration;
import org.xwiki.cache.test.AbstractEvictionGenericTestCache;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;

/**
 * Unit tests for {@link org.xwiki.cache.infinispan.internal.InfinispanCache}.
 *
 * @version $Id$
 */
@ComponentTest
// @formatter:off
@ComponentList({
    InfinispanCacheFactory.class,
    DefaultCacheManager.class,
    DefaultCacheFactory.class,
    DefaultCacheManagerConfiguration.class
})
// @formatter:off
class InfinispanCacheTest extends AbstractEvictionGenericTestCache
{
    @RegisterExtension
    public LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    public InfinispanCacheTest()
    {
        super("infinispan", true);
    }

    @AfterEach
    public void afterEach()
    {
        // Remove when https://jira.xwiki.org/browse/XCOMMONS-2151 is fixed
        this.logCapture.ignoreAllMessages(List.of(
            e -> e.getMessage().equals("ISPN000026: Caught exception purging data container!")));
    }

    @Override
    protected void customizeEviction(EntryEvictionConfiguration eviction)
    {
        // Force expiration thread to wakeup often
        eviction.put(InfinispanConfigurationLoader.CONFX_EXPIRATION_WAKEUPINTERVAL, 100);
    }
}
