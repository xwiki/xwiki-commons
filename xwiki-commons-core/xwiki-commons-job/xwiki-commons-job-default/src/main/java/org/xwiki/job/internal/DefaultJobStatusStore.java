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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.LRUCacheConfiguration;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.job.AbstractJobStatus;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.JobStatusStore;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.tail.LoggerTail;

/**
 * Default implementation of {@link JobStatusStorage}.
 *
 * @version $Id$
 * @since 6.1M2
 */
@Component
@Singleton
public class DefaultJobStatusStore implements JobStatusStore, Initializable
{
    /**
     * The current version of the store. Should be upgraded if any change is made.
     */
    private static final int VERSION = 1;

    /**
     * The name of the file where the job status is stored as XML.
     */
    private static final String FILENAME_STATUS_XML = "status.xml";

    /**
     * The name of the file where the job status is ZIPPED.
     */
    private static final String FILENAME_STATUS_ZIP = FILENAME_STATUS_XML + ".zip";

    /**
     * The name of the file where various information about the status store are stored (like the version of the store).
     */
    private static final String INDEX_FILE = "store.properties";

    /**
     * The name of the property containing the version of the store.
     */
    private static final String INDEX_FILE_VERSION = "version";

    /**
     * Encoding used for file content and names.
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The encoded version of a <code>null</code> value in the id list.
     */
    private static final String FOLDER_NULL = "&null";

    private static final String STATUS_LOG_PREFIX = "log";

    private static final JobStatus NOSTATUS = new DefaultJobStatus<>(null, null, null, null, null);

    /**
     * Used to get the storage directory.
     */
    @Inject
    private JobManagerConfiguration configuration;

    @Inject
    private CacheManager cacheManager;

    @Inject
    private LoggerManager loggerManager;

    @Inject
    private JobStatusSerializer serializer;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final ReadLock readLock = lock.readLock();

    private final WriteLock writeLock = lock.writeLock();

    private ExecutorService executorService;

