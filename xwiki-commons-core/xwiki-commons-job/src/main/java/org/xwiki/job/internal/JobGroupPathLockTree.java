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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.job.GroupedJobInitializerManager;
import org.xwiki.job.JobGroupPath;

/**
 * Helper for hierarchical locking.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Component(roles = JobGroupPathLockTree.class)
@Singleton
public class JobGroupPathLockTree
{
    @Inject
    private GroupedJobInitializerManager groupedJobInitializerManager;

    private final Map<JobGroupPath, ReadWriteSemaphore> tree = new ConcurrentHashMap<>();

    private synchronized ReadWriteSemaphore getSemaphore(JobGroupPath key)
    {
        ReadWriteSemaphore semaphore = this.tree.get(key);

        if (semaphore == null) {
            semaphore = new ReadWriteSemaphore(this.groupedJobInitializerManager.getGroupedJobInitializer(key)
                .getPoolSize());
            this.tree.put(key, semaphore);
        }

        return semaphore;
    }

    /**
     * @param key lock provided job group and all its parents
     */
    public void lock(JobGroupPath key)
    {
        getSemaphore(key).lockWrite();

        for (JobGroupPath path = key.getParent(); path != null; path = path.getParent()) {
            getSemaphore(path).lockRead();
        }
    }

    /**
     * @param key unlock provided job group and all its parents
     */
    public void unlock(JobGroupPath key)
    {
        getSemaphore(key).unlockWrite();

        for (JobGroupPath path = key.getParent(); path != null; path = path.getParent()) {
            getSemaphore(path).unlockRead();
        }
    }
}
