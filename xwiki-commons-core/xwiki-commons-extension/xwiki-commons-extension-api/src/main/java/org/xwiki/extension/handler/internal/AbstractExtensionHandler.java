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
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.handler.ExtensionValidator;
import org.xwiki.job.Request;

/**
 * Base class for {@link ExtensionHandler} implementations.
 *
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractExtensionHandler implements ExtensionHandler
{
    /**
     * The logger to log.
     */
    @Inject
    protected Logger logger;

    /**
     * Used to check if an extension it is possible to install/uninstall a given extension.
     */
    @Inject
    private Provider<ExtensionValidator> defaultValidatorProvider;

    // ExtensionHandler

    @Override
    @Deprecated
    public void uninstall(LocalExtension localExtension, String namespace, Request request) throws UninstallException
    {
        uninstall((InstalledExtension) localExtension, namespace, request);
    }

    @Override
    @Deprecated
    public void upgrade(LocalExtension previousLocalExtension, LocalExtension newLocalExtension, String namespace,
        Request request) throws InstallException
    {
        upgrade(previousLocalExtension != null ? Arrays.asList(previousLocalExtension) : Collections.EMPTY_LIST,
            newLocalExtension, namespace, request);
    }

    @Override
    public void upgrade(Collection<InstalledExtension> previousInstalledExtensions, LocalExtension newLocalExtension,
        String namespace, Request request) throws InstallException
    {
        for (InstalledExtension previousExtension : previousInstalledExtensions) {
            try {
                uninstall(previousExtension, namespace, request);
            } catch (UninstallException e) {
                throw new InstallException("Failed to uninstall previous extension [" + previousExtension + "]", e);
            }
        }
        install(newLocalExtension, namespace, null);
    }

    @Override
    public void initialize(LocalExtension localExtension, String namespace) throws ExtensionException
    {
        // do nothing by default
    }

    @Override
    public void checkInstall(Extension extension, String namespace, Request request) throws InstallException
    {
        this.defaultValidatorProvider.get().checkInstall(extension, namespace, request);
    }

    @Override
    public void checkUninstall(InstalledExtension extension, String namespace, Request request)
        throws UninstallException
    {
        this.defaultValidatorProvider.get().checkUninstall(extension, namespace, request);
    }
}
