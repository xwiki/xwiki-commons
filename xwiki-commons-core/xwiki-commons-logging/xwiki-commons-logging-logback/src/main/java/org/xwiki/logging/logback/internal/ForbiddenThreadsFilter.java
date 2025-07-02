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
package org.xwiki.logging.logback.internal;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Marker;
import org.xwiki.logging.Logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Filters events not from the provided {@link Thread}.
 *
 * @version $Id$
 */
public class ForbiddenThreadsFilter extends Filter<ILoggingEvent>
{
    /**
     * The forbidden thread.
     */
    private Set<Thread> threads = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private boolean canFilter(ILoggingEvent event)
    {
        // Checking the presence of a Logger.ROOT_MARKER marker
        List<Marker> markers = event.getMarkerList();
        if (markers != null) {
            for (Marker marker : markers) {
                if (marker == Logger.ROOT_MARKER) {
                    return false;
                }
            }
        }

        // VirtualMachineError throwable should always end up in the main log
        ThrowableProxy throwable =
            event.getThrowableProxy() instanceof ThrowableProxy throwableProxy ? throwableProxy : null;
        while (throwable != null) {
            if (throwable.getThrowable() instanceof VirtualMachineError) {
                return false;
            }
        }

        return true;
    }

    @Override
    public FilterReply decide(ILoggingEvent event)
    {
        // Make sure it's allowed to filter the event
        // Check that the current thread is part of the allowed threads
        if (canFilter(event) && this.threads.contains(Thread.currentThread())) {
            return FilterReply.DENY;
        }

        return FilterReply.NEUTRAL;
    }

    /**
     * @param thread the new forbidden thread
     */
    public void addThread(Thread thread)
    {
        this.threads.add(thread);
    }

    /**
     * @param thread the thread to remove from the list of forbidden threads
     */
    public void removeThread(Thread thread)
    {
        this.threads.remove(thread);
    }
}
