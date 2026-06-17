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
package org.xwiki.extension.repository.internal.local;

import java.io.File;
import java.nio.file.Path;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.store.blob.BlobStorePropertiesBuilder;
import org.xwiki.store.blob.BlobStorePropertiesCustomizer;

/**
 * Customizes filesystem blob store profiles using the configured local filesystem extension repository location.
 * Applies only when the profile type is "filesystem" and store name equals the extension/repository name.
 *
 * @version $Id$
 * @since 18.2.0RC1
 */
@Component
@Singleton
@Named("org.xwiki.extension.repository.internal.local.LocalExtensionBlobStorePropertiesCustomizer")
public class LocalExtensionBlobStorePropertiesCustomizer implements BlobStorePropertiesCustomizer
{
    @Inject
    private ExtensionManagerConfiguration configuration;

    @Inject
    private Logger logger;

    @Override
    public void customize(BlobStorePropertiesBuilder propertiesBuilder)
    {
        // Only adjust for filesystem stores
        if (!"filesystem".equals(propertiesBuilder.getType())) {
            return;
        }

        // If the name matches the local repository store, map configured directory.
        if (DefaultLocalExtensionRepository.STORE_NAME.equals(propertiesBuilder.getName())) {
            File fileStorageDirectory = this.configuration.getLocalRepository();

            Path path = fileStorageDirectory.toPath();
            this.logger.debug("Customizing filesystem store root directory to [{}] for local extensions store", path);
            // Constant copied from FileSystemBlobStoreProperties to avoid a direct dependency.
            propertiesBuilder.set("filesystem.rootDirectory", path);
        }
    }
}
