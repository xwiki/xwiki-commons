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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.xwiki.job.JobGroupPath;

/**
 * Helper for hierarchical locking.
 *
 * @version $Id$
 * @since 6.1M2
 */
public class JobGroupPathLockTree
{
    private final Map<JobGroupPath, ReadWriteLock> tree = new ConcurrentHashMap<>();

    private synchronized ReadWriteLock getLock(JobGroupPath key)
    {
        ReadWriteLock lock = this.tree.get(key);

        if (lock == null) {
            lock = new ReentrantReadWriteLock(true);
            this.tree.put(key, lock);
        }

        return lock;
    }

    /**
     * @param key lock provided job group and all its parents
     */
    public void lock(JobGroupPath key)
    {
        getLock(key).writeLock().lock();

        for (JobGroupPath path = key.getParent(); path != null; path = path.getParent()) {
            getLock(path).readLock().lock();
        }
    }

    /**
     * @param key unlock provided job group and all its parents
     */
    public void unlock(JobGroupPath key)
    {
        getLock(key).writeLock().unlock();

        for (JobGroupPath path = key.getParent(); path != null; path = path.getParent()) {
            getLock(path).readLock().unlock();
        }
    }
}
