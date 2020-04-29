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
package org.xwiki.extension.handler.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.handler.ExtensionHandlerManager;
import org.xwiki.job.Request;

/**
 * Default implementation of {@link ExtensionHandlerManager}.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultExtensionHandlerManager implements ExtensionHandlerManager
{
    /**
     * Message used when falling to find a proper extension handler.
     */
    private static final String LOOKUPERROR = "Can't find any extension handler for the extension ";

    /**
     * Use to lookup {@link ExtensionHandler} implementations.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Get the handler corresponding to the provided extension.
     *
     * @param localExtension the extension to handler
     * @return the handler
     * @throws ComponentLookupException failed to find a proper handler for the provided extension
     */
    private ExtensionHandler getExtensionHandler(LocalExtension localExtension) throws ComponentLookupException
    {
        return this.componentManager.getInstance(ExtensionHandler.class, localExtension.getType());
    }

    @Override
    public void install(LocalExtension localExtension, String namespace, Request request) throws InstallException
    {
        ExtensionHandler extensionHandler;
        try {
            extensionHandler = getExtensionHandler(localExtension);
        } catch (ComponentLookupException e) {
            throw new InstallException(LOOKUPERROR + '[' + localExtension + ']', e);
        }

        try {
            extensionHandler.install(localExtension, namespace, request);
        } catch (Exception e) {
            throw new InstallException("Failed to install extension [" + localExtension.getId() + "]", e);
        }
    }

    @Override
    @Deprecated
    public void uninstall(LocalExtension localExtension, String namespace, Request request) throws UninstallException
    {
        uninstall((InstalledExtension) localExtension, namespace, request);
    }

    @Override
    public void uninstall(InstalledExtension installedExtension, String namespace, Request request)
        throws UninstallException
    {
        ExtensionHandler extensionHandler;
        try {
            extensionHandler = getExtensionHandler(installedExtension);
        } catch (ComponentLookupException e) {
            throw new UninstallException(LOOKUPERROR + '[' + installedExtension + ']', e);
        }

        try {
            extensionHandler.uninstall(installedExtension, namespace, request);
        } catch (UninstallException e) {
            throw e;
        } catch (Exception e) {
            throw new UninstallException("Failed to uninstall extension [" + installedExtension.getId() + "]", e);
        }
    }

    @Override
    @Deprecated
    public void upgrade(LocalExtension previousLocalExtension, LocalExtension newLocalExtension, String namespace,
        Request request) throws InstallException
    {
        upgrade(previousLocalExtension != null ? Arrays.asList((InstalledExtension) previousLocalExtension)
            : Collections.<InstalledExtension>emptyList(), newLocalExtension, namespace, request);
    }

    @Override
    public void upgrade(Collection<InstalledExtension> previousLocalExtensions, LocalExtension newLocalExtension,
        String namespace, Request request) throws InstallException
    {
        ExtensionHandler extensionHandler;
        try {
            extensionHandler = getExtensionHandler(newLocalExtension);
        } catch (ComponentLookupException e) {
            throw new InstallException(LOOKUPERROR + '[' + newLocalExtension + ']', e);
        }

        try {
            extensionHandler.upgrade(previousLocalExtensions, newLocalExtension, namespace, request);
        } catch (InstallException e) {
            throw e;
        } catch (Exception e) {
            throw new InstallException("Failed to upgrade from extension [" + previousLocalExtensions
                + "] to extension [" + newLocalExtension.getId() + "]", e);
        }
    }

    @Override
    public void initialize(LocalExtension localExtension, String namespace) throws ExtensionException
    {
        try {
            ExtensionHandler extensionHandler = getExtensionHandler(localExtension);

            extensionHandler.initialize(localExtension, namespace);
        } catch (Exception e) {
            throw new InstallException("Failed to initialize extension [" + localExtension.getId() + "]", e);
        }
    }
}
