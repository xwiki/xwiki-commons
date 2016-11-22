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
package org.xwiki.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;

/**
 * Represent an extension.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface Extension extends Comparable<Extension>
{
    /**
     * @see #getRepository()
     * @since 7.0RC1
     */
    String FIELD_REPOSITORY = "repository";

    /**
     * @see #getId()
     * @since 7.0RC1
     */
    String FIELD_ID = "id";

    /**
     * @see #getId()
     * @since 7.0RC1
     */
    String FIELD_VERSION = "version";

    /**
     * @see #getFeatures()
     * @since 7.0RC1
     * @deprecated since 9.0RC1/8.4.2, use {@link #FIELD_FEATURE} instead
     */
    String FIELD_FEATURE = "feature";

    /**
     * @see #getFeatures()
     * @since 7.0RC1
     * @deprecated since 9.0RC1/8.4.2, use {@link #FIELD_FEATURES} instead
     */
    String FIELD_FEATURES = "features";

    /**
     * @see #getFeatures()
     * @since 9.0RC1
     * @since 8.4.2
     */
    String FIELD_EXTENSIONFEATURE = "extensionfeature";

    /**
     * @see #getFeatures()
     * @since 9.0RC1
     * @since 8.4.2
     */
    String FIELD_EXTENSIONFEATURES = "extensionfeatures";

    /**
     * @see #getSummary()
     * @since 7.0RC1
     */
    String FIELD_SUMMARY = "summary";

    /**
     * @see #getDescription()
     * @since 7.0RC1
     */
    String FIELD_DESCRIPTION = "description";

    /**
     * @see #getAuthors()
     * @since 7.0RC1
     */
    String FIELD_AUTHOR = "author";

    /**
     * @see #getAuthors()
     * @since 7.0RC1
     */
    String FIELD_AUTHORS = "authors";

    /**
     * @see #getCategory()
     * @since 7.0RC1
     */
    String FIELD_CATEGORY = "category";

    /**
     * @see #getLicenses()
     * @since 7.0RC1
     */
    String FIELD_LICENSE = "license";

    /**
     * @see #getLicenses()
     * @since 7.0RC1
     */
    String FIELD_LICENSES = "licenses";

    /**
     * @see #getName()
     * @since 7.0RC1
     */
    String FIELD_NAME = "name";

    /**
     * @see #getType()
     * @since 7.0RC1
     */
    String FIELD_TYPE = "type";

    /**
     * @see #getWebSite()
     * @since 7.0RC1
     */
    String FIELD_WEBSITE = "website";

    /**
     * @see #getAllowedNamespaces()
     * @since 8.3M1
     */
    String FIELD_NAMESPACES = "namespaces";

    /**
     * @see #getAllowedNamespaces()
     * @since 8.0M1
     */
    String FIELD_ALLOWEDNAMESPACE = "allowednamespace";

    /**
     * @see #getAllowedNamespaces()
     * @since 8.0M1
     */
    String FIELD_ALLOWEDNAMESPACES = "allowednamespaces";

    /**
     * @see #getScm()
     * @since 7.0RC1
     */
    String FIELD_SCM = "scm";

    /**
     * @see #getRepositories()
     * @since 7.3M1
     */
    String FIELD_REPOSITORIES = "repositories";

    /**
     * @see #getProperties()
     * @since 7.4.3
     * @since 8.0.1
     * @since 8.1M1
     */
    String FIELD_PROPERTIES = "properties";

    /**
     * @see #getIssueManagement()
     * @since 9.0RC1
     * @since 8.4.2
     */
    String FIELD_ISSUEMANAGEMENT = "issuemanagement";

    /**
     * @see #getManagedDependencies()
     * @since 9.0RC1
     * @since 8.4.2
     */
    String FIELD_MANAGEDDEPENDENCIES = "manageddependencies";

    /**
     * @see #getDependencies()
     * @since 9.0RC1
     * @since 8.4.2
     */
    String FIELD_DEPENDENCIES = "dependencies";

    /**
     * Prefix to use for custom properties names in external systems (Maven, etc).
     * 
     * @since 8.3M1
     */
    String IKEYPREFIX = "xwiki.extension.";

    /**
     * Get an extension field (name, summary, id, etc.) by name. Fallback on properties.
     * 
     * @param <T> type of the field value
     * @param fieldName the field name;
     * @return the field value or null if none could be found
     * @since 7.0RC1
     */
    <T> T get(String fieldName);

    /**
     * @return the id/version combination which makes the extension unique
     */
    ExtensionId getId();

    /**
     * Indicate in an extension a list of provided "functionalities". Then when resolving extensions dependencies they
     * can be matched in this list.
     *
     * @return the extension ids also provided by this extension, an empty collection if there is none
     * @deprecated since 8.0M1, use {@link #getExtensionFeatures()} instead
     */
    @Deprecated
    Collection<String> getFeatures();

    /**
     * Indicate in an extension a list of provided "functionalities". Then when resolving extensions dependencies they
     * can be matched in this list.
     * 
     * @return the {@link ExtensionId}s also provided by this extension, an empty collection if there is none
     * @since 8.0M1
     */
    default Collection<ExtensionId> getExtensionFeatures()
    {
        Collection<String> features = getFeatures();
        List<ExtensionId> extensionFeatures = new ArrayList<ExtensionId>(features.size());
        for (String feature : features) {
            extensionFeatures.add(new ExtensionId(feature, getId().getVersion()));
        }

        return extensionFeatures;
    }

    /**
     * Return the {@link ExtensionId} object that matches the passed feature id.
     * 
     * @param featureId the id of the feature
     * @return the {@link ExtensionId} associated to the passed id
     * @since 8.0M1
     */
    default ExtensionId getExtensionFeature(String featureId)
    {
        return getFeatures().contains(featureId) ? new ExtensionId(featureId, getId().getVersion()) : null;
    }

    /**
     * @return the type of the extension
     */
    String getType();

    /**
     * @return the display name of the extension
     */
    String getName();

    /**
     * @return the license of the extension, an empty collection if there is none
     */
    Collection<ExtensionLicense> getLicenses();

    /**
     * @return a short description of the extension
     */
    String getSummary();

    /**
     * @return a description of the extension
     */
    String getDescription();

    /**
     * @return an URL for the extension website
     */
    String getWebSite();

    /**
     * @return the extension authors, an empty collection if there is none
     */
    Collection<ExtensionAuthor> getAuthors();

    /**
     * @return the namespaces where it's allowed to install this extension
     * @since 8.0M1
     */
    default Collection<String> getAllowedNamespaces()
    {
        return Collections.emptyList();
    }

    /**
     * @return the dependencies of the extension, an empty collection if there is none
     */
    Collection<ExtensionDependency> getDependencies();

    /**
     * Managed dependencies are used to override transitive dependencies (usually the version of this transitive
     * dependency).
     * 
     * @return the managed dependencies, empty list if there is none
     * @since 8.1M1
     */
    default Collection<ExtensionDependency> getManagedDependencies()
    {
        return Collections.emptyList();
    }

    /**
     * Return extension file descriptor. Also allows to get the content of the file.
     *
     * @return the file of the extension
     */
    ExtensionFile getFile();

    /**
     * @return the repository of the extension
     */
    ExtensionRepository getRepository();

    /**
     * @return informations related to extensions's source control management
     * @since 6.3M1
     */
    ExtensionScm getScm();

    /**
     * @return informations related to extension's issues management
     * @since 6.3M1
     */
    ExtensionIssueManagement getIssueManagement();

    /**
     * @return the category of the extension
     * @since 7.0M2
     */
    String getCategory();

    /**
     * @return the custom repositories provided by the extension (usually to resolve dependencies)
     * @since 7.3M1
     */
    Collection<ExtensionRepositoryDescriptor> getRepositories();

    // Custom properties

    /**
     * Extends {@link Extension} standard properties.
     * <p>
     * Theses are generally provided by specific repositories. For example a maven repository will provide group and
     * artifacts ids.
     *
     * @return the properties
     */
    Map<String, Object> getProperties();

    /**
     * @param <T> type of the property value
     * @param key the property key
     * @return the property value
     */
    <T> T getProperty(String key);

    /**
     * Get a property.
     *
     * @param <T> type of the property value
     * @param key the property key
     * @param def the value to return if no property is associated to the provided key
     * @return the property value or <code>default</code> of the property is not found
     * @see #getProperty(String)
     */
    <T> T getProperty(String key, T def);
}
