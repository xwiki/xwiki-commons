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
package org.xwiki.extension.versioncheck;

import org.xwiki.component.annotation.Role;
import org.xwiki.extension.version.Version;

/**
 * Provide configuration options for the extension version checker.
 *
 * @version $Id$
 * @since 9.9RC1
 */
@Role
public interface ExtensionVersionCheckConfiguration
{
    /**
     * @return true if updates of the environment extension should be checked
     */
    boolean isEnvironmentCheckEnabled();

    /**
     * @return the number of seconds between each check for a new environment version
     */
    long environmentCheckInterval();

    /**
     * Determine if new environment versions should match the {@link Version.Type} of the currently installed
     * environment extension.
     * If false is returned, {@link #environmentVersionType()} should be used to get the {@link Version.Type} to use.
     *
     * @return true if the new environment version type should be the same as the currently installed environment
     */
    boolean useInstalledEnvironmentVersionType();

    /**
     * @return true if more stable environment versions should be checked. This should only used if
     * {@link #useInstalledEnvironmentVersionType()} returns false.
     */
    boolean checkMoreStableEnvironments();

    /**
     * @return the {@link Version.Type} to use while checking for new environment extensions. This should only be
     * used if {@link #useInstalledEnvironmentVersionType()} returns false.
     */
    Version.Type environmentVersionType();
}
