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

import java.util.Collection;

import org.xwiki.component.annotation.Role;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.job.Request;

/**
 * Handle extension related tasks depending of the type (install, uninstall, etc...).
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Role
public interface ExtensionHandler
{
    /**
     * Install the provided local extension.
     * 
     * @param localExtension the extension to install
     * @param namespace the namespace where to install the extension
     * @param request extra parameters
     * @throws InstallException error when trying to install the extension
     */
    void install(LocalExtension localExtension, String namespace, Request request) throws InstallException;

    /**
     * Uninstall the provided local extension.
     * 
     * @param localExtension the extension to uninstall
     * @param namespace the namespace from where to uninstall the extension
     * @param request extra parameters
     * @throws UninstallException error when trying to uninstall the extension
     * @deprecated starting with 5.ORC1 use {@link #uninstall(InstalledExtension, String, Request)} instead
     */
    @Deprecated
    void uninstall(LocalExtension localExtension, String namespace, Request request) throws UninstallException;

    /**
     * Uninstall the provided local extension.
     * 
     * @param localExtension the extension to uninstall
     * @param namespace the namespace from where to uninstall the extension
     * @param request extra parameters
     * @throws UninstallException error when trying to uninstall the extension
     * @since 5.0RC1
     */
    void uninstall(InstalledExtension localExtension, String namespace, Request request) throws UninstallException;

    /**
     * Upgrade the provided local extension.
     * 
     * @param previousLocalExtension the previous installed version of the extension
     * @param newLocalExtension the extension to install
     * @param namespace the namespace from where to uninstall the extension
     * @param request extra parameters
     * @throws InstallException error when trying to upgrade the extension
     * @deprecated starting with 5.0RC1 use {@link #upgrade(Collection, LocalExtension, String, Request)}
     */
    @Deprecated
    void upgrade(LocalExtension previousLocalExtension, LocalExtension newLocalExtension, String namespace,
        Request request) throws InstallException;

    /**
     * Upgrade the provided local extension.
     * 
     * @param previousLocalExtensions the previous installed versions of the extension
     * @param newLocalExtension the extension to install
     * @param namespace the namespace from where to uninstall the extension
     * @param request extra parameters
     * @throws InstallException error when trying to upgrade the extension
     * @since 5.0RC1
     */
    void upgrade(Collection<InstalledExtension> previousLocalExtensions, LocalExtension newLocalExtension,
        String namespace, Request request) throws InstallException;

    /**
     * Initialize the provided local extension (during application startup, re-initialization...).
     * 
     * @param localExtension the extension to install
     * @param namespace the namespace where to install the extension
     * @throws ExtensionException error when trying to install the extension
     */
    void initialize(LocalExtension localExtension, String namespace) throws ExtensionException;

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
     * @since 4.2M2
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
     * @since 4.2M2
     */
    void checkUninstall(InstalledExtension extension, String namespace, Request request) throws UninstallException;
}
