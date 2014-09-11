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
package org.xwiki.extension;

/**
 * Provide informations related to extensions's Source Control Management.
 *
 * @version $Id$
 * @since 6.3M1
 */
public interface ExtensionScm
{
    /**
     * Get the source control management system connection that describes the repository and how to connect to the
     * repository. Usually read only.
     *
     * @return the connection
     */
    ExtensionScmConnection getConnection();

    /**
     * Same as {@link #getConnection()}, but for developers, i.e. this scm connection will not be read only.
     *
     * @return the connection
     */
    ExtensionScmConnection getDeveloperConnection();

    /**
     * Get the URL to the project's browsable SCM repository.
     *
     * @return the URL
     */
    String getUrl();
}
