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
package org.xwiki.netflux.internal;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ChannelStore}.
 * 
 * @version $Id$
 * @since 13.7RC1
 */
@ComponentTest
class ChannelStoreTest
{
    @InjectMockComponents
    private ChannelStore channelStore;

    @Test
    void createGetRemove()
    {
        Channel channel = this.channelStore.create();
        assertSame(channel, this.channelStore.get(channel.getKey()));
        assertTrue(this.channelStore.remove(channel));
        assertNull(this.channelStore.get(channel.getKey()));
        assertFalse(this.channelStore.remove(channel));
    }

    @Test
    void prune()
    {
        Channel foo = this.channelStore.create();
        Channel bar = this.channelStore.create();

        // Make the second channel expire.
        long now = new Date().getTime();
        long expirationPeriod = 1000 * 60 * 60 * 2; /* 2 hours */
        ReflectionUtils.setFieldValue(bar, "creationDate", now - expirationPeriod - 1);

        this.channelStore.prune();

        assertSame(foo, this.channelStore.get(foo.getKey()));
        assertNull(this.channelStore.get(bar.getKey()));
    }
}
