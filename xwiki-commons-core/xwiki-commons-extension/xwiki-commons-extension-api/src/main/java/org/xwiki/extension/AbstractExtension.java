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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.ObjectUtils;
import org.xwiki.extension.internal.converter.ExtensionIdConverter;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;

/**
 * Base class for {@link Extension} implementations.
 *
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractExtension implements MutableExtension
{
    /**
     * @see #getId()
     */
    protected ExtensionId id;

    /**
     * @see #getExtensionFeatures()
     * @deprecated since 8.0M1, use {@link #featuresMap} instead
     */
    @Deprecated
    protected Set<String> features;

    /**
     * @see #getExtensionFeatures()
     */
    protected Map<String, ExtensionId> featuresMap;

    /**
     * @see #getType()
     */
    protected String type;

    /**
     * @see #getName()
     */
    protected String name;

    /**
     * @see #getLicenses()
     */
    protected List<ExtensionLicense> licenses;

    /**
     * @see #getSummary()
     */
    protected String summary;

    /**
     * @see #getDescription()
     */
    protected String description;

    /**
     * @see #getAuthors()
     */
    protected List<ExtensionAuthor> authors;

    /**
     * @see #getComponents()
     */
    protected List<ExtensionComponent> components;

    /**
     * @see #getWebSite()
     */
    protected String website;

    /**
     * @see #getAllowedNamespaces()
     */
    protected Set<String> allowedNamespaces;

    /**
     * @see #getRepository()
     */
    protected ExtensionRepository repository;

    /**
     * @see #getProperties()
     */
    protected Map<String, Object> properties;

    /**
     * Used to protect properties from concurrent write;
     */
    protected ReentrantLock propertiesLock = new ReentrantLock();

    /**
     * @see #getDependencies()
     */
    protected List<ExtensionDependency> dependencies;

    /**
     * @see #getManagedDependencies()
     * @since 8.1M1
     */
    protected List<ExtensionDependency> managedDependencies;

    /**
     * @see #getScm()
     */
    protected ExtensionScm scm;

    /**
     * @see #getIssueManagement()
     */
    protected ExtensionIssueManagement issueManagement;

    /**
     * @see #getCategory()
     */
    protected String category;

    /**
     * @see #getRepositories()
     */
    protected List<ExtensionRepositoryDescriptor> repositories;

    /**
     * The file of the extension.
     */
    protected ExtensionFile file;

    /**
     * @param repository the repository where this extension comes from
     * @param id the extension identifier
     * @param type the extension type
     */
    public AbstractExtension(ExtensionRepository repository, ExtensionId id, String type)
    {
        this.repository = repository;

        this.id = id;
        this.type = type;
    }

    /**
     * Create new extension descriptor by copying provided one.
     *
     * @param repository the repository where this extension comes from
     * @param extension the extension to copy
     */
    public AbstractExtension(ExtensionRepository repository, Extension extension)
    {
        this(repository, extension.getId(), extension.getType());

        set(extension);
    }

    @Override
    public void set(Extension extension)
    {
        setName(extension.getName());
        setDescription(extension.getDescription());
        setAuthors(extension.getAuthors());
        setWebsite(extension.getWebSite());
        setLicenses(extension.getLicenses());
        setSummary(extension.getSummary());
        setIssueManagement(extension.getIssueManagement());
        setScm(extension.getScm());
        setCategory(extension.getCategory());
        setExtensionFeatures(extension.getExtensionFeatures());
        setAllowedNamespaces(extension.getAllowedNamespaces());
        setComponents(extension.getComponents());

        setDependencies(extension.getDependencies());
        setManagedDependencies(extension.getManagedDependencies());

        setRepositories(extension.getRepositories());

        setProperties(extension.getProperties());
    }

    /**
     * Get an extension field by name. Fallback on properties.
     * 
     * @param fieldName the field name;
     * @return the field value or null if none could be found
     */
    @Override
    public <T> T get(String fieldName)
    {
        switch (fieldName.toLowerCase()) {
            case FIELD_REPOSITORY:
                return (T) getRepository();
            case FIELD_ID:
                return (T) getId().getId();
            case FIELD_VERSION:
                return (T) getId().getVersion();
            case FIELD_FEATURE:
            case FIELD_FEATURES:
                return (T) ExtensionIdConverter.toStringList(getExtensionFeatures());
            case FIELD_EXTENSIONFEATURE:
            case FIELD_EXTENSIONFEATURES:
                return (T) getExtensionFeatures();
            case FIELD_SUMMARY:
                return (T) getSummary();
            case FIELD_DESCRIPTION:
                return (T) getDescription();
            case FIELD_AUTHOR:
            case FIELD_AUTHORS:
                return (T) getAuthors();
            case FIELD_COMPONENT:
            case FIELD_COMPONENTS:
                return (T) getComponents();
            case FIELD_CATEGORY:
                return (T) getCategory();
            case FIELD_LICENSE:
            case FIELD_LICENSES:
                return (T) getLicenses();
            case FIELD_NAME:
                return (T) getName();
            case FIELD_TYPE:
                return (T) getType();
            case FIELD_WEBSITE:
                return (T) getWebSite();
            case FIELD_NAMESPACES:
            case FIELD_ALLOWEDNAMESPACE:
            case FIELD_ALLOWEDNAMESPACES:
                return (T) getAllowedNamespaces();
            case FIELD_SCM:
                return (T) getScm();
            case FIELD_REPOSITORIES:
                return (T) getRepositories();
            case FIELD_PROPERTIES:
                return (T) getProperties();

            default:
                // Unknown field, probably a property
                return getProperty(fieldName);
        }
    }

    @Override
    public ExtensionId getId()
    {
        return this.id;
    }

    /**
     * @param id the extension id
     * @see #getId()
     */
    protected void setId(ExtensionId id)
    {
        this.id = id;
    }

    @Override
    @Deprecated
    public Collection<String> getFeatures()
    {
        return this.features != null ? this.features : Collections.emptyList();
    }

    @Override
    @Deprecated
    public void setFeatures(Collection<String> features)
    {
        List<ExtensionId> extensionFeatures = new ArrayList<>(features.size());
        for (String feature : features) {
            extensionFeatures.add(new ExtensionId(feature, getId().getVersion()));
        }

        setExtensionFeatures(extensionFeatures);
    }

    @Override
    @Deprecated
    public void addFeature(String feature)
    {
        addExtensionFeature(new ExtensionId(feature, getId().getVersion()));
    }

    @Override
    public Collection<ExtensionId> getExtensionFeatures()
    {
        return this.featuresMap != null ? this.featuresMap.values() : Collections.emptyList();
    }

    @Override
    public ExtensionId getExtensionFeature(String featureId)
    {
        ExtensionId feature = null;

        // Search in the extension features
        if (this.featuresMap != null) {
            feature = this.featuresMap.get(featureId);
        }

        // Fallback on extension id
        if (feature == null && featureId.equals(getId().getId())) {
            feature = getId();
        }

        return feature;
    }

    /**
     * @since 8.0M1
     */
    @Override
    public void setExtensionFeatures(Collection<ExtensionId> features)
    {
        Map<String, ExtensionId> map = new LinkedHashMap<>();
        for (ExtensionId feature : features) {
            map.put(feature.getId(), feature);
        }

        setFeatureMap(map);
    }

    /**
     * @since 8.0M1
     */
    @Override
    public void addExtensionFeature(ExtensionId feature)
    {
        Map<String, ExtensionId> map =
            this.featuresMap != null ? new LinkedHashMap<>(this.featuresMap) : new LinkedHashMap<>();
        map.put(feature.getId(), feature);

        setFeatureMap(map);
    }

    private void setFeatureMap(Map<String, ExtensionId> map)
    {
        this.featuresMap = Collections.unmodifiableMap(map);

        // Retro compatibility
        Set<String> list = new LinkedHashSet<>(this.featuresMap.size());
        for (ExtensionId extensionId : this.featuresMap.values()) {
            list.add(extensionId.getId());
        }
        this.features = Collections.unmodifiableSet(list);
    }

    @Override
    public String getType()
    {
        return this.type;
    }

    /**
     * @param type the type of the extension
     * @see #getType()
     */
    protected void setType(String type)
    {
        this.type = type;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public Collection<ExtensionLicense> getLicenses()
    {
        return this.licenses != null ? this.licenses : Collections.emptyList();
    }

    @Override
    public void setLicenses(Collection<ExtensionLicense> licenses)
    {
        this.licenses = Collections.unmodifiableList(new ArrayList<>(licenses));
    }

    @Override
    public void addLicense(ExtensionLicense license)
    {
        List<ExtensionLicense> newLicenses = new ArrayList<>(getLicenses());
        newLicenses.add(license);

        this.licenses = Collections.unmodifiableList(newLicenses);
    }

    @Override
    public String getSummary()
    {
        return this.summary;
    }

    @Override
    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public List<ExtensionAuthor> getAuthors()
    {
        return this.authors != null ? this.authors : Collections.emptyList();
    }

    @Override
    public void setAuthors(Collection<? extends ExtensionAuthor> authors)
    {
        this.authors = Collections.unmodifiableList(new ArrayList<ExtensionAuthor>(authors));
    }

    @Override
    public void addAuthor(ExtensionAuthor author)
    {
        List<ExtensionAuthor> newAuthors = new ArrayList<>(getAuthors());
        newAuthors.add(author);

        this.authors = Collections.unmodifiableList(newAuthors);
    }

    @Override
    public List<ExtensionComponent> getComponents()
    {
        return this.components != null ? this.components : Collections.emptyList();
    }

    @Override
    public void setComponents(Collection<? extends ExtensionComponent> components)
    {
        this.components = Collections.unmodifiableList(new ArrayList<ExtensionComponent>(components));
    }

    @Override
    public void addComponent(ExtensionComponent component)
    {
        List<ExtensionComponent> newComponents = new ArrayList<>(getComponents());
        newComponents.add(component);

        this.components = Collections.unmodifiableList(newComponents);
    }

    @Override
    public String getWebSite()
    {
        return this.website;
    }

    @Override
    public void setWebsite(String website)
    {
        this.website = website;
    }

    @Override
    public Collection<String> getAllowedNamespaces()
    {
        return this.allowedNamespaces;
    }

    /**
     * @since 8.0M1
     */
    @Override
    public void addAllowedNamespace(String namespace)
    {
        Set<String> newNamespaces =
            this.allowedNamespaces != null ? new LinkedHashSet<>(this.allowedNamespaces) : new LinkedHashSet<>();
        newNamespaces.add(namespace);

        this.allowedNamespaces = Collections.unmodifiableSet(newNamespaces);
    }

    /**
     * @since 8.0M1
     */
    @Override
    public void setAllowedNamespaces(Collection<String> namespaces)
    {
        this.allowedNamespaces =
            namespaces != null ? Collections.unmodifiableSet(new LinkedHashSet<>(namespaces)) : null;
    }

    @Override
    public void addDependency(ExtensionDependency dependency)
    {
        List<ExtensionDependency> newDependencies = new ArrayList<>(getDependencies());
        newDependencies.add(dependency);

        this.dependencies = Collections.unmodifiableList(newDependencies);
    }

    @Override
    public List<ExtensionDependency> getDependencies()
    {
        return this.dependencies != null ? this.dependencies : Collections.emptyList();
    }

    @Override
    public void setDependencies(Collection<? extends ExtensionDependency> dependencies)
    {
        this.dependencies = dependencies != null
            ? Collections.unmodifiableList(new ArrayList<ExtensionDependency>(dependencies)) : null;
    }

    /**
     * @since 8.1M1
     */
    @Override
    public void addManagedDependency(ExtensionDependency managedDependency)
    {
        List<ExtensionDependency> newManagedDependencies = new ArrayList<>(getManagedDependencies());
        newManagedDependencies.add(managedDependency);

        this.managedDependencies = Collections.unmodifiableList(newManagedDependencies);
    }

    @Override
    public List<ExtensionDependency> getManagedDependencies()
    {
        return this.managedDependencies != null ? this.managedDependencies : Collections.emptyList();
    }

    @Override
    public void setManagedDependencies(Collection<? extends ExtensionDependency> managedDependencies)
    {
        this.managedDependencies = managedDependencies != null
            ? Collections.unmodifiableList(new ArrayList<ExtensionDependency>(managedDependencies)) : null;
    }

    @Override
    public ExtensionRepository getRepository()
    {
        return this.repository;
    }

    /**
     * @param repository the repository of the extension
     * @see #getRepository()
     */
    protected void setRepository(ExtensionRepository repository)
    {
        this.repository = repository;
    }

    @Override
    public ExtensionScm getScm()
    {
        return this.scm;
    }

    /**
     * @since 6.3M1
     */
    @Override
    public void setScm(ExtensionScm scm)
    {
        this.scm = scm;
    }

    @Override
    public ExtensionIssueManagement getIssueManagement()
    {
        return this.issueManagement;
    }

    /**
     * @since 6.3M1
     */
    @Override
    public void setIssueManagement(ExtensionIssueManagement issueManagement)
    {
        this.issueManagement = issueManagement;
    }

    @Override
    public ExtensionFile getFile()
    {
        return this.file;
    }

    /**
     * @param file the file of the extension
     */
    protected void setFile(ExtensionFile file)
    {
        this.file = file;
    }

    @Override
    public String getCategory()
    {
        return this.category;
    }

    /**
     * @since 7.0M2
     */
    @Override
    public void setCategory(String categrory)
    {
        this.category = categrory;
    }

    @Override
    public Collection<ExtensionRepositoryDescriptor> getRepositories()
    {
        return this.repositories != null ? this.repositories : Collections.emptyList();
    }

    /**
     * @since 7.3M1
     */
    @Override
    public void setRepositories(Collection<? extends ExtensionRepositoryDescriptor> repositories)
    {
        this.repositories = repositories != null ? Collections.unmodifiableList(new ArrayList<>(repositories)) : null;
    }

    /**
     * @since 7.3M1
     */
    @Override
    public void addRepository(ExtensionRepositoryDescriptor repository)
    {
        List<ExtensionRepositoryDescriptor> newrepositories = new ArrayList<>(getRepositories());
        newrepositories.add(repository);

        this.repositories = Collections.unmodifiableList(newrepositories);
    }

    @Override
    public Map<String, Object> getProperties()
    {
        return this.properties != null ? this.properties : Collections.emptyMap();
    }

    @Override
    public <T> T getProperty(String key)
    {
        return (T) getProperties().get(key);
    }

    @Override
    public <T> T getProperty(String key, T def)
    {
        return getProperties().containsKey(key) ? (T) getProperties().get(key) : def;
    }

    @Override
    public void putProperty(String key, Object value)
    {
        try {
            this.propertiesLock.lock();

            Map<String, Object> newProperties = new LinkedHashMap<>(getProperties());
            newProperties.put(key, value);

            this.properties = Collections.unmodifiableMap(newProperties);
        } finally {
            this.propertiesLock.unlock();
        }
    }

    @Override
    public void setProperties(Map<String, Object> properties)
    {
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<>(properties));
    }

    /**
     * @since 8.3M1
     */
    @Override
    public <T> T removeProperty(String key)
    {
        T previous;

        try {
            this.propertiesLock.lock();

            Map<String, Object> newProperties = new LinkedHashMap<>(getProperties());
            previous = (T) newProperties.remove(key);

            this.properties = Collections.unmodifiableMap(newProperties);
        } finally {
            this.propertiesLock.unlock();
        }

        return previous;
    }

    // Object

    @Override
    public String toString()
    {
        return getId().toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || (obj instanceof Extension && getId().equals(((Extension) obj).getId()));
    }

    @Override
    public int hashCode()
    {
        return getId().hashCode();
    }

    @Override
    public int compareTo(Extension o)
    {
        if (o == null) {
            return -1;
        }

        // Try to find this into provided extension
        Integer comparizon = compareTo(this, o);
        if (comparizon != null) {
            return comparizon;
        }

        // Try to find provided extension into this
        comparizon = compareTo(o, this);
        if (comparizon != null) {
            return comparizon;
        }

        return ObjectUtils.compare(getId(), o.getId());
    }

    private static Integer compareTo(Extension e1, Extension e2)
    {
        // Try to find e1 id in e2
        ExtensionId feature = e2.getExtensionFeature(e1.getId().getId());
        if (feature != null) {
            return ObjectUtils.compare(e1.getId().getVersion(), feature.getVersion());
        }

        // Try to find e1 features in e2
        for (ExtensionId feature1 : e1.getExtensionFeatures()) {
            feature = e2.getExtensionFeature(feature1.getId());
            if (feature != null) {
                return ObjectUtils.compare(feature1.getVersion(), feature.getVersion());
            }
        }

        return null;
    }
}
