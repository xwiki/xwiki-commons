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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.namespace.Namespace;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.index.ExtensionIndex;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.internal.RepositoryUtils;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.AdvancedSearchable;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.wrap.WrappingExtensionRepository;

/**
 * Default implementation of {@link ExtensionManager}.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultExtensionManager implements ExtensionManager, Initializable
{
    /**
     * Used to manipulate remote repositories.
     */
    @Inject
    private ExtensionRepositoryManager repositoryManager;

    /**
     * Used to manipulate core extensions.
     */
    @Inject
    private CoreExtensionRepository coreExtensionRepository;

    /**
     * Used to manipulate local extensions.
     */
    @Inject
    private LocalExtensionRepository localExtensionRepository;

    /**
     * Used to manipulate installed extensions.
     */
    @Inject
    private InstalledExtensionRepository installedExtensionRepository;

    @Inject
    private Provider<ExtensionIndex> indexRepositoryProvider;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Logger logger;

    /**
     * The standard repositories.
     */
    private Map<String, ExtensionRepository> standardRepositories = new HashMap<>(3);

    @Override
    public void initialize() throws InitializationException
    {
        this.standardRepositories.put(this.coreExtensionRepository.getDescriptor().getId(),
            this.coreExtensionRepository);
        this.standardRepositories.put(this.localExtensionRepository.getDescriptor().getId(),
            this.localExtensionRepository);
        this.standardRepositories.put(this.installedExtensionRepository.getDescriptor().getId(),
            this.installedExtensionRepository);
    }

    @Override
    public Extension resolveExtension(ExtensionId extensionId) throws ResolveException
    {
        try {
            return this.coreExtensionRepository.resolve(extensionId);
        } catch (ResolveException notACoreExtension) {
            return resolveExtensionFromInstalled(extensionId);
        }
    }

    @Override
    public boolean exists(ExtensionId extensionId)
    {
        if (!this.installedExtensionRepository.exists(extensionId)) {
            if (!this.localExtensionRepository.exists(extensionId)) {
                return this.repositoryManager.exists(extensionId);
            }
        }

        return true;
    }

    /**
     * @param extensionId the extension identifier
     * @return the resolved extension
     * @throws ResolveException error when trying to resolve extension
     */
    private Extension resolveExtensionFromInstalled(ExtensionId extensionId) throws ResolveException
    {
        try {
            return this.installedExtensionRepository.resolve(extensionId);
        } catch (ResolveException notAnInstalledExtension) {
            try {
                return this.localExtensionRepository.resolve(extensionId);
            } catch (ResolveException notALocalExtension) {
                return this.repositoryManager.resolve(extensionId);
            }
        }
    }

    @Override
    @Deprecated
    public Extension resolveExtension(ExtensionDependency extensionDependency) throws ResolveException
    {
        try {
            return this.coreExtensionRepository.resolve(extensionDependency);
        } catch (ResolveException notACoreExtension) {
            try {
                return this.localExtensionRepository.resolve(extensionDependency);
            } catch (ResolveException notALocalExtension) {
                return this.repositoryManager.resolve(extensionDependency);
            }
        }
    }

    @Override
    public Extension resolveExtension(ExtensionDependency extensionDependency, String namespace) throws ResolveException
    {
        try {
            return this.coreExtensionRepository.resolve(extensionDependency);
        } catch (ResolveException notACoreExtension) {
            InstalledExtension extension =
                this.installedExtensionRepository.getInstalledExtension(extensionDependency.getId(), namespace);

            if (extension != null
                && extensionDependency.getVersionConstraint().containsVersion(extension.getId().getVersion())) {
                return extension;
            }

            try {
                return this.localExtensionRepository.resolve(extensionDependency);
            } catch (ResolveException notALocalExtension) {
                return this.repositoryManager.resolve(extensionDependency);
            }
        }
    }

    @Override
    public ExtensionRepository getRepository(String repositoryId)
    {
        // Try index
        if (repositoryId.equals("index")) {
            return this.indexRepositoryProvider.get();
        }

        // Try internal repositories
        ExtensionRepository repository = this.standardRepositories.get(repositoryId);

        // Try component repositories
        ComponentManager componentManager = this.componentManagerProvider.get();
        if (componentManager.hasComponent(ExtensionRepository.class, repositoryId)) {
            try {
                return componentManager.getInstance(ExtensionRepository.class, repositoryId);
            } catch (ComponentLookupException e) {
                this.logger.error("Failed to lookup component", e);
            }
        }

        // Try remote repositories
        if (repository == null) {
            repository = this.repositoryManager.getRepository(repositoryId);
        }

        return repository;
    }

    @Override
    public Extension getAccessibleExtension(String feature, Namespace namespace)
    {
        // Try installed extension
        Extension extension = this.installedExtensionRepository.getInstalledExtension(feature, namespace.serialize());

        if (extension == null) {
            // Try core extension
            extension = this.coreExtensionRepository.getCoreExtension(feature);
        }

        return extension;
    }

    @Override
    public IterableResult<Extension> searchAccessibleExtensions(Namespace namespace, ExtensionQuery query)
        throws SearchException
    {
        return RepositoryUtils.search(query,
            Arrays.asList(new SearchableInstalledExtensionRepository(this.installedExtensionRepository, namespace),
                this.coreExtensionRepository));
    }

    private static class SearchableInstalledExtensionRepository
        extends WrappingExtensionRepository<InstalledExtensionRepository> implements AdvancedSearchable
    {
        private final Namespace namespace;

        private final String serializedNamespace;

        SearchableInstalledExtensionRepository(InstalledExtensionRepository repository, Namespace namespace)
        {
            super(repository);

            this.namespace = namespace;
            this.serializedNamespace = this.namespace != null ? this.namespace.serialize() : null;
        }

        @Override
        public IterableResult<Extension> search(String pattern, int offset, int nb) throws SearchException
        {
            ExtensionQuery query = new ExtensionQuery(pattern);
            query.setOffset(offset);
            query.setLimit(nb);

            return search(query);
        }

        @Override
        public boolean isFilterable()
        {
            return getWrapped().isFilterable();
        }

        @Override
        public boolean isSortable()
        {
            return getWrapped().isSortable();
        }

        @Override
        public IterableResult<Extension> search(ExtensionQuery query) throws SearchException
        {
            return (IterableResult) (this.namespace != null
                ? getWrapped().searchInstalledExtensions(this.serializedNamespace, query)
                : getWrapped().searchInstalledExtensions(query));
        }
    }
}
