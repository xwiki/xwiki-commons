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
package org.xwiki.extension.internal.converter;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.IssueManagement;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Scm;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.DefaultExtensionIssueManagement;
import org.xwiki.extension.DefaultExtensionScm;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.extension.ExtensionScmConnection;
import org.xwiki.extension.internal.ExtensionUtils;
import org.xwiki.extension.internal.maven.DefaultMavenExtension;
import org.xwiki.extension.internal.maven.DefaultMavenExtensionDependency;
import org.xwiki.extension.internal.maven.MavenExtension;
import org.xwiki.extension.internal.maven.MavenUtils;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;

/**
 * Create an {@link Extension} from a Maven {@link Model}.
 * 
 * @version $Id$
 * @since 7.0M1
 */
@Component
@Singleton
public class ExtensionConverter extends AbstractConverter<Extension>
{
    @Inject
    private ExtensionLicenseManager licenseManager;

    @Override
    protected <G extends Extension> G convertToType(Type targetType, Object value)
    {
        if (value instanceof Model) {
            return (G) convertToExtension((Model) value);
        }

        throw new ConversionException(String.format("Unsupported target type [%s]", targetType));
    }

    private MavenExtension convertToExtension(Model model)
    {
        Properties properties = (Properties) model.getProperties().clone();

        String version = MavenUtils.resolveVersion(model);
        String groupId = MavenUtils.resolveGroupId(model);

        DefaultMavenExtension extension = new DefaultMavenExtension(null, groupId, model.getArtifactId(), version,
            MavenUtils.packagingToType(model.getPackaging()));

        extension.setName(getPropertyString(properties, Extension.FIELD_NAME, true, model.getName()));
        extension.setSummary(getPropertyString(properties, Extension.FIELD_SUMMARY, true, model.getDescription()));
        extension.setWebsite(getPropertyString(properties, Extension.FIELD_WEBSITE, true, model.getUrl()));

        // authors
        for (Developer developer : model.getDevelopers()) {
            URL authorURL = null;
            if (developer.getUrl() != null) {
                try {
                    authorURL = new URL(developer.getUrl());
                } catch (MalformedURLException e) {
                    // TODO: log ?
                }
            }

            extension.addAuthor(new DefaultExtensionAuthor(
                StringUtils.defaultIfBlank(developer.getName(), developer.getId()), authorURL));
        }

        // licenses
        for (License license : model.getLicenses()) {
            extension.addLicense(getExtensionLicense(license));
        }

        // scm
        Scm scm = model.getScm();
        if (scm != null
            && (scm.getConnection() != null || scm.getDeveloperConnection() != null || scm.getUrl() != null)) {
            ExtensionScmConnection connection = MavenUtils.toExtensionScmConnection(scm.getConnection());
            ExtensionScmConnection developerConnection =
                MavenUtils.toExtensionScmConnection(scm.getDeveloperConnection());

            extension.setScm(new DefaultExtensionScm(scm.getUrl(), connection, developerConnection));
        }

        // issue management
        IssueManagement issueManagement = model.getIssueManagement();
        if (issueManagement != null && issueManagement.getUrl() != null) {
            extension.setIssueManagement(
                new DefaultExtensionIssueManagement(issueManagement.getSystem(), issueManagement.getUrl()));
        }

        // features
        String featuresString = getProperty(properties, Extension.FIELD_FEATURES, true);
        if (StringUtils.isNotBlank(featuresString)) {
            Collection<String> features = ExtensionUtils.importPropertyStringList(featuresString, true);
            for (String feature : features) {
                extension
                    .addExtensionFeature(ExtensionIdConverter.toExtensionId(feature, extension.getId().getVersion()));
            }
        }

        // category
        String categoryString = getProperty(properties, Extension.FIELD_CATEGORY, true);
        if (StringUtils.isNotBlank(categoryString)) {
            extension.setCategory(categoryString);
        }

        // namespaces
        String namespacesString = getProperty(properties, Extension.FIELD_NAMESPACES, true);
        if (StringUtils.isNotBlank(namespacesString)) {
            Collection<String> namespaces = ExtensionUtils.importPropertyStringList(namespacesString, true);
            extension.setAllowedNamespaces(namespaces);
        }

        // repositories
        List<ExtensionRepositoryDescriptor> repositories;
        List<Repository> mavenRepositories = model.getRepositories();
        if (!mavenRepositories.isEmpty()) {
            repositories = new ArrayList<>(mavenRepositories.size());

            for (Repository mavenRepository : mavenRepositories) {
                try {
                    repositories.add(new DefaultExtensionRepositoryDescriptor(mavenRepository.getId(), "maven",
                        new URI(mavenRepository.getUrl())));
                } catch (URISyntaxException e) {
                    // TODO: log ?
                }
            }
        } else {
            repositories = null;
        }
        extension.setRepositories(repositories);

        // dependencies
        for (Dependency mavenDependency : model.getDependencies()) {
            if (!mavenDependency.isOptional() && (mavenDependency.getScope() == null
                || mavenDependency.getScope().equals("compile") || mavenDependency.getScope().equals("runtime"))) {
                DefaultExtensionDependency extensionDependency = toExtensionDependency(mavenDependency, model);

                extensionDependency.setRepositories(repositories);
                extension.addDependency(extensionDependency);
            }
        }

        // managed dependencies
        if (model.getDependencyManagement() != null) {
            for (Dependency mavenDependency : model.getDependencyManagement().getDependencies()) {
                DefaultExtensionDependency extensionDependency = toExtensionDependency(mavenDependency, model);

                extensionDependency.setRepositories(repositories);
                extension.addManagedDependency(extensionDependency);
            }
        }

        // various properties

        extension.putProperty(MavenUtils.PKEY_MAVEN_MODEL, model);

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith("xwiki.extension.")) {
                extension.putProperty(key, entry.getValue());
            }
        }

        return extension;
    }

    private DefaultExtensionDependency toExtensionDependency(Dependency mavenDependency, Model model)
    {
        String dependencyGroupId = MavenUtils.resolveGroupId(mavenDependency.getGroupId(), model, true);
        String dependencyArtifactId = mavenDependency.getArtifactId();
        String dependencyClassifier = mavenDependency.getClassifier();
        String dependencyVersion = MavenUtils.resolveVersion(mavenDependency.getVersion(), model, true);

        DefaultMavenExtensionDependency dependency = new DefaultMavenExtensionDependency(
            MavenUtils.toExtensionId(dependencyGroupId, dependencyArtifactId, dependencyClassifier),
            new DefaultVersionConstraint(dependencyVersion), mavenDependency);

        return dependency;
    }

    private String getProperty(Properties properties, String propertyName, boolean delete)
    {
        return delete ? (String) properties.remove(Extension.IKEYPREFIX + propertyName)
            : properties.getProperty(Extension.IKEYPREFIX + propertyName);
    }

    private String getPropertyString(Properties properties, String propertyName, boolean delete, String def)
    {
        return StringUtils.defaultString(getProperty(properties, propertyName, delete), def);
    }

    // TODO: download custom licenses content
    private ExtensionLicense getExtensionLicense(License license)
    {
        if (license.getName() == null) {
            return new ExtensionLicense("noname", null);
        }

        return createLicenseByName(license.getName());
    }

    private ExtensionLicense createLicenseByName(String name)
    {
        ExtensionLicense extensionLicense = this.licenseManager.getLicense(name);

        return extensionLicense != null ? extensionLicense : new ExtensionLicense(name, null);
    }
}
