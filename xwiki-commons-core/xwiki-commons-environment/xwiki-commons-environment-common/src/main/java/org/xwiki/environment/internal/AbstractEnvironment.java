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
import java.nio.file.Files;

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

    /**
     * The name of the temporary directory which will be cleaned at every restart.
     */
    private static final String TEMP_NAME = "xwiki-temp";

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

    /**
     * {@inheritDoc}
     * <p>
     * Rather than overriding this, it is safer to override {@link #getPermanentDirectoryName()} if you need to change
     * the default behavior. This is because this method does a number of checks to make sure the directory exists, is a
     * directory (not a file) and the XWiki process has permission to write to it. If the directory doesn't exist it is
     * created and if it cannot be written to, an error is printed in the log and it is passed over for the default
     * permanent directory. Thus by overriding {@link #getPermanentDirectoryName()} you'll still benefit from all those
     * checks.
     * </p>
     */
    @Override
    public File getPermanentDirectory()
    {
        // Note: We're initializing the permanent directory here instead of in an Initializable.initialize() method
        // since otherwise we get a cyclic dependency with the Configuration Source implementation used to get the
        // Environment configuration property for the permanent directory location (since that Source require the
        // Environment for finding the configuration resource in the executing Environment).
        if (this.permanentDirectory == null) {
            String systemProperty = System.getProperty("xwiki.data.dir");
            final String classSpecified = getPermanentDirectoryName();
            final String configured = this.configurationProvider.get().getPermanentDirectoryPath();

            final String[] locations =
                new String[] { systemProperty, classSpecified, configured, getTemporaryDirectoryName(),
                    DEFAULT_TMP_DIRECTORY };
            this.permanentDirectory = initializeDirectory(locations, false);

            if (systemProperty == null && classSpecified == null && configured == null) {
                // There's no defined permanent directory, fall back to the temporary directory but issue a warning
                this.logger.warn("No permanent directory configured, fallbacking to temporary directory. "
                    + "You should set the \"environment.permanentDirectory\" configuration property in the "
                    + "xwiki.properties file.");
            }
            this.logger.info("Using permanent directory [{}]", this.permanentDirectory);
        }

        return this.permanentDirectory;
    }

    /**
     * Get the name of the permanent directory to use. This name will be preferred when choosing the permanent directory
     * and if it is not able to be written to, this class will fail over to the default directory after printing an
     * error in the log file.
     *
     * @return the permanent directory as specified
     */
    protected String getPermanentDirectoryName()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Rather than overriding this, it is safer to override {@link #getTemporaryDirectoryName()} This is
     * because this function does a number of checks to make sure the directory exists, is a directory (not a file) and
     * the XWiki process has permission to write to it. If the directory doesn't exist it is created and if it cannot be
     * written to, an error is printed in the log and it is passed over for the default temporary directory.
     * </p>
     *
     * @see Environment#getTemporaryDirectory()
     */
    @Override
    public File getTemporaryDirectory()
    {
        if (this.temporaryDirectory == null) {
            final String[] locations = new String[] { getTemporaryDirectoryName(), DEFAULT_TMP_DIRECTORY };
            this.temporaryDirectory = initializeDirectory(locations, true);
        }

        return this.temporaryDirectory;
    }

    /**
     * Get the name of the temporary directory to use. The path given name will be preferred when choosing the temporary
     * directory and if it is not able to be written to, this class will fail over to the default directory after
     * printing an error in the log file.
     *
     * @return the temporary directory as specified
     */
    protected String getTemporaryDirectoryName()
    {
        return null;
    }

    /**
     * @param locations the names of the directories to try to initialize ordered from best to worst. If none of these
     *            can be initialized, the system will be halted.
     * @param isTemp true if the directory is a temporary directory.
     * @return the initialized directory as a {@link File} or null if the directory doesn't exist, cannot be created or
     *         the passed name was null
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
                this.logger.warn("Falling back on [{}] as the {} directory.", location, tempOrPermanent);
            }
            first = false;
            final File dir = initializeDirectory(location, isTemp, tempOrPermanent);
            if (dir != null) {
                return dir;
            }
        }

        throw new RuntimeException(String.format(
            "Could not find a writable %s directory. Check the server logs for more information.", tempOrPermanent));
    }

    /**
     * @param directoryName the name of the directory to initialize (ensure it exists, create the directory)
     * @param isTemp true if we are initializing a temporary directory.
     * @param tempOrPermanent a string describing the type of directory, namely "temporary" or "permanent", to aid
     *            logging.
     * @return the initialized directory as a {@link File} or null if the directory doesn't exist and cannot be created
     *         or if the process doesn't have permission to write to it.
     */
    private File initializeDirectory(final String directoryName, final boolean isTemp, final String tempOrPermanent)
    {
        final File dir = (isTemp) ? new File(directoryName, TEMP_NAME) : new File(directoryName);

        if (dir.exists()) {
            if (dir.isDirectory() && dir.canWrite()) {
                return initDir(dir, isTemp);
            }

            // Not a directory or can't write to it, lets log an error here.
            this.logger.error("Configured {} directory [{}] is {}.", tempOrPermanent, dir.getAbsolutePath(),
                (dir.isDirectory()) ? "not writable" : "not a directory");

            return null;

        }

        try {
            Files.createDirectories(dir.toPath());

            return initDir(dir, isTemp);
        } catch (IOException e) {
            this.logger.error("Configured {} directory [{}] could not be created.", tempOrPermanent,
                dir.getAbsolutePath(), e);
        }

        return null;
    }

    private File initDir(final File directory, final boolean isTemp)
    {
        if (isTemp) {
            try {
                FileUtils.cleanDirectory(directory);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to empty the temporary directory [%s]. "
                    + "Are there files inside of it which XWiki " + "does not have permission to delete?",
                    directory.getAbsolutePath()), e);
            }
        }

        return directory;
    }
}
