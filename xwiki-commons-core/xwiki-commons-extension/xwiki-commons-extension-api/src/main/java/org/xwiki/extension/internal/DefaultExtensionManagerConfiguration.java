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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.environment.Environment;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.properties.ConverterManager;

/**
 * Default implementation of {@link ExtensionManagerConfiguration}.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultExtensionManagerConfiguration implements ExtensionManagerConfiguration
{
    /**
     * Used to parse repositories entries from the configuration.
     */
    private static final Pattern REPOSITORYIDPATTERN = Pattern.compile("([^:]+):([^:]+):(.+)");

    /**
     * The default user agent.
     */
    private static final String DEFAULT_USERAGENT = "XWikiExtensionManager";

    /**
     * The prefix of all the extension related properties.
     */
    private static final String CK_PREFIX = "extension.";

    /**
     * The prefix of all the core extension related properties.
     */
    private static final String CK_CORE_PREFIX = CK_PREFIX + "core.";

    /**
     * The prefix of all the extension repository related properties.
     */
    private static final String CK_REPOSITORIES_PREFIX = CK_PREFIX + "repositories.";

    private static final String CK_IGNORED_DEPENDENCIES = CK_PREFIX + "ignoredDependencies";

    private static final Set<String> DEFAULT_IGNORED_DEPENDENCIES =
        new HashSet<>(Arrays.asList("stax:stax", "javax.xml.stream:stax-api", "stax:stax-api", "xalan:xalan",
            "xalan:serializer", "xml-apis:xml-apis", "xerces:xmlParserAPIs"));

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Used to get permanent directory.
     */
    @Inject
    private Environment environment;

    /**
     * The configuration.
     */
    @Inject
    private Provider<ConfigurationSource> configuration;

    @Inject
    private ExtensionFactory extensionFactory;

    @Inject
    private Provider<CoreExtensionRepository> coreExtensionRepository;

    @Inject
    private ConverterManager converter;

    // Cache

    /**
     * @see DefaultExtensionManagerConfiguration#getLocalRepository()
     */
    private File localRepository;

    private List<RecommendedVersion> recommendedVersions;

    private static class RecommendedVersion
    {
        private Pattern idPattern;

        private VersionConstraint versionConstraint;

        RecommendedVersion(Pattern idPattern, VersionConstraint version)
        {
            this.idPattern = idPattern;
            this.versionConstraint = version;
        }

        boolean matches(String id)
        {
            return this.idPattern.matcher(id).matches();
        }

        public VersionConstraint getVersionConstraint()
        {
            return this.versionConstraint;
        }
    }

    /**
     * @return extension manage home folder
     */
    public File getHome()
    {
        return new File(this.environment.getPermanentDirectory(), "extension/");
    }

    @Override
    public File getLocalRepository()
    {
        if (this.localRepository == null) {
            String localRepositoryPath = this.configuration.get().getProperty(CK_PREFIX + "localRepository");

            if (localRepositoryPath == null) {
                this.localRepository = new File(getHome(), "repository/");
            } else {
                this.localRepository = new File(localRepositoryPath);
            }
        }

        return this.localRepository;
    }

    @Override
    public Collection<ExtensionRepositoryDescriptor> getExtensionRepositoryDescriptors()
    {
        Collection<ExtensionRepositoryDescriptor> repositories;

        List<String> repositoryStrings =
            this.configuration.get().getProperty(CK_PREFIX + "repositories", Collections.emptyList());

        if (repositoryStrings.isEmpty()) {
            repositories = null;
        } else {
            Map<String, ExtensionRepositoryDescriptor> repositoriesMap = new LinkedHashMap<>();
            for (String repositoryString : repositoryStrings) {
                if (StringUtils.isNotBlank(repositoryString)) {
                    try {
                        ExtensionRepositoryDescriptor extensionRepositoryId = parseRepository(repositoryString);
                        if (repositoriesMap.containsKey(extensionRepositoryId.getId())) {
                            this.logger.warn(
                                "Duplicated repository id in [{}] first found in [{}]. The last one will be used.",
                                extensionRepositoryId, repositoriesMap.get(extensionRepositoryId.getId()));
                        }
                        repositoriesMap.put(extensionRepositoryId.getId(), extensionRepositoryId);
                    } catch (Exception e) {
                        this.logger.warn("Ignoring invalid repository configuration [{}]. " + "Root cause [{}]",
                            repositoryString, ExceptionUtils.getRootCauseMessage(e));
                    }
                } else {
                    this.logger.debug("Empty repository id found in the configuration");
                }
            }

            repositories = repositoriesMap.values();

            // Get extended properties

            for (ExtensionRepositoryDescriptor descriptor : repositories) {
                setRepositoryProperties((DefaultExtensionRepositoryDescriptor) descriptor);
            }
        }

        return repositories;
    }

    /**
     * @param descriptor the repository descriptor to update with custom properties
     */
    private void setRepositoryProperties(DefaultExtensionRepositoryDescriptor descriptor)
    {
        String id = descriptor.getId();

        String prefix = CK_REPOSITORIES_PREFIX + id + '.';

        ConfigurationSource configurationSource = this.configuration.get();

        for (String key : configurationSource.getKeys()) {
            if (key.startsWith(prefix)) {
                descriptor.putProperty(key.substring(prefix.length()),
                    configurationSource.getProperty(key, String.class));
            }
        }
    }

    @Override
    public Collection<ExtensionRepositoryId> getRepositories()
    {
        Collection<ExtensionRepositoryId> repositories = new ArrayList<>();

        for (ExtensionRepositoryDescriptor descriptor : getExtensionRepositoryDescriptors()) {
            repositories.add(new ExtensionRepositoryId(descriptor));
        }

        return repositories;
    }

    /**
     * Create a {@link DefaultExtensionRepositoryDescriptor} from a string entry.
     *
     * @param repositoryString the repository configuration entry
     * @return the {@link DefaultExtensionRepositoryDescriptor}
     * @throws URISyntaxException Failed to create an {@link URI} object from the configuration entry
     * @throws ExtensionManagerConfigurationException Failed to parse configuration
     */
    private ExtensionRepositoryDescriptor parseRepository(String repositoryString)
        throws URISyntaxException, ExtensionManagerConfigurationException
    {
        Matcher matcher = REPOSITORYIDPATTERN.matcher(repositoryString);

        if (matcher.matches()) {
            return this.extensionFactory.getExtensionRepositoryDescriptor(matcher.group(1), matcher.group(2),
                new URI(matcher.group(3)), null);
        }

        throw new ExtensionManagerConfigurationException(
            String.format("Invalid repository configuration format for [%s]. Should have been matching [%s].",
                repositoryString, REPOSITORYIDPATTERN.toString()));
    }

    @Override
    public String getUserAgent()
    {
        // TODO: add version (need a way to get platform version first)
        return this.configuration.get().getProperty(CK_PREFIX + "userAgent", DEFAULT_USERAGENT);
    }

    @Override
    public boolean resolveCoreExtensions()
    {
        return this.configuration.get().getProperty(CK_CORE_PREFIX + "resolve", true);
    }

    protected List<String> getRecommendedVersions()
    {
        // Try configuration
        Object configurationValue = this.configuration.get().getProperty(CK_PREFIX + "recommendedVersions");
        if (configurationValue != null) {
            if (configurationValue instanceof List) {
                return (List) configurationValue;
            } else {
                return ExtensionUtils.importPropertyStringList(configurationValue.toString(), true);
            }
        }

        // Try environment extension
        CoreExtensionRepository repository = this.coreExtensionRepository.get();

        CoreExtension environmentExtension = repository.getEnvironmentExtension();

        if (environmentExtension != null) {
            String listString = environmentExtension.getProperty("xwiki.extension.recommendedVersions");
            return ExtensionUtils.importPropertyStringList(listString, true);
        }

        return null;
    }

    private RecommendedVersion getRecomendedVersion(String id)
    {
        if (this.recommendedVersions == null) {
            List<String> list = getRecommendedVersions();
            if (list != null) {
                List<ExtensionId> extensions = this.converter.convert(ExtensionId.TYPE_LIST, list);

                List<RecommendedVersion> versions = new ArrayList<>(extensions.size());
                for (ExtensionId extensionId : extensions) {
                    versions.add(new RecommendedVersion(Pattern.compile(extensionId.getId()),
                        this.extensionFactory.getVersionConstraint(extensionId.getVersion().getValue())));
                }

                this.recommendedVersions = versions;
            } else {
                this.recommendedVersions = Collections.emptyList();
            }

        }

        // Searching matching recommended version
        for (RecommendedVersion version : this.recommendedVersions) {
            if (version.matches(id)) {
                return version;
            }
        }

        return null;

    }

    @Override
    public VersionConstraint getRecomendedVersionConstraint(String id)
    {
        RecommendedVersion recommendedVersion = getRecomendedVersion(id);

        if (recommendedVersion != null) {
            return recommendedVersion.getVersionConstraint();
        }

        return null;
    }

    @Override
    public VersionConstraint getRecomendedVersionConstraint(String id, VersionConstraint defaultVersion)
    {
        VersionConstraint constraint = getRecomendedVersionConstraint(id);

        if (constraint != null && !constraint.equals(defaultVersion)) {
            return constraint;
        }

        return null;
    }

    @Override
    public boolean isIgnoredDependency(ExtensionDependency dependency)
    {
        Set<String> ignoredDependencies =
            this.configuration.get().getProperty(CK_IGNORED_DEPENDENCIES, DEFAULT_IGNORED_DEPENDENCIES);

        return ignoredDependencies.contains(dependency.getId());
    }
}
