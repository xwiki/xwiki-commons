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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link ReadWriteSemaphore}.
 *
 * @version $Id$
 */
class ReadWriteSemaphoreTest
{
    /**
     * Reproduces a concurrency bug a previous implementation of this class had: it mutated {@code readCounter} and
     * {@code writeCounter} with {@code incrementAndGet()}/{@code decrementAndGet()}, then decided how many permits
     * to acquire or release on the strength of a <em>separate</em>, later {@code get()} on the same counter. That
     * gap between the two reads is a classic time-of-check-to-time-of-use race: by the time a thread re-read the
     * counter, another concurrently-running thread could already have changed it, so a caller could compute the
     * wrong number of permits to acquire or release. Losing even one permit that way permanently deadlocks every
     * later caller, since nothing can ever supply it again.
     *
     * <p>This hammers a parent/child pair of semaphores shaped like the ones {@link JobGroupPathLockTree} uses for
     * a group and one of its sub-groups (a child registers as both a writer of its own semaphore and a reader of
     * its parent's) with many concurrent readers and writers, and simply requires that every one of them eventually
     * completes. Against the buggy implementation described above this reliably deadlocks within a few hundred
     * operations; it is kept running for many more than that here for a comfortable margin.</p>
     */
    @Test
    @Timeout(30)
    void concurrentReadersAndWritersEventuallyAllComplete() throws InterruptedException
    {
        int roundsPerThread = 20000;
        int childThreads = 4;
        int parentWriterThreads = 2;

        ReadWriteSemaphore parent = new ReadWriteSemaphore(1);
        ReadWriteSemaphore child = new ReadWriteSemaphore(2);

        int totalThreads = childThreads + parentWriterThreads;
        CountDownLatch done = new CountDownLatch(totalThreads);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        for (int t = 0; t < childThreads; t++) {
            startThread("child-" + t, done, failure, () -> {
                for (int i = 0; i < roundsPerThread; i++) {
                    child.lockWrite();
                    parent.lockRead();
                    jitter();
                    parent.unlockRead();
                    child.unlockWrite();
                }
            });
        }

        for (int t = 0; t < parentWriterThreads; t++) {
            startThread("parentWriter-" + t, done, failure, () -> {
                for (int i = 0; i < roundsPerThread; i++) {
                    parent.lockWrite();
                    jitter();
                    parent.unlockWrite();
                }
            });
        }

        // A permanently lost permit means at least one of the threads above never returns from its blocking
        // lockRead()/lockWrite() call again, so the latch never reaches 0.
        boolean completed = done.await(25, TimeUnit.SECONDS);

        assertNull(failure.get(), "A thread failed unexpectedly: " + failure.get());
        assertTrue(completed, "At least one thread never completed its rounds: a permit was permanently lost, "
            + "deadlocking every later caller waiting on it");
    }

    /**
     * A tight lock/unlock loop rarely leaves a genuine gap where no writer is active, since another thread is
     * usually immediately ready to take its place. Occasionally yielding widens that window, which is where a lost
     * permit becomes observable.
     */
    private void jitter()
    {
        if (ThreadLocalRandom.current().nextInt(20) == 0) {
            Thread.yield();
        }
    }

    private void startThread(String name, CountDownLatch done, AtomicReference<Throwable> failure, Runnable task)
    {
        Thread thread = new Thread(() -> {
            try {
                task.run();
            } catch (Throwable e) {
                failure.compareAndSet(null, e);
            } finally {
                done.countDown();
            }
        }, name);
        thread.setDaemon(true);
        thread.start();
    }
}
