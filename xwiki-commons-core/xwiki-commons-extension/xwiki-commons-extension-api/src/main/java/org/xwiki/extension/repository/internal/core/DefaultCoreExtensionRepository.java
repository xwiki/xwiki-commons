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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.ExtensionNotFoundException;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.internal.RepositoryUtils;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.version.Version;

/**
 * Default implementation of {@link CoreExtensionRepository}.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultCoreExtensionRepository extends AbstractExtensionRepository
    implements CoreExtensionRepository, Initializable
{
    /**
     * The core extensions.
     */
    protected transient Map<String, DefaultCoreExtension> extensions = new ConcurrentHashMap<>();

    /**
     * The extension associated to the environment.
     */
    protected transient DefaultCoreExtension environmentExtension;

    /**
     * The logger to log.
     */
    @Inject
    private transient Logger logger;

    /**
     * Used to scan jars to find extensions.
     */
    @Inject
    private transient CoreExtensionScanner scanner;

    @Inject
    private ExtensionRepositoryManager repositoryManager;

    @Inject
    private ExtensionManagerConfiguration configuration;

    /**
     * Default constructor.
     */
    public DefaultCoreExtensionRepository()
    {
        super(new DefaultExtensionRepositoryDescriptor("core", "xwiki-core", null));
    }

    @Override
    public boolean isFilterable()
    {
        return true;
    }

    @Override
    public boolean isSortable()
    {
        return true;
    }

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.extensions.putAll(this.scanner.loadExtensions(this));

            this.environmentExtension = this.scanner.loadEnvironmentExtension(this);
            if (this.environmentExtension != null) {
                this.extensions.put(this.environmentExtension.getId().getId(), this.environmentExtension);
            }

            // Put extensions features in the map
            for (DefaultCoreExtension coreExtension : this.extensions.values()) {
                addExtensionFeatures(coreExtension);
            }

            // Update core extensions only if there is any remote repository and it's not disabled
            if (this.configuration.resolveCoreExtensions() && !this.repositoryManager.getRepositories().isEmpty()) {
                // Start a background thread to get more details about the found extensions
                Thread thread = new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        DefaultCoreExtensionRepository.this.scanner
                            .updateExtensions(DefaultCoreExtensionRepository.this.extensions.values());
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.setDaemon(true);
                thread.setName("Core extension repository updater");
                thread.start();
            }
        } catch (Exception e) {
            this.logger.warn("Failed to load core extensions", e);
        }
    }

    /**
     * @since 9.4RC1
     */
    protected void addExtension(DefaultCoreExtension coreExtension)
    {
        this.extensions.put(coreExtension.getId().getId(), coreExtension);

        addExtensionFeatures(coreExtension);
    }

    private void addExtensionFeatures(DefaultCoreExtension coreExtension)
    {
        for (ExtensionId feature : coreExtension.getExtensionFeatures()) {
            this.extensions.put(feature.getId(), coreExtension);
        }
    }

    // Repository

    @Override
    public CoreExtension resolve(ExtensionId extensionId) throws ResolveException
    {
        CoreExtension extension = getCoreExtension(extensionId.getId());

        if (extension == null
            || (extensionId.getVersion() != null && !extension.getId().getVersion().equals(extensionId.getVersion()))) {
            throw new ExtensionNotFoundException("Could not find extension [" + extensionId + "]");
        }

        return extension;
    }

    @Override
    public CoreExtension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        CoreExtension extension = getCoreExtension(extensionDependency.getId());

        if (extension == null
            || (!extensionDependency.getVersionConstraint().containsVersion(extension.getId().getVersion()))) {
            throw new ExtensionNotFoundException("Could not find extension dependency [" + extensionDependency + "]");
        }

        return extension;
    }

    @Override
    public boolean exists(ExtensionId extensionId)
    {
        Extension extension = getCoreExtension(extensionId.getId());

        if (extension == null
            || (extensionId.getVersion() != null && !extension.getId().getVersion().equals(extensionId.getVersion()))) {
            return false;
        }

        return true;
    }

    @Override
    public boolean exists(String feature)
    {
        return this.extensions.containsKey(feature);
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        Extension extension = getCoreExtension(id);

        if (extension == null) {
            throw new ExtensionNotFoundException("Could not find extension with id [" + id + "]");
        }

        Collection<Version> versions;
        if (nb == 0 || offset > 0) {
            versions = Collections.emptyList();
        } else {
            versions = Arrays.asList(extension.getId().getVersion());
        }

        return new CollectionIterableResult<Version>(1, offset, versions);
    }

    // CoreExtensionRepository

    @Override
    public CoreExtension getEnvironmentExtension()
    {
        return this.environmentExtension;
    }

    @Override
    public int countExtensions()
    {
        return this.extensions.size();
    }

    @Override
    public Collection<CoreExtension> getCoreExtensions()
    {
        return new ArrayList<CoreExtension>(this.extensions.values());
    }

    @Override
    public CoreExtension getCoreExtension(String feature)
    {
        if (feature == null) {
            return null;
        }

        return this.extensions.get(feature);
    }

    // Searchable

    @Override
    public IterableResult<Extension> search(String pattern, int offset, int nb) throws SearchException
    {
        return (IterableResult) RepositoryUtils.searchInCollection(pattern, offset, nb, this.extensions.values(), true);
    }

    @Override
    public IterableResult<Extension> search(ExtensionQuery query) throws SearchException
    {
        return (IterableResult) RepositoryUtils.searchInCollection(query, this.extensions.values(), true);
    }
}
