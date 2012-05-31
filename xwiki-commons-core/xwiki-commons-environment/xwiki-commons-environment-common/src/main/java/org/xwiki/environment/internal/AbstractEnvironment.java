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
package org.xwiki.environment.internal;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.xwiki.environment.Environment;

/**
 * Makes it easy to implement {@link org.xwiki.environment.Environment}.
 *
 * @version $Id$
 * @since 3.5M1
 */
public abstract class AbstractEnvironment implements Environment
{
    /**
     * Default temporary directory to use when none has been specified.
     */
    private static final String DEFAULT_TMP_DIRECTORY = System.getProperty("java.io.tmpdir");

    /** The name of the temporary directory which will be cleaned every restart. */
    private static final String TEMP_NAME = "xwiki-temp";

    /**
     * The logger to log.
     */
    @Inject
    protected Logger logger;

    /**
     * Environment configuration data. We load it lazily to avoid a cyclic runtime dependency between the Environment
     * implementation (which requires a Configuration Source) and the Configuration Source implementation (which
     * requires an Environment).
     */
    @Inject
    private Provider<EnvironmentConfiguration> configurationProvider;

    /**
     * @see #getTemporaryDirectory()
     */
    private File temporaryDirectory;

    /**
     * @see #getPermanentDirectory()
     */
    private File permanentDirectory;

    /**
     * @param permanentDirectory see {@link #getPermanentDirectory()}
     */
    public void setPermanentDirectory(File permanentDirectory)
    {
        this.permanentDirectory = permanentDirectory;
    }

    /**
     * @param temporaryDirectory see {@link #getTemporaryDirectory()}
     */
    public void setTemporaryDirectory(File temporaryDirectory)
    {
        this.temporaryDirectory = temporaryDirectory;
    }

    /* Rather than overriding this, it is safer to override getPermanentDirectoryName() */
    @Override
    public File getPermanentDirectory()
    {
        if (this.permanentDirectory == null) {
            final String classSpecified = this.getPermanentDirectoryName();
            final String configured = this.configurationProvider.get().getPermanentDirectoryPath();
            if (classSpecified == null && configured == null) {
                // There's no defined permanent directory,
                // fall back to the temporary directory but issue a warning
                this.logger.warn("No permanent directory configured. "
                                 + "Using a temporary directory.");
            }
            final String[] locations = new String[] {
                classSpecified,
                configured,
                this.getTemporaryDirectoryName(),
                DEFAULT_TMP_DIRECTORY
            };
            this.permanentDirectory = this.initializeDirectory(locations, false);
        }
        return this.permanentDirectory;
    }

    /**
     * @return the permanent directory as specified
     */
    protected String getPermanentDirectoryName()
    {
        return null;
    }

    /* Rather than overriding this, it is safer to override getTemporaryDirectoryName() */
    @Override
    public File getTemporaryDirectory()
    {
        if (this.temporaryDirectory == null) {
            final String[] locations = new String[] {
                this.getTemporaryDirectoryName(),
                DEFAULT_TMP_DIRECTORY
            };
            this.temporaryDirectory = this.initializeDirectory(locations, true);
        }
        return this.temporaryDirectory;
    }

    /**
     * @return the temporary directory as specified
     */
    protected String getTemporaryDirectoryName()
    {
        return null;
    }

    /**
     * @param locations the names of the directories to try to initialize ordered from best to worst.
     *                  If none of these can be initialized, the system will be halted.
     * @param isTemp true if the directory is a temporary directory.
     * @return the initialized directory as a {@link File} or null if the directory doesn't exist,
     *         cannot be created or the passed name was null
     */
    private File initializeDirectory(final String[] locations, final boolean isTemp)
    {
        final String tempOrPermanent = (isTemp) ? "temporary" : "permanent";
        boolean first = true;
        for (final String location : locations) {
            if (location == null) {
                continue;
            }
            if (!first) {
                this.logger.warn("Falling back on [{}] for {} directory.",
                                  location, tempOrPermanent);
            }
            first = false;
            final File dir = this.initializeDirectory(location, isTemp, tempOrPermanent);
            if (dir != null) {
                return dir;
            }
        }

        throw new RuntimeException(
            String.format("Could not find a writable [%s] directory. "
                          + "Check the server log for more information.", tempOrPermanent));
    }

    /**
     * @param directoryName the name of the directory to initialize (ensure it exists, create the
     *                      directory)
     * @param isTemp true if we are initializing a temporary directory.
     * @param tempOrPermanent a string describing the type of directory,
     *                        namely "temporary" or "permanent", to aid logging.
     * @return the initialized directory as a {@link File} or null if the directory doesn't exist
     *         and cannot be created or if the process doesn't have permission to write to it.
     */
    private File initializeDirectory(final String directoryName,
                                     final boolean isTemp,
                                     final String tempOrPermanent)
    {
        final File dir = (isTemp) ? new File(directoryName, TEMP_NAME) : new File(directoryName);

        if (dir.exists()) {
            if (dir.isDirectory() && dir.canWrite()) {
                return this.initDir(dir, isTemp);
            }

            // Not a directory or can't write to it, lets log an error here.
            final String[] params = new String[] {
                tempOrPermanent,
                dir.getAbsolutePath(),
                (dir.isDirectory()) ? "not writable" : "not a directory"
            };
            this.logger.error("Configured {} directory [{}] is {}.", params);
            return null;

        } else if (dir.mkdirs()) {
            return this.initDir(dir, isTemp);
        }
        this.logger.error("Configured {} directory [{}] could not be created, check permissions.",
                          tempOrPermanent, dir.getAbsolutePath());
        return null;
    }

    /**
     * Initialize temporary or permanent directory for use.
     *
     * @param directory the directory to initialize.
     * @param isTemp true if it is a temporary directory.
     * @return the newly initialized directory.
     */
    private File initDir(final File directory, final boolean isTemp)
    {
        if (isTemp) {
            this.initTempDir(directory);
        }
        return directory;
    }

    /**
     * Initialize the internal xwiki-temp directory.
     * This function clears the directory out.
     *
     * @param tempDir the xwiki-temp subdirectory which is internal/deleted per load.
     */
    private void initTempDir(final File tempDir)
    {
        // We can't prevent all bad configurations eg: persistent dir == /dev/null
        // But setting the persistent dir to the xwiki-temp subdir is easy enough to catch.
        final File permDir = this.getPermanentDirectory();
        try {
            if (tempDir.equals(permDir) || FileUtils.directoryContains(tempDir, permDir)) {
                throw new RuntimeException(
                    "The configured persistent store directory falls within the "
                    + TEMP_NAME + " sub-directory of the temporary directory, this "
                    + "sub-directory is reserved (deleted on start-up) and must never "
                    + "be used. Please review your configuration.");
            }
        } catch (IOException e) {
            // Shouldn't happen since these directories were already verified to be writable.
            throw new RuntimeException("Failure when checking if configured permanent store "
                                       + "directory is subdirectory of temporary directory.");
        }

        try {
            FileUtils.cleanDirectory(tempDir);
        } catch (IOException e) {
            throw new RuntimeException(
                String.format("Failed to empty the temporary directory [%s], are their files "
                              + "inside of it which xwiki does not have permission to delete?",
                              tempDir.getAbsolutePath()));
        }
    }
}
