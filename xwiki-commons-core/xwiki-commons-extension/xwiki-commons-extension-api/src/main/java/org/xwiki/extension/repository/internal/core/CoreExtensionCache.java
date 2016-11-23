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
package org.xwiki.extension.repository.internal.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.environment.Environment;
import org.xwiki.extension.internal.PathUtils;
import org.xwiki.extension.repository.internal.ExtensionSerializer;

/**
 * Store resolve core extension to not have to resolve it again at next restart.
 * 
 * @version $Id$
 * @since 6.4M1
 */
@Component(roles = CoreExtensionCache.class)
@Singleton
public class CoreExtensionCache implements Initializable
{
    /**
     * The String to search in a descriptor URL to know if it's inside a jar or another packaged file.
     */
    private static final String PACKAGE_MARKER = "!/";

    @Inject
    private Environment environment;

    @Inject
    private ExtensionSerializer serializer;

    @Inject
    private Logger logger;

    private File folder;

    @Override
    public void initialize() throws InitializationException
    {
        File permanentDirectory = this.environment.getPermanentDirectory();
        if (permanentDirectory != null) {
            this.folder = new File(permanentDirectory, "cache/extension/core/");
        }
    }

    /**
     * @param extension the extension to store
     * @throws Exception when failing to store the extension
     */
    public void store(DefaultCoreExtension extension) throws Exception
    {
        if (this.folder == null) {
            return;
        }

        URL descriptorURL = extension.getDescriptorURL();

        if (!descriptorURL.getPath().contains(PACKAGE_MARKER)) {
            // Usually mean jars are not kept, don't cache that or it's going to be a nightmare when upgrading
            return;
        }

        File file = getFile(descriptorURL);

        // Make sure the file parents exist
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileOutputStream stream = new FileOutputStream(file)) {
            this.serializer.saveExtensionDescriptor(extension, stream);
        }
    }

    /**
     * @param repository the repository to set in the new extension instance
     * @param descriptorURL the extension descriptor URL
     * @return the extension corresponding to the passed descriptor URL, null if none could be found
     */
    public DefaultCoreExtension getExtension(DefaultCoreExtensionRepository repository, URL descriptorURL)
    {
        if (this.folder == null) {
            return null;
        }

        if (!descriptorURL.getPath().contains(PACKAGE_MARKER)) {
            // Usually mean jars are not kept, make sure to not take into account such a wrongly cached descriptor
            return null;
        }

        File file = getFile(descriptorURL);

        if (file.exists()) {
            try (FileInputStream stream = new FileInputStream(file)) {
                DefaultCoreExtension coreExtension =
                    this.serializer.loadCoreExtensionDescriptor(repository, descriptorURL, stream);

                return coreExtension;
            } catch (Exception e) {
                this.logger.warn("Failed to parse cached core extension", e);
            }
        }

        return null;
    }

    private String getExtensionFileName(URL url)
    {
        URL extensionURL;
        try {
            extensionURL = PathUtils.getExtensionURL(url);
        } catch (IOException e) {
            return null;
        }

        String extensionPath = extensionURL.toExternalForm();
        int index = extensionPath.lastIndexOf('/');
        if (index > 0 && index < extensionPath.length()) {
            extensionPath = extensionPath.substring(index + 1);

            index = extensionPath.lastIndexOf('.');
            if (index > 0 && index < extensionPath.length()) {
                extensionPath = extensionPath.substring(0, index);
            }

            return extensionPath;
        }

        return null;
    }

    private File getFile(URL url)
    {
        StringBuilder builder = new StringBuilder();

        String fileName = getExtensionFileName(url);
        if (fileName != null) {
            builder.append(fileName);
            builder.append('-');
        }

        builder.append(DigestUtils.md5Hex(url.toExternalForm()));

        builder.append(".xed");

        return new File(this.folder, builder.toString());
    }
}
