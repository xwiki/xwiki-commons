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
package org.xwiki.cache.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.cache.DisposableCacheValue;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AbstractCache}.
 *
 * @version $Id$
 */
class AbstractCacheTest
{
    @RegisterExtension
    private final LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    private final AbstractCache<Object> cache = new AbstractCache<>(new CacheConfiguration("testcache"))
    {
        @Override
        public void set(String key, Object value)
        {
            // Not needed by the tests
        }

        @Override
        public Object get(String key)
        {
            return null;
        }

        @Override
        public void remove(String key)
        {
            // Not needed by the tests
        }

        @Override
        public void removeAll()
        {
            // Not needed by the tests
        }
    };

    @Test
    void disposeCacheValue() throws Exception
    {
        DisposableCacheValue value = mock();

        this.cache.disposeCacheValue(value);

        verify(value).dispose();
    }

    @Test
    void disposeCacheValueWhenDisposeFailsWithException() throws Exception
    {
        DisposableCacheValue value = mock();
        doThrow(new Exception("dispose failure")).when(value).dispose();

        this.cache.disposeCacheValue(value);

        assertEquals("Error when trying to dispose a cache object of cache [testcache]: [Exception: dispose failure]",
            this.logCapture.getMessage(0));
    }

    @Test
    void disposeCacheValueWhenDisposeFailsWithLinkageError() throws Exception
    {
        DisposableCacheValue value = mock();
        doThrow(new NoClassDefFoundError("missing class")).when(value).dispose();

        this.cache.disposeCacheValue(value);

        assertEquals("Error when trying to dispose a cache object of cache [testcache]: "
            + "[NoClassDefFoundError: missing class]", this.logCapture.getMessage(0));
    }

    @Test
    void disposeCacheValueWhenDisposeFailsWithVirtualMachineError() throws Exception
    {
        DisposableCacheValue value = mock();
        OutOfMemoryError error = new OutOfMemoryError();
        doThrow(error).when(value).dispose();

        assertSame(error, assertThrows(OutOfMemoryError.class, () -> this.cache.disposeCacheValue(value)));
    }
}
