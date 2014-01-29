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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.event.status.JobStatus;

/**
 * Default implementation of {@link JobStatusStorage}.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultJobStatusStorage implements JobStatusStorage, Initializable
{
    /**
     * The name of the file where the job status is stored.
     */
    private static final String FILENAME_STATUS = "status.xml";

    /**
     * Encoding used for file content and names.
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * The encoded version of a <code>null</code> value in the id list.
     */
    private static final String FOLDER_NULL = "&null";

    /**
     * Used to get the storage directory.
     */
    @Inject
    private JobManagerConfiguration configuration;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    private JobStatusSerializer serializer;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.serializer = new JobStatusSerializer();

            repair();
        } catch (Exception e) {
            this.logger.error("Failed to load jobs", e);
        }
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
     */
    private void repair()
    {
        File folder = this.configuration.getStorage();

        if (folder.exists()) {
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
            } else if (file.getName().equals(FILENAME_STATUS)) {
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
            }
        }
    }

    private JobStatus loadStatus(List<String> id)
    {
        return loadStatus(getJobFolder(id));
    }

    /**
     * @param folder the folder from where to load the job status
     */
    private JobStatus loadStatus(File folder)
    {
        File statusFile = new File(folder, FILENAME_STATUS);
        if (statusFile.exists()) {
            return loadJobStatus(statusFile);
        }

        return null;
    }

    /**
     * @param statusFile the file containing job status to load
     * @return the job status
     * @throws Exception when failing to load the job status from the file
     */
    private JobStatus loadJobStatus(File statusFile)
    {
        return (JobStatus) this.serializer.read(statusFile);
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
            for (String idElement : id) {
                folder = new File(folder, encode(idElement));
            }
        }

        return folder;
    }

    /**
     * @param status the job status to save
     * @throws IOException when falling to store the provided status
     */
    private void saveJobStatus(JobStatus status) throws IOException
    {
        File statusFile = getJobFolder(status.getRequest().getId());
        statusFile = new File(statusFile, FILENAME_STATUS);

        this.serializer.write(status, statusFile);
    }

    @Override
    public JobStatus getJobStatus(String id)
    {
        return getJobStatus(id != null ? Arrays.asList(id) : (List<String>) null);
    }

    @Override
    public JobStatus getJobStatus(List<String> id)
    {
        try {
            return loadStatus(id);
        } catch (Exception e) {
            this.logger.warn("Failed to load job status for id [{}]", id, e);

            return null;
        }
    }

    @Override
    public void store(JobStatus status)
    {
        // On store Serializable job status on file system
        if (status instanceof Serializable) {
            try {
                saveJobStatus(status);
            } catch (Exception e) {
                this.logger.warn("Failed to save job status [{}]", status, e);
            }
        }
    }

    @Override
    public JobStatus remove(String id)
    {
        return remove(Arrays.asList(id));
    }

    @Override
    public JobStatus remove(List<String> id)
    {
        File jobFolder = getJobFolder(id);

        if (jobFolder.exists()) {
            JobStatus status = loadStatus(jobFolder);

            try {
                FileUtils.deleteDirectory(jobFolder);
            } catch (IOException e) {
                this.logger.warn("Failed to delete job folder [{}]", jobFolder, e);
            }

            return status;
        }

        return null;
    }
}
