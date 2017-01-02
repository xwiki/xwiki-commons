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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.util.artifact.DefaultArtifactTypeRegistry;
import org.eclipse.aether.util.repository.JreProxySelector;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.xwiki.extension.maven.internal.MavenUtils;

import com.google.common.io.Files;

/**
 * Encapsulate {@link DefaultRepositorySystemSession} to generate and clean a temporary local repository for each
 * sessions.
 *
 * @version $Id$
 * @since 6.0
 */
public class XWikiRepositorySystemSession extends AbstractForwardingRepositorySystemSession implements AutoCloseable
{
    static final JreProxySelector JREPROXYSELECTOR = new JreProxySelector();

    private final DefaultRepositorySystemSession session;

    /**
     * @param repositorySystem the AETHER repository system component
     */
    public XWikiRepositorySystemSession(RepositorySystem repositorySystem)
    {
        this.session = MavenRepositorySystemUtils.newSession();

        // Local repository

        File localDir = Files.createTempDir();
        LocalRepository localRepository = new LocalRepository(localDir);
        this.session
            .setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(this.session, localRepository));

        // Proxy selector

        this.session.setProxySelector(JREPROXYSELECTOR);

        // Remove all system properties that could disrupt effective pom resolution
        this.session.setSystemProperty("version", null);
        this.session.setSystemProperty("groupId", null);

        // Add various type descriptors
        ArtifactTypeRegistry artifactTypeRegistry = this.session.getArtifactTypeRegistry();
        if (artifactTypeRegistry instanceof DefaultArtifactTypeRegistry) {
            DefaultArtifactTypeRegistry defaultArtifactTypeRegistry =
                (DefaultArtifactTypeRegistry) artifactTypeRegistry;
            defaultArtifactTypeRegistry
                .add(new DefaultArtifactType("bundle", MavenUtils.JAR_EXTENSION, "", MavenUtils.JAVA_LANGUAGE));
            defaultArtifactTypeRegistry
                .add(new DefaultArtifactType("eclipse-plugin", MavenUtils.JAR_EXTENSION, "", MavenUtils.JAVA_LANGUAGE));
        }

        // Fail when the pom is missing or invalid
        this.session.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(false, false));
    }

    @Override
    protected RepositorySystemSession getSession()
    {
        return this.session;
    }

    @Override
    public void close()
    {
        LocalRepository repository = this.session.getLocalRepository();

        if (repository.getBasedir().exists()) {
            try {
                FileUtils.deleteDirectory(repository.getBasedir());
            } catch (IOException e) {
                // TODO: Should probably log something even if it should be pretty rare
            }
        }
    }

    /**
     * @param userAgent the user agent
     */
    public void setUserAgent(String userAgent)
    {
        this.session.setConfigProperty(ConfigurationProperties.USER_AGENT, userAgent);
    }

    /**
     * @param properties the custom properties
     */
    public void addConfigurationProperties(Map<String, String> properties)
    {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            this.session.setConfigProperty(entry.getKey(), entry.getValue());
        }
    }
}
