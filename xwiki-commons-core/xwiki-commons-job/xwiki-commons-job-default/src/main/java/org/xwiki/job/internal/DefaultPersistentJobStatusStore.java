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
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.job.AbstractJobStatus;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.JobStatusStore;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.logging.LogQueue;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.tail.LoggerTail;

/**
 * Default implementation of {@link JobStatusStore}.
 *
 * @version $Id$
 * @since 18.1.0RC1
 */
@Component
@Singleton
public class DefaultPersistentJobStatusStore implements PersistentJobStatusStore, Initializable
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

    private static final String STATUS_LOG_PREFIX = "log";

    @Inject
    private LoggerManager loggerManager;

    @Inject
    private JobStatusSerializer serializer;

    @Inject
    private List<JobStatusFolderResolver> folderResolvers;

    @Inject
    private JobManagerConfiguration configuration;

    @Inject
    private Logger logger;

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
                        File properFolder = getAndMoveJobFolder(status.getRequest().getId(), false);

                        if (!folder.equals(properFolder)) {
                            moveJobStatus(folder, file, properFolder);
                        }
                    }
                } catch (Exception e) {
                    this.logger.warn("Failed to load job status in folder [{}]", folder, e);
                }
            }
        }
    }

    private void moveJobStatus(File sourceDirectory, File statusFile, File targetDirectory)
    {
        try {
            // Check if the target already exists.
            File targetStatusZip = new File(targetDirectory, FILENAME_STATUS_ZIP);
            File targetStatus = new File(targetDirectory, FILENAME_STATUS_XML);

            // Compare the last modified times of the source and the target status file.
            long sourceLastModified = statusFile.lastModified();
            // The target last modified time will be 0 if the target status file does not exist.
            long targetLastModified = Math.max(targetStatusZip.lastModified(), targetStatus.lastModified());

            if (sourceLastModified > targetLastModified) {
                // Source is newer (or target doesn't exist), overwrite the target.
                if (targetDirectory.isDirectory()) {
                    deleteJobStatusFiles(targetDirectory);
                }

                // Move the status in its right place
                FileUtils.moveFileToDirectory(statusFile, targetDirectory, true);
                // Move the job log, too.
                for (File file : sourceDirectory.listFiles()) {
                    if (!file.isDirectory() && file.getName().startsWith(STATUS_LOG_PREFIX)) {
                        FileUtils.moveFileToDirectory(file, targetDirectory, true);
                    }
                }
            } else {
                // Target is more recent, remove the source.
                deleteJobStatusFiles(sourceDirectory);
            }

            // Remove the source directory and its parents if they are empty now.
            cleanEmptyDirectories(sourceDirectory);
        } catch (IOException e) {
            this.logger.error("Failed to move job status and log files, and cleaning up", e);
        }
    }

    private void cleanEmptyDirectories(File sourceDirectory) throws IOException
    {
        Path storagePath = this.configuration.getStorage().toPath();
        for (Path sourcePath = sourceDirectory.toPath();
            !Objects.equals(storagePath, sourcePath) && sourcePath != null && isDirEmpty(sourcePath);
            sourcePath = sourcePath.getParent()) {
            Files.delete(sourcePath);
        }
    }

    private static boolean isDirEmpty(Path directory) throws IOException
    {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    /**
     * @param folder the folder from where to load the job status
     * @throws IOException when failing to load the status file
     */
    private JobStatus loadStatus(File folder) throws IOException
    {
        // First try as ZIP
        File statusFile = getStatusFile(folder);
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

        return null;
    }

    private static File getStatusFile(File folder)
    {
        File statusFile = new File(folder, FILENAME_STATUS_ZIP);
        if (!statusFile.exists()) {
            // Then try as XML
            statusFile = new File(folder, FILENAME_STATUS_XML);
        }
        return statusFile;
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
     * @param moveToCurrent if the job status should be moved from a previous to the current location
     * @return the folder where to store the job related informations
     */
    private File getAndMoveJobFolder(List<String> id, boolean moveToCurrent)
    {
        JobStatusFolderResolver currentResolver = this.folderResolvers.get(0);

        File folder = currentResolver.getFolder(id);

        if (moveToCurrent && !getStatusFile(folder).exists()) {
            File previousFolder = null;
            File previousStatusFile = null;
            for (JobStatusFolderResolver folderResolver
                : this.folderResolvers.subList(1, this.folderResolvers.size())) {
                File f = folderResolver.getFolder(id);
                File s = getStatusFile(f);
                if (s.exists()) {
                    previousFolder = f;
                    previousStatusFile = s;
                    break;
                }
            }

            if (previousFolder != null) {
                moveJobStatus(previousFolder, previousStatusFile, folder);
            }
        }

        return folder;
    }

    private File getJobLogBaseFile(List<String> id)
    {
        return new File(getAndMoveJobFolder(id, true), STATUS_LOG_PREFIX);
    }

    /**
     * @param status the job status to save
     */
    @Override
    public void saveJobStatusWithLock(JobStatus status) throws IOException
    {
        if (!JobUtils.isSerializable(status)) {
            return;
        }

        File statusFile = getAndMoveJobFolder(status.getRequest().getId(), true);
        statusFile = new File(statusFile, FILENAME_STATUS_ZIP);

        this.logger.debug("Serializing status [{}] in [{}]", status.getRequest().getId(), statusFile);

        this.serializer.write(status, statusFile);
    }

    @Override
    public synchronized JobStatus loadJobStatusWithLock(List<String> id) throws IOException
    {
        return loadStatus(getAndMoveJobFolder(id, true));
    }

    @Override
    public void removeJobStatusWithLock(List<String> id)
    {
        // Delete the job status from all possible locations to ensure that when loading it again, it indeed
        // cannot be found anymore.
        for (JobStatusFolderResolver folderResolver : this.folderResolvers) {
            File jobFolder = folderResolver.getFolder(id);

            if (jobFolder.isDirectory()) {
                try {
                    deleteJobStatusFiles(jobFolder);
                    cleanEmptyDirectories(jobFolder);
                } catch (IOException e) {
                    this.logger.warn("Failed to delete job folder [{}]", jobFolder, e);
                }
            }
        }
    }

    private static void deleteJobStatusFiles(File jobFolder) throws IOException
    {
        for (File file : jobFolder.listFiles()) {
            if (!file.isDirectory() && (file.getName().startsWith(STATUS_LOG_PREFIX)
                || file.getName().startsWith(FILENAME_STATUS_XML)))
            {
                Files.delete(file.toPath());
            }
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
}
