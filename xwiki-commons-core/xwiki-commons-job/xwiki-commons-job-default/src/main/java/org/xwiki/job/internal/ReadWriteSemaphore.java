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
package org.xwiki.job.internal;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.xwiki.stability.Unstable;

/**
 * A specific concurrency implementation for managing Semaphore with Read/Write lock capabilities.
 * This semaphore allows several process to access a resource in read only, but lock it for accessing it in write when
 * the defined pool size is reached.
 *
 * @version $Id$
 * @since 12.5RC1
 */
@Unstable
public class ReadWriteSemaphore
{
    private final AtomicInteger readCounter;
    private final AtomicInteger writeCounter;
    private final Semaphore semaphore;

    /**
     * Create a semaphore with the given number of permits.
     * @param poolSize the number of permits to allow.
     */
    public ReadWriteSemaphore(int poolSize)
    {
        this.semaphore = new Semaphore(poolSize, true);
        this.readCounter = new AtomicInteger(0);
        this.writeCounter = new AtomicInteger(0);
    }

    /**
     * Takes one permit on the semaphore for writing, but also takes as much permits as they are reader
     * so we only allow reader or other writer depending on the semaphore size.
     */
    public void lockWrite()
    {
        this.writeCounter.incrementAndGet();

        if (this.writeCounter.get() == 1) {
            this.semaphore.acquireUninterruptibly(this.readCounter.get() + 1);
        } else {
            this.semaphore.acquireUninterruptibly();
        }
    }

    /**
     * Release a permit in the semaphore. If the writer counter is now empty, then we also release as many permits as
     * they are readers.
     */
    public void unlockWrite()
    {
        this.writeCounter.decrementAndGet();

        if (this.writeCounter.get() == 0) {
            this.semaphore.release(this.readCounter.get() + 1);
        } else {
            this.semaphore.release();
        }
    }

    /**
     * Increment the reader counter, and takes a permit in the semaphore only if there is already at least one writer
     * on it: we ensure to block new readers if the semaphore is full already.
     */
    public void lockRead()
    {
        this.readCounter.incrementAndGet();

        if (this.writeCounter.get() > 0) {
            this.semaphore.acquireUninterruptibly();
        }
    }

    /**
     * Decrement the reader counter, and release a permit in the semaphore only if there is already at least one writer
     * because we took previously a permit for this reader.
     */
    public void unlockRead()
    {
        this.readCounter.decrementAndGet();

        if (this.writeCounter.get() > 0) {
            this.semaphore.release();
        }
    }
}
