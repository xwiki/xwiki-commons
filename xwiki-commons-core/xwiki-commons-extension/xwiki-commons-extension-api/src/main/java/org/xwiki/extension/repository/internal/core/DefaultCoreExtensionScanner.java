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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.environment.Environment;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.internal.PathUtils;
import org.xwiki.extension.internal.maven.MavenExtensionDependency;
import org.xwiki.extension.internal.maven.MavenUtils;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.internal.MavenExtension;
import org.xwiki.properties.ConverterManager;

import com.google.common.base.Predicates;

/**
 * Scan jars to find core extensions.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultCoreExtensionScanner implements CoreExtensionScanner, Disposable
{
    /**
     * Logger to use to log shutdown information (opposite of initialization).
     */
    private static final Logger SHUTDOWN_LOGGER = LoggerFactory.getLogger("org.xwiki.shutdown");

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Used to resolve found core extensions.
     */
    @Inject
    private ExtensionRepositoryManager repositoryManager;

    /**
     * Used to parse some custom properties.
     */
    @Inject
    private ConverterManager converter;

    @Inject
    private Environment environment;

    @Inject
    private CoreExtensionCache cache;

    private boolean shouldStop;

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        this.shouldStop = true;
    }

    private Dependency toDependency(String id, String version, String type) throws ResolveException
    {
        Matcher matcher = MavenUtils.PARSER_ID.matcher(id);
        if (!matcher.matches()) {
            throw new ResolveException("Bad id " + id + ", expected format is <groupId>:<artifactId>[:<classifier>]");
        }

        Dependency dependency = new Dependency();

        dependency.setGroupId(matcher.group(1));
        dependency.setArtifactId(matcher.group(2));
        if (matcher.group(4) != null) {
            dependency.setClassifier(StringUtils.defaultString(matcher.group(4), ""));
        }

        if (version != null) {
            dependency.setVersion(version);
        }

        if (type != null) {
            dependency.setType(type);
        }

        return dependency;
    }

    private String getArtifactId(DefaultCoreExtension extension) throws ResolveException
    {
        String artifactId;

        if (extension instanceof MavenExtension) {
            artifactId = ((MavenExtension) extension).getMavenArtifactId();
        } else {
            Matcher matcher = MavenUtils.PARSER_ID.matcher(extension.getId().getId());
            if (!matcher.matches()) {
                throw new ResolveException("Bad id " + extension.getId().getId()
                    + ", expected format is <groupId>:<artifactId>[:<classifier>]");
            }
            artifactId = matcher.group(2);
        }

        return artifactId;
    }

    private DefaultCoreExtension getCoreExension(URL descriptorURL, DefaultCoreExtensionRepository repository)
        throws Exception
    {
        DefaultCoreExtension coreExtension = this.cache.getExtension(repository, descriptorURL);

        if (coreExtension != null && coreExtension.getDescriptorURL().equals(descriptorURL)) {
            return coreExtension;
        }

        return parseMavenPom(descriptorURL, repository);
    }

    private DefaultCoreExtension parseMavenPom(URL descriptorURL, DefaultCoreExtensionRepository repository)
        throws Exception
    {
        InputStream descriptorStream = descriptorURL.openStream();
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model mavenModel = reader.read(descriptorStream);

            URL extensionURL = PathUtils.getExtensionURL(descriptorURL);

            Extension mavenExtension = this.converter.convert(Extension.class, mavenModel);

            DefaultCoreExtension coreExtension = new MavenCoreExtension(repository, extensionURL, mavenExtension);
            coreExtension.setDescriptorURL(descriptorURL);

            // If no parent is provided no need to resolve it to get more details
            if (mavenModel.getParent() == null) {
                this.cache.store(coreExtension);
                coreExtension.setCached(true);
            }

            return coreExtension;
        } finally {
            IOUtils.closeQuietly(descriptorStream);
        }
    }

    @Override
    public void updateExtensions(Collection<DefaultCoreExtension> extensions)
    {
        for (DefaultCoreExtension extension : extensions) {
            // If XWiki is stopping before this is finished then we need to exit.
            if (this.shouldStop) {
                SHUTDOWN_LOGGER.debug("Aborting Extension Update as XWiki is stopping");
                break;
            }

            if (!extension.isCached()) {
                try {
                    Extension remoteExtension = this.repositoryManager.resolve(extension.getId());

                    extension.set(remoteExtension);

                    // Cache it
                    if (extension.getDescriptorURL() != null) {
                        this.cache.store(extension);
                        extension.setCached(true);
                    }
                } catch (ResolveException e) {
                    this.logger.debug("Can't find remote extension with id [{}]", extension.getId(), e);
                } catch (Exception e) {
                    this.logger.warn("Failed to update core extension [{}]: [{}]", extension.getId(),
                        ExceptionUtils.getRootCauseMessage(e), e);
                }
            }
        }
    }

    @Override
    public Map<String, DefaultCoreExtension> loadExtensions(DefaultCoreExtensionRepository repository)
    {
        Map<String, DefaultCoreExtension> extensions = new HashMap<String, DefaultCoreExtension>();

        loadExtensionsFromClassloaders(extensions, repository);

        return extensions;
    }

    @Override
    public DefaultCoreExtension loadEnvironmentExtension(DefaultCoreExtensionRepository repository)
    {
        InputStream is = this.environment.getResourceAsStream("/META-INF/MANIFEST.MF");

        if (is != null) {
            // Probably running in an application server
            try {
                Manifest manifest = new Manifest(is);

                Attributes manifestAttributes = manifest.getMainAttributes();
                String extensionId = manifestAttributes.getValue(MavenUtils.MF_EXTENSION_ID);

                if (extensionId != null) {
                    String[] mavenId = StringUtils.split(extensionId, ':');

                    String descriptorPath = String.format("/META-INF/maven/%s/%s/pom.xml", mavenId[0], mavenId[1]);

                    URL descriptorURL = this.environment.getResource(descriptorPath);

                    if (descriptorURL != null) {
                        try {
                            DefaultCoreExtension coreExtension = parseMavenPom(descriptorURL, repository);

                            return coreExtension;
                        } catch (Exception e) {
                            this.logger.warn("Failed to parse extension descriptor [{}]", descriptorURL, e);
                        }
                    } else {
                        this.logger.warn("Can't find resource file [{}] which contains distribution informations.",
                            descriptorPath);
                    }
                }
            } catch (IOException e) {
                this.logger.error("Failed to parse environment META-INF/MANIFEST.MF file", e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }

        this.logger.debug("No declared environmennt extension");

        return null;
    }

    private void loadExtensionsFromClassloaders(Map<String, DefaultCoreExtension> extensions,
        DefaultCoreExtensionRepository repository)
    {
        Collection<URL> mavenURLs = ClasspathHelper.forPackage(MavenUtils.MAVENPACKAGE);

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setScanners(new ResourcesScanner());
        configurationBuilder.setUrls(mavenURLs);
        configurationBuilder.filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(MavenUtils.MAVENPACKAGE)));

        Reflections reflections = new Reflections(configurationBuilder);

        Set<String> descriptors = reflections.getResources(Predicates.equalTo("pom.xml"));

        for (String descriptor : descriptors) {
            URL descriptorURL = getClass().getClassLoader().getResource(descriptor);

            if (descriptorURL != null) {
                try {
                    DefaultCoreExtension coreExtension = getCoreExension(descriptorURL, repository);

                    DefaultCoreExtension existingCoreExtension = extensions.get(coreExtension.getId().getId());

                    if (existingCoreExtension == null) {
                        extensions.put(coreExtension.getId().getId(), coreExtension);
                    } else {
                        this.logger.warn("Collision between core extension [{} ({})] and [{} ({})]",
                            coreExtension.getId(), coreExtension.getDescriptorURL(), existingCoreExtension.getId(),
                            existingCoreExtension.getDescriptorURL());

                        if (coreExtension.getId().getVersion()
                            .compareTo(existingCoreExtension.getId().getVersion()) > 0) {
                            extensions.put(coreExtension.getId().getId(), coreExtension);

                            this.logger.warn("[{} ({})] is selected", coreExtension.getId(),
                                coreExtension.getDescriptorURL());
                        } else {
                            this.logger.warn("[{} ({})] is selected", existingCoreExtension.getId(),
                                existingCoreExtension.getDescriptorURL());
                        }
                    }
                } catch (Exception e) {
                    this.logger.warn("Failed to parse extension descriptor [{}] ([{}])", descriptorURL, descriptor, e);
                }
            } else {
                this.logger.error("Could not find resource URL for descriptor [{}]", descriptor);
            }
        }

        // Try to find more

        guess(extensions, repository);
    }

    private void guess(Map<String, DefaultCoreExtension> extensions, DefaultCoreExtensionRepository repository)
    {
        Set<ExtensionDependency> dependencies = new HashSet<ExtensionDependency>();

        Set<String> validatedFiles = new HashSet<String>();
        for (DefaultCoreExtension coreExtension : extensions.values()) {
            validatedFiles.add(coreExtension.getURL().toString());

            for (ExtensionDependency dependency : coreExtension.getDependencies()) {
                if (!extensions.containsKey(dependency.getId())) {
                    dependencies.add(dependency);
                }
            }
        }

        // Normalize and guess

        Map<String, Object[]> fileNames = new HashMap<String, Object[]>();
        Map<String, Object[]> guessedArtefacts = new HashMap<String, Object[]>();
        // ClasspathHelper.forClassLoader() get even the JARs that are made not reachable by the application server
        // So the trick is to get all resources in which we can access a META-INF folder
        Collection<URL> urls = ClasspathHelper.forPackage("META-INF");

        for (URL url : urls) {
            URL extensionURL;
            try {
                extensionURL = PathUtils.getExtensionURL(url);
            } catch (IOException e) {
                this.logger.error("Failed to convert to extension URL", e);
                continue;
            }

            if (!validatedFiles.contains(extensionURL.toString())) {
                try {
                    String path = extensionURL.getPath();
                    String filename = path.substring(path.lastIndexOf('/') + 1);
                    String type = null;

                    int extIndex = filename.lastIndexOf('.');
                    if (extIndex != -1) {
                        type = filename.substring(extIndex + 1);
                        filename = filename.substring(0, extIndex);
                    }

                    int index;
                    if (!filename.endsWith(MavenUtils.SNAPSHOTSUFFIX)) {
                        index = filename.lastIndexOf('-');
                    } else {
                        index = filename.lastIndexOf('-', filename.length() - MavenUtils.SNAPSHOTSUFFIX.length());
                    }

                    if (index != -1) {
                        fileNames.put(filename, new Object[] {extensionURL});

                        String artefactname = filename.substring(0, index);
                        String version = filename.substring(index + 1);

                        guessedArtefacts.put(artefactname, new Object[] {version, extensionURL, type});
                    }
                } catch (Exception e) {
                    this.logger.warn("Failed to parse resource name [{}]", extensionURL, e);
                }
            }
        }

        // Try to resolve version no easy to find from the pom.xml
        try {
            for (DefaultCoreExtension coreExtension : extensions.values()) {
                String artifactId = getArtifactId(coreExtension);

                Object[] artefact = guessedArtefacts.get(artifactId);

                if (artefact != null) {
                    if (coreExtension.getId().getVersion().getValue().charAt(0) == '$') {
                        coreExtension.setId(new ExtensionId(coreExtension.getId().getId(), (String) artefact[0]));
                        coreExtension.setGuessed(true);
                    }

                    if (coreExtension.getType().charAt(0) == '$') {
                        coreExtension.setType((String) artefact[2]);
                        coreExtension.setGuessed(true);
                    }
                }
            }

            // Add dependencies that does not provide proper pom.xml resource and can't be found in the classpath
            for (ExtensionDependency extensionDependency : dependencies) {
                Dependency dependency;

                if (extensionDependency instanceof MavenExtensionDependency) {
                    dependency = ((MavenExtensionDependency) extensionDependency).getMavenDependency();
                } else {
                    dependency = toDependency(extensionDependency.getId(),
                        extensionDependency.getVersionConstraint().getValue(), null);
                }

                String dependencyId = dependency.getGroupId() + ':' + dependency.getArtifactId();

                DefaultCoreExtension coreExtension = extensions.get(dependencyId);
                if (coreExtension == null) {
                    String dependencyFileName = dependency.getArtifactId() + '-' + dependency.getVersion();
                    if (dependency.getClassifier() != null) {
                        dependencyFileName += '-' + dependency.getClassifier();
                        dependencyId += ':' + dependency.getClassifier();
                    }

                    Object[] filenameArtifact = fileNames.get(dependencyFileName);
                    Object[] guessedArtefact = guessedArtefacts.get(dependency.getArtifactId());

                    if (filenameArtifact != null) {
                        coreExtension = new DefaultCoreExtension(repository, (URL) filenameArtifact[0],
                            new ExtensionId(dependencyId, dependency.getVersion()),
                            MavenUtils.packagingToType(dependency.getType()));
                        coreExtension.setGuessed(true);
                    } else if (guessedArtefact != null) {
                        coreExtension = new DefaultCoreExtension(repository, (URL) guessedArtefact[1],
                            new ExtensionId(dependencyId, (String) guessedArtefact[0]),
                            MavenUtils.packagingToType(dependency.getType()));
                        coreExtension.setGuessed(true);
                    }

                    if (coreExtension != null) {
                        extensions.put(dependencyId, coreExtension);
                    }
                }
            }
        } catch (Exception e) {
            this.logger.warn("Failed to guess extra information about some extensions", e);
        }
    }
}
