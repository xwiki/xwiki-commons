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
import org.xwiki.extension.ExtensionId;
import org.xwiki.stability.Unstable;

/**
 * Define a component used to retrieve the last available version of a given extension
 * and compare it to the installed version.
 *
 * @since 9.9-RC1
 * @version $Id$
 */
@Role
@Unstable
public interface ExtensionVersionChecker
{
    /**
     * Ensure that the given extension is in its last available version.
     *
     * @param extensionId the ID of the extension to check
     * @return true if the extension is up to date
     * @throws ExtensionVersionCheckException if an error happened
     */
    boolean isUpToDate(ExtensionId extensionId) throws ExtensionVersionCheckException;
}
