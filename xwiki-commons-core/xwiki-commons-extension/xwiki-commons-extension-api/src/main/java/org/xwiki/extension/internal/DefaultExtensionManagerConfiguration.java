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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.environment.Environment;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryId;

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

    // Cache

    /**
     * @see DefaultExtensionManagerConfiguration#getLocalRepository()
     */
    private File localRepository;

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
            String localRepositoryPath = this.configuration.get().getProperty("extension.localRepository");

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
            this.configuration.get().getProperty("extension.repositories", Collections.<String> emptyList());

        if (repositoryStrings.isEmpty()) {
            repositories = null;
        } else {
            Map<String, ExtensionRepositoryDescriptor> repositoriesMap =
                new LinkedHashMap<String, ExtensionRepositoryDescriptor>();
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
                        this.logger.warn("Faild to parse repository [" + repositoryString + "] from configuration", e);
                    }
                } else {
                    this.logger.debug("Empty repository id found in the configuration");
                }
            }

            repositories = repositoriesMap.values();
        }

        return repositories;
    }

    @Override
    public Collection<ExtensionRepositoryId> getRepositories()
    {
        Collection<ExtensionRepositoryId> repositories = new ArrayList<ExtensionRepositoryId>();

        for (ExtensionRepositoryDescriptor descriptor : getExtensionRepositoryDescriptors()) {
            repositories.add(new ExtensionRepositoryId(descriptor));
        }

        return repositories;
    }

    /**
     * Create a {@link ExtensionRepositoryDescriptor} from a string entry.
     * 
     * @param repositoryString the repository configuration entry
     * @return the {@link ExtensionRepositoryDescriptor}
     * @throws URISyntaxException Failed to create an {@link URI} object from the configuration entry
     * @throws ExtensionManagerConfigurationException Failed to parse configuration
     */
    private ExtensionRepositoryDescriptor parseRepository(String repositoryString) throws URISyntaxException,
        ExtensionManagerConfigurationException
    {
        Matcher matcher = REPOSITORYIDPATTERN.matcher(repositoryString);

        if (matcher.matches()) {
            return new ExtensionRepositoryDescriptor(matcher.group(1), matcher.group(2), new URI(matcher.group(3)));
        }

        throw new ExtensionManagerConfigurationException("Don't match repository configuration [" + repositoryString
            + "]");
    }

    @Override
    public String getUserAgent()
    {
        // TODO: add version (need a way to get platform version first)
        return this.configuration.get().getProperty("extension.userAgent", DEFAULT_USERAGENT);
    }
}
