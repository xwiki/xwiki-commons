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
package org.xwiki.extension.repository.xwiki.internal;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.xwiki.extension.AbstractRatingExtension;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionIssueManagement;
import org.xwiki.extension.DefaultExtensionScm;
import org.xwiki.extension.DefaultExtensionScmConnection;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.extension.internal.ExtensionUtils;
import org.xwiki.extension.rating.DefaultExtensionRating;
import org.xwiki.extension.rating.RatingExtension;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionAuthor;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionDependency;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionIssueManagement;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionRating;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionRepository;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionScm;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionScmConnection;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionVersion;
import org.xwiki.extension.repository.xwiki.model.jaxb.License;
import org.xwiki.extension.repository.xwiki.model.jaxb.Namespaces;
import org.xwiki.extension.repository.xwiki.model.jaxb.Property;
import org.xwiki.extension.version.internal.DefaultVersion;

/**
 * XWiki Repository implementation of {@link org.xwiki.extension.Extension}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class XWikiExtension extends AbstractRatingExtension implements RatingExtension
{
    public XWikiExtension(XWikiExtensionRepository repository, ExtensionVersion restExtension,
        ExtensionLicenseManager licenseManager)
    {
        super(repository, new ExtensionId(restExtension.getId(), restExtension.getVersion()), restExtension.getType());

        setName(restExtension.getName());
        setSummary(restExtension.getSummary());
        setWebsite(restExtension.getWebsite());

        setDescription(restExtension.getDescription());

        // Features
        for (org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionId feature : restExtension
            .getExtensionFeatures()) {
            addExtensionFeature(new ExtensionId(feature.getId(),
                feature.getVersion() != null ? new DefaultVersion(feature.getVersion()) : getId().getVersion()));
        }

        // Rating
        ExtensionRating restRating = restExtension.getRating();
        if (restRating != null) {
            setRating(
                new DefaultExtensionRating(restRating.getTotalVotes(), restRating.getAverageVote(), getRepository()));
        }

        // Authors
        for (ExtensionAuthor restAuthor : restExtension.getAuthors()) {
            URL url;
            try {
                url = new URL(restAuthor.getUrl());
            } catch (MalformedURLException e) {
                url = null;
            }

            addAuthor(new DefaultExtensionAuthor(restAuthor.getName(), url));
        }

        // License

        for (License restLicense : restExtension.getLicenses()) {
            if (restLicense.getName() != null) {
                ExtensionLicense extensionLicense = licenseManager.getLicense(restLicense.getName());
                if (extensionLicense != null) {
                    addLicense(extensionLicense);
                } else {
                    List<String> content = null;
                    if (restLicense.getContent() != null) {
                        try {
                            content = IOUtils.readLines(new StringReader(restLicense.getContent()));
                        } catch (IOException e) {
                            // That should never happen
                        }
                    }

                    addLicense(new ExtensionLicense(restLicense.getName(), content));
                }
            }
        }

        // Scm

        ExtensionScm restSCM = restExtension.getScm();
        if (restSCM != null) {
            DefaultExtensionScmConnection connection = toDefaultExtensionScmConnection(restSCM.getConnection());
            DefaultExtensionScmConnection developerConnection =
                toDefaultExtensionScmConnection(restSCM.getDeveloperConnection());

            setScm(new DefaultExtensionScm(restSCM.getUrl(), connection, developerConnection));
        }

        // Issue management

        ExtensionIssueManagement restIssueManagement = restExtension.getIssueManagement();
        if (restIssueManagement != null) {
            setIssueManagement(
                new DefaultExtensionIssueManagement(restIssueManagement.getSystem(), restIssueManagement.getUrl()));
        }

        // Category
        setCategory(restExtension.getCategory());

        // Allowed namespaces
        Namespaces namespaces = restExtension.getAllowedNamespaces();
        if (namespaces != null) {
            setAllowedNamespaces(namespaces.getNamespaces());
        }

        // Recommended
        setRecommended(restExtension.isRecommended());

        // Properties
        for (Property restProperty : restExtension.getProperties()) {
            putProperty(restProperty.getKey(), restProperty.getStringValue());
        }

        // Make sure the dependency will be resolved in the extension repository first
        if (repository != null) {
            addRepository(repository.getDescriptor());
        }

        // Repositories
        for (ExtensionRepository restRepository : restExtension.getRepositories()) {
            try {
                addRepository(toDefaultExtensionRepositoryDescriptor(restRepository));
            } catch (URISyntaxException e) {
                // TODO: Log something ?
            }
        }

        // Dependencies
        setDependencies(toXWikiExtensionDependencies(restExtension.getDependencies()));

        // Managed dependencies
        setManagedDependencies(toXWikiExtensionDependencies(restExtension.getManagedDependencies()));

        // File

        setFile(new XWikiExtensionFile(repository, getId()));

        // XWiki Repository often act as a proxy and the source extension might have more information that the XWiki
        // Repository version supports
        setName(ExtensionUtils.importProperty(this, Extension.FIELD_NAME, getName()));
        setSummary(ExtensionUtils.importProperty(this, Extension.FIELD_SUMMARY, getSummary()));
        setWebsite(ExtensionUtils.importProperty(this, Extension.FIELD_WEBSITE, getWebSite()));
        setAllowedNamespaces(ExtensionUtils.importProperty(this, Extension.FIELD_NAMESPACES, getAllowedNamespaces()));
    }

    private Collection<XWikiExtensionDependency> toXWikiExtensionDependencies(
        List<ExtensionDependency> restDependencies)
    {
        List<XWikiExtensionDependency> newDependencies = new ArrayList<>(restDependencies.size());

        for (ExtensionDependency dependency : restDependencies) {
            newDependencies.add(new XWikiExtensionDependency(dependency,
                this.repository != null ? this.repository.getDescriptor() : null));
        }

        return newDependencies;
    }

    protected static DefaultExtensionScmConnection toDefaultExtensionScmConnection(ExtensionScmConnection connection)
    {
        if (connection != null) {
            return new DefaultExtensionScmConnection(connection.getSystem(), connection.getPath());
        } else {
            return null;
        }
    }

    protected static DefaultExtensionRepositoryDescriptor toDefaultExtensionRepositoryDescriptor(
        ExtensionRepository restRepository) throws URISyntaxException
    {
        return new DefaultExtensionRepositoryDescriptor(restRepository.getId(), restRepository.getType(),
            new URI(restRepository.getUri()));
    }

    @Override
    public XWikiExtensionRepository getRepository()
    {
        return (XWikiExtensionRepository) super.getRepository();
    }
}
