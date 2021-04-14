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

import java.util.Collection;
import java.util.Map;

import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;

/**
 * Mutable version of {@link Extension}.
 *
 * @version $Id$
 * @since 8.4RC1
 */
public interface MutableExtension extends Extension
{
    /**
     * Update optional informations based on the provided extension.
     *
     * @param extension the extension from which to get informations
     */
    void set(Extension extension);

    /**
     * @param features the extension ids also provided by this extension
     * @deprecated since 8.0M1, use {@link #setExtensionFeatures(Collection)} instead
     */
    @Deprecated
    void setFeatures(Collection<String> features);

    /**
     * Add a new feature to the extension.
     *
     * @param feature a feature name
     * @deprecated since 8.0M1, use {@link #addExtensionFeature(ExtensionId)} instead
     */
    @Deprecated
    void addFeature(String feature);

    /**
     * @param features the {@link ExtensionId}s also provided by this extension
     */
    void setExtensionFeatures(Collection<ExtensionId> features);

    /**
     * Add a new feature to the extension.
     *
     * @param feature a feature name
     */
    void addExtensionFeature(ExtensionId feature);

    /**
     * @param name the display name of the extension
     */
    void setName(String name);

    /**
     * @param licenses the licenses of the extension
     */
    void setLicenses(Collection<ExtensionLicense> licenses);

    /**
     * Add a new license to the extension.
     *
     * @param license a license
     */
    void addLicense(ExtensionLicense license);

    /**
     * @param summary a short description of the extension
     */
    void setSummary(String summary);

    /**
     * @param description a description of the extension
     */
    void setDescription(String description);

    /**
     * @param authors the authors of the extension
     */
    void setAuthors(Collection<? extends ExtensionAuthor> authors);

    /**
     * Add a new author to the extension.
     *
     * @param author an author
     */
    void addAuthor(ExtensionAuthor author);

    /**
     * @param component the components provided by the extension
     * @since 13.3RC1
     */
    default void setComponents(Collection<? extends ExtensionComponent> component)
    {

    }

    /**
     * Add a new component to the extension.
     *
     * @param component a component provided by the extension
     * @since 13.3RC1
     */
    default void addComponent(ExtensionComponent component)
    {

    }

    /**
     * @param website an URL for the extension website
     */
    void setWebsite(String website);

    /**
     * Add a new allowed namespace to the extension.
     *
     * @param namespace a namespace
     */
    void addAllowedNamespace(String namespace);

    /**
     * @param namespaces the namespaces where it's allowed to install this extension
     */
    void setAllowedNamespaces(Collection<String> namespaces);

    /**
     * Add a new dependency to the extension.
     *
     * @param dependency a dependency
     */
    void addDependency(ExtensionDependency dependency);

    /**
     * @param dependencies the dependencies of the extension
     * @see #getDependencies()
     */
    void setDependencies(Collection<? extends ExtensionDependency> dependencies);

    /**
     * Add a new managed dependency to the extension.
     *
     * @param managedDependency a managed dependency;
     */
    void addManagedDependency(ExtensionDependency managedDependency);

    /**
     * @param managedDependencies the managed dependencies of the extension
     * @see #getManagedDependencies()
     */
    void setManagedDependencies(Collection<? extends ExtensionDependency> managedDependencies);

    /**
     * @param scm informations related to extensions's Source Control Management;
     */
    void setScm(ExtensionScm scm);

    /**
     * @param issueManagement informations related to extension's issues management
     */
    void setIssueManagement(ExtensionIssueManagement issueManagement);

    /**
     * @param categrory the category of the extension;
     */
    void setCategory(String categrory);

    /**
     * @param repositories the custom repositories provided by the extension (usually to resolve dependencies)
     */
    void setRepositories(Collection<? extends ExtensionRepositoryDescriptor> repositories);

    /**
     * Add a new repository to the extension.
     *
     * @param repository a repository descriptor;
     */
    void addRepository(ExtensionRepositoryDescriptor repository);

    /**
     * Set a property.
     *
     * @param key the property key
     * @param value the property value
     * @see #getProperty(String)
     */
    void putProperty(String key, Object value);

    /**
     * Replace existing properties with provided properties.
     *
     * @param properties the properties
     */
    void setProperties(Map<String, Object> properties);

    /**
     * Remove the property associated to the passed key and return its value.
     * 
     * @param <T> type of the property value
     * @param key the property key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for
     *         <tt>key</tt>;
     */
    <T> T removeProperty(String key);
}
