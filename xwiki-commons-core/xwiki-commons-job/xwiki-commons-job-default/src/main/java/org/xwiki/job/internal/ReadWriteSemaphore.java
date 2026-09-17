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

/**
 * A specific concurrency implementation for managing Semaphore with Read/Write lock capabilities.
 * This semaphore allows several process to access a resource in read only, but lock it for accessing it in write when
 * the defined pool size is reached.
 *
 * @version $Id$
 * @since 12.5RC1
 */
public class ReadWriteSemaphore
{
    /**
     * Guards {@link #readCounter}, {@link #writeCounter} and {@link #activeWriters}, and is waited/notified on to
     * block callers until the state allows them to proceed. A previous implementation tracked the same counters but
     * encoded the write/read exclusion as a number of permits to acquire from a {@link java.util.concurrent.Semaphore}
     * computed from a counter snapshot; a reader's snapshot at lock time could legitimately differ from the one
     * still current at its own unlock time (readers and writers coming and going in between), so the permits taken
     * and given back did not always match, permanently losing some and deadlocking every later caller. Expressing
     * the invariant directly on the counters under one monitor removes that whole class of mismatch.
     */
    private final Object monitor = new Object();

    private final int poolSize;

    private int readCounter;

    /**
     * Incremented as soon as {@link #lockWrite()} is called, before the caller is actually granted write access, so
     * that any {@link #lockRead()} called afterwards blocks behind it instead of possibly running ahead of an
     * already-announced writer.
     */
    private int writeCounter;

    private int activeWriters;

    /**
     * Ticket handed out, in {@link #lockWrite()} call order, to each writer waiting for a slot; paired with
     * {@link #nextWriteTicketToServe}, this is what makes an earlier writer take priority over a later one once a
     * slot frees up. Plain {@code wait()}/{@code notifyAll()} gives no such guarantee on its own: every writer
     * waiting on {@link #monitor} wakes up and re-checks its condition in whatever order the JVM happens to grant
     * them the monitor, which without a ticket could let a later writer take a slot a still-waiting earlier one was
     * counting on.
     */
    private long nextWriteTicket;

    private long nextWriteTicketToServe;

    /**
     * Create a semaphore with the given number of permits.
     * @param poolSize the number of permits to allow.
     */
    public ReadWriteSemaphore(int poolSize)
    {
        this.poolSize = poolSize;
    }

    /**
     * Blocks until a slot is free, then takes it. Active readers and active writers share the same pool of slots
     * (bounded by the configured pool size): a writer only has to wait for as many currently-active readers as it
     * takes to free up a slot for it, not for every active reader to leave. Also immediately signals intent so that
     * any reader calling {@link #lockRead()} afterwards waits behind this call instead of possibly being granted
     * ahead of it. Writers are served in the order they called this method, so a slot freed up for an earlier
     * writer cannot be taken by a later one instead.
     */
    public void lockWrite()
    {
        synchronized (this.monitor) {
            this.writeCounter++;

            long myTicket = this.nextWriteTicket++;

            while (myTicket != this.nextWriteTicketToServe || this.activeWriters + this.readCounter >= this.poolSize) {
                waitUninterruptibly();
            }

            this.nextWriteTicketToServe++;
            this.activeWriters++;

            // Another writer next in line may already be able to proceed too (there can be more than one slot).
            this.monitor.notifyAll();
        }
    }

    /**
     * Release a writer slot taken by {@link #lockWrite()}.
     */
    public void unlockWrite()
    {
        synchronized (this.monitor) {
            this.activeWriters--;
            this.writeCounter--;

            this.monitor.notifyAll();
        }
    }

    /**
     * Blocks until no writer is active or waiting, then registers as a reader. Readers are never bounded by the
     * pool size, only mutually exclusive with writers.
     */
    public void lockRead()
    {
        synchronized (this.monitor) {
            while (this.writeCounter > 0) {
                waitUninterruptibly();
            }

            this.readCounter++;
        }
    }

    /**
     * Release a read registration taken by {@link #lockRead()}.
     */
    public void unlockRead()
    {
        synchronized (this.monitor) {
            this.readCounter--;

            this.monitor.notifyAll();
        }
    }

    /**
     * Wait on {@link #monitor} for a state change, without responding to interruption (matching the
     * {@code acquireUninterruptibly} semantics this class previously relied on), while still restoring the thread's
     * interrupted status before returning so the caller is not left unable to observe it.
     */
    private void waitUninterruptibly()
    {
        boolean interrupted = false;

        try {
            while (true) {
                try {
                    this.monitor.wait();
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
