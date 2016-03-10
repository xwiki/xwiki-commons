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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.stability.Unstable;

/**
 * Base class for {@link Extension} implementations.
 *
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractExtension implements Extension
{
    /**
     * @see #getId()
     */
    protected ExtensionId id;

    /**
     * @see #getFeatures()
     */
    protected Set<String> features;

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
     * @see #getWebSite()
     */
    protected String website;

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

    /**
     * Update optional informations based on the provided extension.
     *
     * @param extension the extension from which to get informations
     */
    protected void set(Extension extension)
    {
        setFeatures(extension.getFeatures());

        setName(extension.getName());
        setDescription(extension.getDescription());
        setAuthors(extension.getAuthors());
        setWebsite(extension.getWebSite());
        setLicenses(extension.getLicenses());
        setSummary(extension.getSummary());
        setIssueManagement(extension.getIssueManagement());
        setScm(extension.getScm());
        setCategory(extension.getCategory());

        setDependencies(extension.getDependencies());

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
                return (T) getFeatures();
            case FIELD_SUMMARY:
                return (T) getSummary();
            case FIELD_DESCRIPTION:
                return (T) getDescription();
            case FIELD_AUTHOR:
            case FIELD_AUTHORS:
                return (T) getAuthors();
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
            case FIELD_SCM:
                return (T) getScm();
            case FIELD_REPOSITORIES:
                return (T) getRepositories();

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
    public Collection<String> getFeatures()
    {
        return this.features != null ? this.features : Collections.<String>emptyList();
    }

    /**
     * @param features the extension ids also provided by this extension
     */
    public void setFeatures(Collection<String> features)
    {
        this.features = Collections.unmodifiableSet(new HashSet<String>(features));
    }

    /**
     * Add a new feature to the extension.
     *
     * @param feature a feature name
     */
    public void addFeature(String feature)
    {
        Set<String> newFeatures = new HashSet<String>(getFeatures());
        newFeatures.add(feature);

        this.features = Collections.unmodifiableSet(newFeatures);
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

    /**
     * @param name the display name of the extension
     */
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public Collection<ExtensionLicense> getLicenses()
    {
        return this.licenses != null ? this.licenses : Collections.<ExtensionLicense>emptyList();
    }

    /**
     * @param licenses the licenses of the extension
     */
    public void setLicenses(Collection<ExtensionLicense> licenses)
    {
        this.licenses = Collections.unmodifiableList(new ArrayList<ExtensionLicense>(licenses));
    }

    /**
     * Add a new license to the extension.
     *
     * @param license a license
     */
    public void addLicense(ExtensionLicense license)
    {
        List<ExtensionLicense> newLicenses = new ArrayList<ExtensionLicense>(getLicenses());
        newLicenses.add(license);

        this.licenses = Collections.unmodifiableList(newLicenses);
    }

    @Override
    public String getSummary()
    {
        return this.summary;
    }

    /**
     * @param summary a short description of the extension
     */
    public void setSummary(String summary)
    {
        this.summary = summary;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @param description a description of the extension
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public List<ExtensionAuthor> getAuthors()
    {
        return this.authors != null ? this.authors : Collections.<ExtensionAuthor>emptyList();
    }

    /**
     * @param authors the authors of the extension
     */
    public void setAuthors(Collection<? extends ExtensionAuthor> authors)
    {
        this.authors = Collections.unmodifiableList(new ArrayList<ExtensionAuthor>(authors));
    }

    /**
     * Add a new author to the extension.
     *
     * @param author an author
     */
    public void addAuthor(ExtensionAuthor author)
    {
        List<ExtensionAuthor> newAuthors = new ArrayList<ExtensionAuthor>(getAuthors());
        newAuthors.add(author);

        this.authors = Collections.unmodifiableList(newAuthors);
    }

    @Override
    public String getWebSite()
    {
        return this.website;
    }

    /**
     * @param website an URL for the extension website
     */
    public void setWebsite(String website)
    {
        this.website = website;
    }

    /**
     * Add a new dependency to the extension.
     *
     * @param dependency a dependency
     */
    public void addDependency(ExtensionDependency dependency)
    {
        List<ExtensionDependency> newDependencies = new ArrayList<ExtensionDependency>(getDependencies());
        newDependencies.add(dependency);

        this.dependencies = Collections.unmodifiableList(newDependencies);
    }

    @Override
    public List<? extends ExtensionDependency> getDependencies()
    {
        return this.dependencies != null ? this.dependencies : Collections.<ExtensionDependency>emptyList();
    }

    /**
     * @param dependencies the dependencies of the extension
     * @see #getDependencies()
     */
    public void setDependencies(Collection<? extends ExtensionDependency> dependencies)
    {
        this.dependencies =
            dependencies != null ? Collections.unmodifiableList(new ArrayList<ExtensionDependency>(dependencies))
                : null;
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
     * @param scm informations related to extensions's Source Control Management
     * @since 6.3M1
     */
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
     * @param issueManagement informations related to extension's issues management
     * @since 6.3M1
     */
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
     * @param categrory the category of the extension
     * @since 7.0M2
     */
    @Unstable
    public void setCategory(String categrory)
    {
        this.category = categrory;
    }

    @Override
    public Collection<ExtensionRepositoryDescriptor> getRepositories()
    {
        return this.repositories != null ? this.repositories : Collections.<ExtensionRepositoryDescriptor>emptyList();
    }

    /**
     * @param repositories the custom repositories provided by the extension (usually to resolve dependencies)
     * @since 7.3M1
     */
    public void setRepositories(Collection<? extends ExtensionRepositoryDescriptor> repositories)
    {
        this.repositories = repositories != null ? Collections.unmodifiableList(new ArrayList<>(repositories)) : null;
    }

    /**
     * Add a new repository to the extension.
     *
     * @param repository a repository descriptor
     * @since 7.3M1
     */
    public void addRepository(ExtensionRepositoryDescriptor repository)
    {
        List<ExtensionRepositoryDescriptor> newrepositories =
            new ArrayList<ExtensionRepositoryDescriptor>(getRepositories());
        newrepositories.add(repository);

        this.repositories = Collections.unmodifiableList(newrepositories);
    }

    @Override
    public Map<String, Object> getProperties()
    {
        return this.properties != null ? this.properties : Collections.<String, Object>emptyMap();
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

    /**
     * Set a property.
     *
     * @param key the property key
     * @param value the property value
     * @see #getProperty(String)
     */
    public void putProperty(String key, Object value)
    {
        synchronized (this.propertiesLock) {
            Map<String, Object> newProperties = new LinkedHashMap<String, Object>(getProperties());
            newProperties.put(key, value);

            this.properties = Collections.unmodifiableMap(newProperties);
        }
    }

    /**
     * Replace existing properties with provided properties.
     *
     * @param properties the properties
     */
    public void setProperties(Map<String, Object> properties)
    {
        this.properties = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(properties));
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
}