    private Cache<JobStatus> cache;

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
        try {
            // Check if the store need to be upgraded
            File folder = this.configuration.getStorage();
            File file = new File(folder, INDEX_FILE);

            FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class, null, true)
                    .configure(new Parameters().properties().setFile(file));
            PropertiesConfiguration properties = builder.getConfiguration();
            int version = properties.getInt(INDEX_FILE_VERSION, 0);
            if (VERSION > version) {
                repair();

                // Update version
                properties.setProperty(INDEX_FILE_VERSION, VERSION);
                builder.save();
            }
        } catch (Exception e) {
            this.logger.error("Failed to load jobs", e);
        }

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

    private String toUniqueString(List<String> id)
    {
        return StringUtils.join(id, '/');
    }

    /**
     * @param name the file or directory name to encode
     * @return the encoding name
     */
    private String encode(String name)
    {
        String encoded;

        if (name != null) {
            try {
                encoded = URLEncoder.encode(name, DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                // Should never happen

                encoded = name;
            }
        } else {
            encoded = FOLDER_NULL;
        }

        return encoded;
    }

    /**
     * Load jobs from directory.
     * 
     * @throws IOException when failing to load statuses
     */
    private void repair() throws IOException
    {
        File folder = this.configuration.getStorage();

        if (folder.exists()) {
            if (!folder.isDirectory()) {
                throw new IOException("Not a directory: " + folder);
            }

            repairFolder(folder);
        }
    }

    /**
     * @param folder the folder from where to load the jobs
     */
    private void repairFolder(File folder)
    {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                repairFolder(file);
            } else if (file.getName().equals(FILENAME_STATUS_ZIP) || file.getName().equals(FILENAME_STATUS_XML)) {
                try {
                    JobStatus status = loadStatus(folder);

                    if (status != null) {
                        File properFolder = getJobFolder(status.getRequest().getId());

                        if (!folder.equals(properFolder)) {
                            // Move the status in its right place
                            try {
                                FileUtils.moveFileToDirectory(file, properFolder, true);
                            } catch (IOException e) {
                                this.logger.error("Failed to move job status file", e);
                            }
                        }
                    }
                } catch (Exception e) {
                    this.logger.warn("Failed to load job status in folder [{}]", folder, e);
                }
            }
        }
    }

    private JobStatus loadStatus(List<String> id) throws IOException
    {
        return loadStatus(getJobFolder(id));
    }

    /**
     * @param folder the folder from where to load the job status
     * @throws IOException when failing to load the status file
     */
    private JobStatus loadStatus(File folder) throws IOException
    {
        this.readLock.lock();

        try {
            // First try as ZIP
            File statusFile = new File(folder, FILENAME_STATUS_ZIP);
            if (!statusFile.exists()) {
                // Then try as XML
                statusFile = new File(folder, FILENAME_STATUS_XML);
            }
            if (statusFile.exists()) {
                JobStatus status = loadJobStatus(statusFile);

                // Check if there is a separated log available
                for (File child : folder.listFiles()) {
                    if (!child.isDirectory() && child.getName().startsWith(STATUS_LOG_PREFIX)) {
                        try {
                            LoggerTail loggerTail = createLoggerTail(new File(folder, STATUS_LOG_PREFIX), true);

                            if (status instanceof AbstractJobStatus) {
                                ((AbstractJobStatus) status).setLoggerTail(loggerTail);
                            }
                        } catch (Exception e) {
                            this.logger.error("Failed to load the job status log in [{}]", folder, e);
                        }

                        break;
                    }
                }

                return status;
            }
        } finally {
            this.readLock.unlock();
        }

        return null;
    }

    /**
     * @param statusFile the file containing job status to load
     * @return the job status
     * @throws IOException when failing to load the job status from the file
     */
    private JobStatus loadJobStatus(File statusFile) throws IOException
    {
        return this.serializer.read(statusFile);
    }

    // JobStatusStorage

    /**
     * @param id the id of the job
     * @return the folder where to store the job related informations
     */
    private File getJobFolder(List<String> id)
    {
        File folder = this.configuration.getStorage();

        if (id != null) {
            // Create a different folder for each element
            for (String fullIdElement : id) {
                // But cut each element is it's bigger than 255 bytes (and not characters) since it's a very common
                // limit for a single element of the path among file systems
                // To be sure to deal with characters not taking more than 1 byte, we start by encoding it in base 64
                String encodedIdElement = Base64.encodeBase64String(fullIdElement.getBytes());
                if (encodedIdElement.length() > 255) {
                    do {
                        folder = new File(folder, encode(encodedIdElement.substring(0, 255)));
                        encodedIdElement = encodedIdElement.substring(255);
                    } while (encodedIdElement.length() > 255);
                } else {
                    folder = new File(folder, encode(encodedIdElement));
                }
            }
        }

        return folder;
    }

    private File getJobLogBaseFile(List<String> id)
    {
        return new File(getJobFolder(id), STATUS_LOG_PREFIX);
    }

    /**
     * @param status the job status to save
     */
    private void saveJobStatus(JobStatus status)
    {
        try {
            this.writeLock.lock();

            try {
                File statusFile = getJobFolder(status.getRequest().getId());
                statusFile = new File(statusFile, FILENAME_STATUS_ZIP);

                this.logger.debug("Serializing status [{}] in [{}]", status.getRequest().getId(), statusFile);

                this.serializer.write(status, statusFile);
            } finally {
                this.writeLock.unlock();
            }
        } catch (Exception e) {
            this.logger.warn("Failed to save job status [{}]", status, e);
        }
    }

    @Override
    public JobStatus getJobStatus(List<String> id)
    {
        String idString = toUniqueString(id);

        JobStatus status = this.cache.get(idString);

        if (status == null) {
            status = maybeLoadStatus(id, idString);
        }

        return status == NOSTATUS ? null : status;
    }

    private synchronized JobStatus maybeLoadStatus(List<String> id, String idString)
    {
        JobStatus status = this.cache.get(idString);

        if (status == null) {
            try {
                status = loadStatus(id);

                this.cache.set(idString, status);
            } catch (Exception e) {
                this.logger.warn("Failed to load job status for id {}", id, e);

                this.cache.remove(idString);
            }
        }

        return status;
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

    private void store(JobStatus status, boolean async)
    {
        if (status != null && status.getRequest() != null && status.getRequest().getId() != null) {
            synchronized (this.cache) {
                String id = toUniqueString(status.getRequest().getId());

                this.logger.debug("Store status [{}] in cache", id);

                this.cache.set(id, status);
            }

            // Only store Serializable job status on file system
            if (JobUtils.isSerializable(status)) {
                if (async) {
                    this.executorService.execute(new JobStatusSerializerRunnable(status));
                } else {
                    saveJobStatus(status);
                }
            }
        }
    }

    @Override
    public void remove(List<String> id)
    {
        this.writeLock.lock();

        try {
            File jobFolder = getJobFolder(id);

            if (jobFolder.exists()) {
                try {
                    FileUtils.deleteDirectory(jobFolder);
                } catch (IOException e) {
                    this.logger.warn("Failed to delete job folder [{}]", jobFolder, e);
                }
            }

            this.cache.remove(toUniqueString(id));
        } finally {
            this.writeLock.unlock();
        }
    }

    @Override
    public LoggerTail createLoggerTail(List<String> jobId, boolean readonly)
    {
        if (jobId != null) {
            try {
                return createLoggerTail(getJobLogBaseFile(jobId), readonly);
            } catch (Exception e) {
                this.logger.error("Failed to create a logger tail for job [{}]", jobId, e);
            }
        }

        return new LogQueue();
    }

    private LoggerTail createLoggerTail(File logBaseFile, boolean readonly) throws IOException
    {
        return this.loggerManager.createLoggerTail(logBaseFile.toPath(), readonly);
    }

    /**
     * Remove all elements from the cache.
     */
    public void flushCache()
    {
        this.cache.removeAll();
    }
}
