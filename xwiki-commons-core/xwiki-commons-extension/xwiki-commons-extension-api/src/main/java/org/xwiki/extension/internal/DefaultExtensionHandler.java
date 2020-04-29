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
package org.xwiki.extension.internal;

import java.util.Collection;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.job.Request;

/**
 * The default extension handler (not doing much since it's for extensions without an associated file).
 * 
 * @version $Id$
 * @since 12.4RC1
 */
@Component
@Singleton
public class DefaultExtensionHandler implements ExtensionHandler
{
    @Override
    public void install(LocalExtension localExtension, String namespace, Request request) throws InstallException
    {
        // Nothing to do
    }

    @Override
    public void uninstall(LocalExtension localExtension, String namespace, Request request) throws UninstallException
    {
        // Nothing to do
    }

    @Override
    public void uninstall(InstalledExtension localExtension, String namespace, Request request)
        throws UninstallException
    {
        // Nothing to do
    }

    @Override
    public void upgrade(LocalExtension previousLocalExtension, LocalExtension newLocalExtension, String namespace,
        Request request) throws InstallException
    {
        // Nothing to do
    }

    @Override
    public void upgrade(Collection<InstalledExtension> previousLocalExtensions, LocalExtension newLocalExtension,
        String namespace, Request request) throws InstallException
    {

        // Nothing to do
    }

    @Override
    public void initialize(LocalExtension localExtension, String namespace) throws ExtensionException
    {

        // Nothing to do
    }

    @Override
    public void checkInstall(Extension extension, String namespace, Request request) throws InstallException
    {
        // Always allowed
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.extension.handler.ExtensionHandler#checkUninstall(org.xwiki.extension.InstalledExtension,
     *      java.lang.String, org.xwiki.job.Request)
     */
    @Override
    public void checkUninstall(InstalledExtension extension, String namespace, Request request)
        throws UninstallException
    {
        // Always allowed
    }
}
