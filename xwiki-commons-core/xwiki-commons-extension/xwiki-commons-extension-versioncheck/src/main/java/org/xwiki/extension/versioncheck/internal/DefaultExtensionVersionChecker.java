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
package org.xwiki.extension.versioncheck.internal;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.versioncheck.ExtensionVersionCheckException;
import org.xwiki.extension.versioncheck.ExtensionVersionChecker;

/**
 * This is the default implementation of {@link ExtensionVersionChecker}.
 *
 * @version $Id$
 * @since 9.9-RC1
 */
@Singleton
@Component
public class DefaultExtensionVersionChecker implements ExtensionVersionChecker
{
    @Inject
    private InstalledExtensionRepository installedExtensionRepository;

    @Inject
    private ExtensionRepositoryManager extensionRepositoryManager;

    @Override
    public boolean isUpToDate(ExtensionId extensionId) throws ExtensionVersionCheckException
    {
        // Check if the extension is in the local repository
        InstalledExtension installedExtension = installedExtensionRepository.getInstalledExtension(extensionId);
        if (installedExtension == null)
        {
            return false;
        }

        try {
            for (Version version : extensionRepositoryManager.resolveVersions(
                    installedExtension.getId().getId(), 0, -1)) {
                if (version.compareTo(extensionId.getVersion()) > 0) {
                    return true;
                }
            }
        } catch (ResolveException e) {
            throw new ExtensionVersionCheckException(
                    String.format("Failed to compare versions for extension [{}]", extensionId.getId()), e);
        }

        return false;
    }
}
