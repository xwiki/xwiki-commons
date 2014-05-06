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
package org.xwiki.extension.repository.aether.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.RemoteRepository;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.repository.AbstractExtensionRepositoryFactory;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryException;
import org.xwiki.extension.repository.ExtensionRepositoryManager;

/**
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
@Named("maven")
public class AetherExtensionRepositoryFactory extends AbstractExtensionRepositoryFactory implements Initializable
{
    @Inject
    private ComponentManager componentManager;

    @Inject
    private Provider<PlexusContainer> plexusProvider;

    @Inject
    private ExtensionManagerConfiguration configuration;

    @Inject
    private ExtensionRepositoryManager repositoryManager;

    private RepositorySystem repositorySystem;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.repositorySystem = this.plexusProvider.get().lookup(RepositorySystem.class);
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to lookup RepositorySystem", e);
        }
    }

    public XWikiRepositorySystemSession createRepositorySystemSession()
    {
        XWikiRepositorySystemSession session = new XWikiRepositorySystemSession(this.repositorySystem);

        session.setUserAgent(this.configuration.getUserAgent());

        return session;
    }

    @Override
    public ExtensionRepository createRepository(ExtensionRepositoryDescriptor repositoryDescriptor)
        throws ExtensionRepositoryException
    {
        try {
            return new AetherExtensionRepository(repositoryDescriptor, this, this.plexusProvider.get(),
                this.componentManager);
        } catch (Exception e) {
            throw new ExtensionRepositoryException("Failed to create repository [" + repositoryDescriptor + "]", e);
        }
    }

    List<RemoteRepository> getAllMavenRepositories(RemoteRepository firstRepository)
    {
        Collection<ExtensionRepository> extensionRepositories = this.repositoryManager.getRepositories();

        List<RemoteRepository> reposirories = new ArrayList<RemoteRepository>(extensionRepositories.size());

        // Put first repository
        reposirories.add(firstRepository);

        // Add other repositories (and filter first one)
        for (ExtensionRepository extensionRepository : extensionRepositories) {
            if (extensionRepository instanceof AetherExtensionRepository) {
                RemoteRepository repository = ((AetherExtensionRepository) extensionRepository).getRemoteRepository();

                if (firstRepository != repository) {
                    reposirories.add(repository);
                }
            }
        }

        return reposirories;
    }
}
