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
package org.xwiki.extension.handler;

import org.xwiki.component.annotation.Role;
import org.xwiki.extension.Extension;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.job.Request;

/**
 * Check if executing a given action on a passed extension is allowed.
 * <p>
 * Mostly used for default behavior when {@link ExtensionHandler} does not have any special check for its type. The goal
 * is to make easy for any environment using commons-extension module to have a default restriction on all extensions
 * types (for example in XWiki unless the type has special checking you need programming right to install an extension).
 *
 * @version $Id$
 * @since 4.2M2
 */
@Role
public interface ExtensionValidator
{
    /**
     * Check if installing the passed extension is allowed.
     * <p>
     * It is generally used to do some non generic checking of whether or not it is possible to install the passed
     * extension (not the right environment, not enough rights, etc.).
     *
     * @param extension the extension to install
     * @param namespace the namespace from where to install
     * @param request extra parameters
     * @throws InstallException installing the extension will fail
     */
    void checkInstall(Extension extension, String namespace, Request request) throws InstallException;

    /**
     * Check if uninstalling the passed extension is allowed.
     * <p>
     * It is generally used to do some non generic checking of whether or not it is possible to uninstall the passed
     * extension (not the right environment, not enough rights, etc.).
     *
     * @param extension the extension to uninstall
     * @param namespace the namespace from where to uninstall
     * @param request extra parameters
     * @throws UninstallException uninstalling the extension will fail
     */
    void checkUninstall(InstalledExtension extension, String namespace, Request request) throws UninstallException;
}
