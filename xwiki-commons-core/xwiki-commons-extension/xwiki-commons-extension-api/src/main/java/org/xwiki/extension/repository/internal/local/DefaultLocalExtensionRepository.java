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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepositoryException;
import org.xwiki.extension.repository.internal.AbstractCachedExtensionRepository;

/**
 * Default implementation of {@link LocalExtensionRepository}.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
// TODO: make it threadsafe bulletproofs
public class DefaultLocalExtensionRepository extends AbstractCachedExtensionRepository<DefaultLocalExtension>
    implements LocalExtensionRepository, Initializable
{
    private static final String ID = "local";

    /**
     * Used to get repository path.
     */
    @Inject
    private transient ExtensionManagerConfiguration configuration;

    /**
     * The logger to log.
     */
    @Inject
    private transient Logger logger;

    /**
     * The component manager.
     */
    @Inject
    private transient ComponentManager componentManager;

    /**
     * Used to manipulate filesystem repository storage.
     */
    private transient LocalExtensionStorage storage;

    /**
     * Make the repository ignore features.
     */
    public DefaultLocalExtensionRepository()
    {
        super(true);
    }

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.storage =
                new LocalExtensionStorage(this, this.configuration.getLocalRepository(), this.componentManager);
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to intialize local extension storage", e);
        }

        setDescriptor(new DefaultExtensionRepositoryDescriptor(ID, ID, this.storage.getRootFolder().toURI()));

        try {
            this.storage.loadExtensions();
        } catch (IOException e) {
            throw new InitializationException("Failed to load load etensions", e);
        }
    }

    /**
     * Register a new local extension.
     *
     * @param localExtension the new local extension
     */
    protected void addLocalExtension(DefaultLocalExtension localExtension)
    {
        addCachedExtension(localExtension);
    }

    // LocalRepository

    @Override
    public LocalExtension getLocalExtension(ExtensionId extensionId)
    {
        return this.extensions.get(extensionId);
    }

    @Override
    public Collection<LocalExtension> getLocalExtensions()
    {
        return Collections.<LocalExtension>unmodifiableCollection(this.extensions.values());
    }

    @Override
    public Collection<LocalExtension> getLocalExtensionVersions(String id)
    {
        Collection<DefaultLocalExtension> versions = this.extensionsVersions.get(id);

        return versions != null ? Collections.<LocalExtension>unmodifiableCollection(versions)
            : Collections.<LocalExtension>emptyList();
    }

    /**
     * Create a new local extension from a remote extension.
     *
     * @param extension the extension to copy
     * @return the new local extension
     */
    private DefaultLocalExtension createExtension(Extension extension)
    {
        DefaultLocalExtension localExtension = new DefaultLocalExtension(this, extension);

        if (StringUtils.isNotEmpty(localExtension.getType())) {
            localExtension.setFile(this.storage.getNewExtensionFile(localExtension.getId(), localExtension.getType()));
        }

        return localExtension;
    }

    @Override
    public int countExtensions()
    {
        return this.extensions.size();
    }

    @Override
    public LocalExtension storeExtension(Extension extension) throws LocalExtensionRepositoryException
    {
        DefaultLocalExtension localExtension = this.extensions.get(extension.getId());

        if (localExtension == null) {
            try {
                localExtension = createExtension(extension);

                // Store the extension file if any
                DefaultLocalExtensionFile extensionFile = localExtension.getFile();
                if (extensionFile != null) {
                    File targetFile = localExtension.getFile().getFile();
                    InputStream is = extension.getFile().openStream();
                    FileUtils.copyInputStreamToFile(is, targetFile);
                }

                // Store the extension descriptor
                this.storage.saveDescriptor(localExtension);

                // Cache extension
                addLocalExtension(localExtension);
            } catch (Exception e) {
                // TODO: clean

                throw new LocalExtensionRepositoryException("Failed to save extension [" + extension + "] descriptor",
                    e);
            }
        } else {
            throw new LocalExtensionRepositoryException(
                "Extension [" + extension + "] already exists in local repository");
        }

        return localExtension;
    }

    @Override
    public void setProperties(LocalExtension localExtension, Map<String, Object> properties)
        throws LocalExtensionRepositoryException
    {
        DefaultLocalExtension extension = this.extensions.get(localExtension.getId());

        if (extension != null) {
            extension.setProperties(properties);
            try {
                this.storage.saveDescriptor(extension);
            } catch (Exception e) {
                throw new LocalExtensionRepositoryException(
                    "Failed to save descriptor for extension [" + localExtension + "]", e);
            }
        }
    }

    @Override
    public void removeExtension(LocalExtension extension) throws ResolveException
    {
        DefaultLocalExtension localExtension = resolve(extension.getId());

        try {
            this.storage.removeExtension(localExtension);
        } catch (IOException e) {
            // Should not happen if the local extension exists

            this.logger.error("Failed to remove extension [" + extension + "]", e);
        }

        // Remove the extension from the caches
        removeCachedExtension(localExtension);
    }
}
