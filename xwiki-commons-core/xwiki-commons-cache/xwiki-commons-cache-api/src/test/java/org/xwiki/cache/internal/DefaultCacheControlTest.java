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
package org.xwiki.cache.internal;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Validate {@link DefaultCacheControl}.
 * 
 * @version $Id$
 */
@ComponentTest
public class DefaultCacheControlTest
{
    @MockComponent
    private Execution execution;

    @InjectMockComponents
    private DefaultCacheControl cacheControl;

    private ExecutionContext context = new ExecutionContext();

    @BeforeEach
    public void beforeEach()
    {
        when(this.execution.getContext()).thenReturn(context);
    }

    private Date toDate(LocalDateTime dateTtime)
    {
        return Date.from(dateTtime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Tests

    @Test
    public void isCacheReadAllowed()
    {
        LocalDateTime now = LocalDateTime.now();

        assertTrue(this.cacheControl.isCacheReadAllowed());
        assertTrue(this.cacheControl.isCacheReadAllowed(now.minusDays(1)));
        assertTrue(this.cacheControl.isCacheReadAllowed(now.plusDays(1)));
        assertTrue(this.cacheControl.isCacheReadAllowed(toDate(now.minusDays(1))));
        assertTrue(this.cacheControl.isCacheReadAllowed(toDate(now.plusDays(1))));

        this.cacheControl.setCacheReadAllowed(false);

        assertFalse(this.cacheControl.isCacheReadAllowed());
        assertFalse(this.cacheControl.isCacheReadAllowed(now.minusDays(1)));
        assertTrue(this.cacheControl.isCacheReadAllowed(now.plusDays(1)));
        assertFalse(this.cacheControl.isCacheReadAllowed(toDate(now.minusDays(1))));
        assertTrue(this.cacheControl.isCacheReadAllowed(toDate(now.plusDays(1))));

        this.cacheControl.setCacheReadAllowed(true);

        assertTrue(this.cacheControl.isCacheReadAllowed());
        assertTrue(this.cacheControl.isCacheReadAllowed(now.minusDays(1)));
        assertTrue(this.cacheControl.isCacheReadAllowed(now.plusDays(1)));
        assertTrue(this.cacheControl.isCacheReadAllowed(toDate(now.minusDays(1))));
        assertTrue(this.cacheControl.isCacheReadAllowed(toDate(now.plusDays(1))));
    }
}
