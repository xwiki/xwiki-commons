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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.inject.Inject;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.LRUCacheConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.JobStatusStore;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.tail.LoggerTail;

import com.google.common.util.concurrent.Striped;

/**
 * Default implementation of {@link JobStatusStore} that handles caching and locking. The actual storage is delegated to
 * {@link PersistentJobStatusStore}.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Component
@Singleton
public class DefaultJobStatusStore implements JobStatusStore, Initializable
{
    private static final JobStatus NOSTATUS = new DefaultJobStatus<>(null, null, null, null, null);

    @Inject
    private Logger logger;

    private ExecutorService executorService;

    @Inject
    private JobManagerConfiguration configuration;

    @Inject
    private PersistentJobStatusStore persistentJobStatusStore;

    @Inject
    private CacheManager cacheManager;

    private Cache<JobStatus> cache;

    private final Striped<Lock> locks = Striped.lock(16);

    /**
     * Tracks in-flight writes (both sync and async) to prevent stale reads from disk during the window between cache
     * eviction and save completion.
     */
    private final ConcurrentMap<String, JobStatus> pendingWrites = new ConcurrentHashMap<>();

    class JobStatusSerializerRunnable implements Runnable
    {
        /**
         * The status to store.
         */
        private final JobStatus status;

        JobStatusSerializerRunnable(JobStatus status)
        {
            this.status = status;
        }

        @Override
        public void run()
        {
            saveJobStatus(this.status);
        }
    }


    @Override
    public void initialize() throws InitializationException
    {
        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("Job status serializer")
            .daemon(true).priority(Thread.MIN_PRIORITY).build();
        this.executorService =
            new ThreadPoolExecutor(0, 10, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory);

        // Initialize cache
        LRUCacheConfiguration cacheConfiguration =
            new LRUCacheConfiguration("job.status", this.configuration.getJobStatusCacheSize());
        try {
            this.cache = this.cacheManager.createNewCache(cacheConfiguration);
        } catch (CacheException e) {
            throw new InitializationException("Failed to initialize job status cache", e);
        }
    }

    @Override
    public JobStatus getJobStatus(List<String> id)
    {
        String idString = toUniqueString(id);

        JobStatus status = this.cache.get(idString);

        if (status == null) {
            Lock lock = this.locks.get(idString);
            lock.lock();
            try {
                // Check again to avoid reading the same status twice. All reading happens inside the lock.
                status = this.cache.get(idString);

                if (status == null) {
                    // Check pending writes before loading from disk to avoid reading stale data during the
                    // window between cache eviction and save completion.
                    status = this.pendingWrites.get(idString);
                    if (status != null) {
                        this.cache.set(idString, status);
                    } else {
                        try {
                            status = this.persistentJobStatusStore.loadJobStatus(id);
                            this.cache.set(idString, computeCacheValue(status));
                        } catch (Exception e) {
                            this.logger.warn("Failed to load job status for id {}", id, e);

                            this.cache.remove(idString);
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        return status == NOSTATUS ? null : status;
    }

    private static @NonNull JobStatus computeCacheValue(JobStatus status)
    {
        return status != null ? status : NOSTATUS;
    }

    @Override
    public void store(JobStatus status)
    {
        store(status, false);
    }

    @Override
    public void storeAsync(JobStatus status)
    {
        store(status, true);
    }

    @Override
    public void remove(List<String> id)
    {
        String idString = toUniqueString(id);

        Lock lock = this.locks.get(idString);
        lock.lock();

        try {
            this.persistentJobStatusStore.removeJobStatus(id);

            this.cache.remove(idString);
        } catch (IOException e) {
            this.logger.warn("Failed to remove job status for id {}", id, e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public LoggerTail createLoggerTail(List<String> jobId, boolean readonly)
    {
        return this.persistentJobStatusStore.createLoggerTail(jobId, readonly);
    }

    private String toUniqueString(List<String> id)
    {
        return StringUtils.join(id, '/');
    }

    private void store(JobStatus status, boolean async)
    {
        if (status != null && status.getRequest() != null && status.getRequest().getId() != null) {
            String id = toUniqueString(status.getRequest().getId());
            boolean serializable = JobUtils.isSerializable(status);

            Lock lock = this.locks.get(id);
            lock.lock();
            try {
                this.logger.debug("Store status [{}] in cache", id);

                this.cache.set(id, status);

                // Track pending writes for serializable statuses to prevent stale reads from disk during the
                // window between cache eviction and save completion.
                if (serializable) {
                    this.pendingWrites.put(id, status);
                }
            } finally {
                lock.unlock();
            }

            // Only store Serializable job status on the file system
            if (serializable) {
                if (async) {
                    this.executorService.execute(new JobStatusSerializerRunnable(status));
                } else {
                    saveJobStatus(status);
                }
            }
        }
    }

    protected void saveJobStatus(JobStatus status)
    {
        String id = toUniqueString(status.getRequest().getId());
        Lock lock = this.locks.get(id);
        lock.lock();
        try {
            this.persistentJobStatusStore.saveJobStatus(status);
        } catch (Exception e) {
            this.logger.warn("Failed to save job status for id {}", status.getRequest().getId(), e);
        } finally {
            // Use two-arg remove to only clear our own entry, not one from a newer store() call.
            this.pendingWrites.remove(id, status);
            lock.unlock();
        }
    }
}
