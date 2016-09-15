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
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;

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
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Logger logger;

    /**
     * The standard repositories.
     */
    private Map<String, ExtensionRepository> standardRepositories = new HashMap<String, ExtensionRepository>(3);

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
}
