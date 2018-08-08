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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.repository.AbstractExtensionRepositoryFactory;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryException;

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
            RemoteRepository.Builder aetherRepositoryBuilder = new RemoteRepository.Builder(
                repositoryDescriptor.getId(), "default", repositoryDescriptor.getURI().toString());

            // Checksum policy
            String checksumPolicy = repositoryDescriptor.getProperty("checksumPolicy");
            if (StringUtils.isEmpty(checksumPolicy)) {
                checksumPolicy = RepositoryPolicy.CHECKSUM_POLICY_WARN;
            }

            // Update policy
            String updatePolicy = RepositoryPolicy.UPDATE_POLICY_ALWAYS;

            // Don't cached SNAPSHOTs
            aetherRepositoryBuilder.setPolicy(new RepositoryPolicy(true, updatePolicy, checksumPolicy));

            // Authentication
            String username = repositoryDescriptor.getProperty("auth.user");
            if (username != null) {
                AuthenticationBuilder authenticationBuilder = new AuthenticationBuilder();
                authenticationBuilder.addUsername(username);
                authenticationBuilder.addPassword(repositoryDescriptor.getProperty("auth.password"));
                aetherRepositoryBuilder.setAuthentication(authenticationBuilder.build());
            }

            // Proxy
            Proxy proxy = XWikiRepositorySystemSession.JREPROXYSELECTOR.getProxy(aetherRepositoryBuilder.build());
            aetherRepositoryBuilder.setProxy(proxy);

            RemoteRepository aetherRepository = aetherRepositoryBuilder.build();

            return new AetherExtensionRepository(repositoryDescriptor, this, aetherRepository,
                this.plexusProvider.get(), this.componentManager);
        } catch (Exception e) {
            throw new ExtensionRepositoryException("Failed to create repository [" + repositoryDescriptor + "]", e);
        }
    }
}
