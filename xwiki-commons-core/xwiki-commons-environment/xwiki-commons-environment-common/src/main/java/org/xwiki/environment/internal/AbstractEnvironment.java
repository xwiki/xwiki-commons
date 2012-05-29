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
            final String classSpecified = this.getTemporaryDirectoryName();
            final String configured = this.configurationProvider.get().getPermanentDirectoryPath();
            if (classSpecified == null && configured == null) {
                // There's no defined permanent directory,
                // fall back to the temporary directory but issue a warning
                this.logger.warn("No permanent directory configured. "
                                 + "Using a temporary directory [{}]",
                                 DEFAULT_TMP_DIRECTORY);
            }
            final String[] locations = new String[] {
                classSpecified,
                configured,
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
        final String tempOrPerminent = (isTemp) ? "temporary" : "permanent";
        boolean first = true;
        for (final String location : locations) {
            if (location == null) {
                continue;
            }
            if (!first) {
                this.logger.warn("Falling back on [{}] for {} directory.",
                                  location, tempOrPerminent);
            }
            first = false;
            final File dir = this.initializeDirectory(location, isTemp, tempOrPerminent);
            if (dir != null) {
                return dir;
            }
        }

        throw new RuntimeException("Could not find a writable "
                                   + tempOrPerminent + " directory. "
                                   + "Check the server log for more information.");
    }

    /**
     * @param directoryName the name of the directory to initialize (ensure it exists, create the
     *                      directory)
     * @param isTemp true if we are initializing a temporary directory.
     * @param tempOrPerminent a string describing the type of directory,
     *                        namely "temporary" or "permanent", to aid logging.
     * @return the initialized directory as a {@link File} or null if the directory doesn't exist
     *         and cannot be created or if the process doesn't have permission to write to it.
     */
    private File initializeDirectory(final String directoryName,
                                     final boolean isTemp,
                                     final String tempOrPerminent)
    {
        final File dir = (isTemp) ? new File(directoryName, TEMP_NAME) : new File(directoryName);

        if (dir.exists()) {
            if (dir.isDirectory() && dir.canWrite()) {
                try {
                    if (isTemp) {
                        this.initTempDir(dir);
                    }
                    return dir;
                } catch (IOException e) {
                    // Will be logged below.
                }
            }
            final String[] params = new String[] {
                tempOrPerminent,
                dir.getAbsolutePath(),
                (dir.isDirectory()) ? "not writable" : "not a directory"
            };
            this.logger.error("Configured {} directory [{}] is {}.", params);
            return null;
        } else if (dir.mkdirs()) {
            return dir;
        }
        this.logger.error("Configured {} directory [{}] could not be created, check permissions.",
                          tempOrPerminent, dir.getAbsolutePath());
        return null;
    }

    /**
     * Initialize the internal xwiki-temp directory.
     * This function clears the directory out.
     *
     * @param tempDir the xwiki-temp subdirectory which is internal/deleted per load.
     * @throws IOException if something goes wrong trying to clear the directory.
     * @throws RuntimeException if the configuration is "silly" and puts the persistent dir
     *                          inside of the delete-on-start directory.
     */
    private void initTempDir(final File tempDir) throws IOException
    {
        // We can't prevent all bad configurations eg: persistent dir == /dev/null
        // But setting the persistent dir to the xwiki-temp subdir is easy enough to catch.
        final File permDir = this.getPermanentDirectory();
        if (tempDir.equals(permDir) || FileUtils.directoryContains(tempDir, permDir)) {
            throw new RuntimeException(
                "The configured persistent store directory falls within the "
                + TEMP_NAME + " sub-directory of the temporary directory, this "
                + "sub-directory is reserved (deleted on start-up) and must never "
                + "be used. Please review your configuration.");
        }

        FileUtils.cleanDirectory(tempDir);
    }
}
