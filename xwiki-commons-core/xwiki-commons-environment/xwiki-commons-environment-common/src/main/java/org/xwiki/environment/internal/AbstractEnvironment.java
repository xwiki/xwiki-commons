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

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.environment.Environment;
import org.xwiki.environment.EnvironmentConfiguration;

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
    private static final File DEFAULT_TMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));

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

    @Override
    public File getPermanentDirectory()
    {
        if (this.permanentDirectory == null) {
            // The permanent directory hasn't been set by the user, try to find a configuration for it.
            this.permanentDirectory = this.configurationProvider.get().getPermanentDirectory();
            if (this.permanentDirectory == null) {
                this.permanentDirectory = getTemporaryDirectory();
                // There's no defined permanent directory, fall back to the temporary directory but issue a warning
                this.logger.warn("No permanent directory configured. Using a temporary directory [{}]",
                    this.permanentDirectory);
            }
        }
        return this.permanentDirectory;
    }

    @Override
    public File getTemporaryDirectory()
    {
        File tmpDirectory;
        if (getTemporaryDirectoryInternal() == null) {
            tmpDirectory = DEFAULT_TMP_DIRECTORY;
        } else {
            tmpDirectory = getTemporaryDirectoryInternal();
        }
        return tmpDirectory;
    }

    /**
     * @return the temporary directory as specified
     */
    protected File getTemporaryDirectoryInternal()
    {
        return this.temporaryDirectory;
    }
}
