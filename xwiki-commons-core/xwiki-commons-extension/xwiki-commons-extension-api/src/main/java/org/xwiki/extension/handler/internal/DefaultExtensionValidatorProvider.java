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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.ExtensionValidator;
import org.xwiki.job.Request;

/**
 * Make sure to always provide a validator.
 *
 * @version $Id$
 * @since 4.2M2
 */
@Component
@Singleton
public class DefaultExtensionValidatorProvider implements Provider<ExtensionValidator>, Initializable
{
    /**
     * Used to lookup the actual validator.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * The validator is kept in cache.
     */
    private ExtensionValidator defaultValidator;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.defaultValidator = this.componentManager.getInstance(ExtensionValidator.class);
        } catch (ComponentLookupException e) {
            this.defaultValidator = new ExtensionValidator()
            {
                @Override
                public void checkUninstall(InstalledExtension extension, String namespace, Request request)
                    throws UninstallException
                {
                    // Valid
                }

                @Override
                public void checkInstall(Extension extension, String namespace, Request request)
                    throws InstallException
                {
                    // Valid
                }
            };
        }
    }

    @Override
    public ExtensionValidator get()
    {
        return this.defaultValidator;
    }
}
