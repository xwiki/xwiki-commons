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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.util.artifact.DefaultArtifactTypeRegistry;
import org.eclipse.aether.util.repository.JreProxySelector;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.xwiki.environment.Environment;
import org.xwiki.extension.maven.internal.MavenUtils;

/**
 * Encapsulate {@link DefaultRepositorySystemSession} to generate and clean a temporary local repository for each
 * sessions.
 *
 * @version $Id$
 * @since 6.0
 */
public class XWikiRepositorySystemSession extends AbstractForwardingRepositorySystemSession implements AutoCloseable
{
    /**
     * The {@link ArtifactType} corresponding to a known type.
     */
    public static final Map<String, ArtifactType> TYPE_MAPPING = new HashMap();

    static final JreProxySelector JREPROXYSELECTOR = new JreProxySelector();

    private static final String TYPE_BUNDLE = "bundle";

    private static final String TYPE_ECLIPSE_PLUGIN = "eclipse-plugin";

    private static final String TYPE_WEBJAR = "webjar";

    static {
        TYPE_MAPPING.put(TYPE_BUNDLE,
            new DefaultArtifactType(TYPE_BUNDLE, MavenUtils.JAR_EXTENSION, "", MavenUtils.JAVA_LANGUAGE));
        TYPE_MAPPING.put(TYPE_ECLIPSE_PLUGIN,
            new DefaultArtifactType(TYPE_ECLIPSE_PLUGIN, MavenUtils.JAR_EXTENSION, "", MavenUtils.JAVA_LANGUAGE));
        TYPE_MAPPING.put(TYPE_WEBJAR,
            new DefaultArtifactType(TYPE_WEBJAR, MavenUtils.JAR_EXTENSION, "", (String) null));
    }

    private final RepositorySystemSession session;

    private final boolean closable;

    /**
     * @param session a pre-existing session
     */
    public XWikiRepositorySystemSession(RepositorySystemSession session)
    {
        this.session = session;
        this.closable = false;

        // Add various type descriptors
        addTypes(session);
    }

    /**
     * @param repositorySystem the AETHER repository system component
     * @param enviroment the environment component
     * @throws IOException when failing to create a temporary directory to download the required files
     */
    public XWikiRepositorySystemSession(RepositorySystem repositorySystem, Environment enviroment) throws IOException
    {
        DefaultRepositorySystemSession wsession = MavenRepositorySystemUtils.newSession();
        this.session = wsession;
        this.closable = true;

        // Local repository

        Path downloadDirectory = enviroment.getTemporaryDirectory().toPath().resolve("extension/download");
        Files.createDirectories(downloadDirectory);
        File localDir = Files.createTempDirectory(downloadDirectory, "repository").toFile();
        LocalRepository localRepository = new LocalRepository(localDir);
        wsession.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(wsession, localRepository));

        // Proxy selector

        wsession.setProxySelector(JREPROXYSELECTOR);

        // Remove all system properties that could disrupt effective pom resolution
        wsession.setSystemProperty("version", null);
        wsession.setSystemProperty("groupId", null);

        // Add various type descriptors
        addTypes(wsession);

        // Fail when the pom is missing or invalid
        wsession.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(false, false));
    }

    private void addTypes(RepositorySystemSession session)
    {
        // TODO: Find them in extensions registered in pom files
        ArtifactTypeRegistry artifactTypeRegistry = session.getArtifactTypeRegistry();
        if (artifactTypeRegistry instanceof DefaultArtifactTypeRegistry) {
            DefaultArtifactTypeRegistry defaultArtifactTypeRegistry =
                (DefaultArtifactTypeRegistry) artifactTypeRegistry;

            TYPE_MAPPING.forEach((key, value) -> {
                defaultArtifactTypeRegistry.add(value);
            });
        }
    }

    @Override
    protected RepositorySystemSession getSession()
    {
        return this.session;
    }

    @Override
    public void close()
    {
        if (this.closable) {
            LocalRepository repository = this.session.getLocalRepository();

            if (repository.getBasedir().exists()) {
                try {
                    FileUtils.deleteDirectory(repository.getBasedir());
                } catch (IOException e) {
                    // TODO: Should probably log something even if it should be pretty rare
                }
            }
        }
    }

    /**
     * @param userAgent the user agent
     */
    public void setUserAgent(String userAgent)
    {
        if (this.session instanceof DefaultRepositorySystemSession) {
            ((DefaultRepositorySystemSession) this.session).setConfigProperty(ConfigurationProperties.USER_AGENT,
                userAgent);
        }
    }

    /**
     * @param properties the custom properties
     */
    public void addConfigurationProperties(Map<String, String> properties)
    {
        if (this.session instanceof DefaultRepositorySystemSession) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                ((DefaultRepositorySystemSession) this.session).setConfigProperty(entry.getKey(), entry.getValue());
            }
        }
    }
}
