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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.stability.Unstable;

/**
 * Base class for {@link ExtensionDependency} implementations.
 *
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractExtensionDependency implements ExtensionDependency
{
    /**
     * @see #getId()
     */
    protected String id;

    /**
     * @see #getVersionConstraint()
     */
    protected VersionConstraint versionConstraint;

    /**
     * @see #getExclusions()
     * @since 12.2
     */
    protected List<ExtensionPattern> exclusions;

    /**
     * @see #getRepositories()
     */
    protected List<ExtensionRepositoryDescriptor> repositories;

    /**
     * @see #isOptional()
     */
    protected boolean optional;

    /**
     * @see #getProperties()
     */
    protected Map<String, Object> properties = new HashMap<>();

    /**
     * Create new instance by cloning the provided one.
     *
     * @param dependency the extension dependency to copy
     * @since 7.3M1
     */
    public AbstractExtensionDependency(ExtensionDependency dependency)
    {
        this(dependency, null);
    }

    /**
     * Create new instance by cloning the provided one with different version constraint.
     *
     * @param dependency the extension dependency to copy
     * @param versionConstraint the version constraint to set
     */
    public AbstractExtensionDependency(ExtensionDependency dependency, VersionConstraint versionConstraint)
    {
        this(dependency.getId(), versionConstraint != null ? versionConstraint : dependency.getVersionConstraint(),
            dependency.isOptional(), dependency.getProperties());

        setExclusions(dependency.getExclusions());
        setRepositories(dependency.getRepositories());
    }

    /**
     * @param id the id (or feature) of the extension dependency
     * @param versionConstraint the version constraint of the extension dependency
     */
    public AbstractExtensionDependency(String id, VersionConstraint versionConstraint)
    {
        this(id, versionConstraint, false);
    }

    /**
     * @param id the id (or feature) of the extension dependency
     * @param versionConstraint the version constraint of the extension dependency
     * @param optional true if the dependency is optional
     * @since 9.6RC1
     */
    public AbstractExtensionDependency(String id, VersionConstraint versionConstraint, boolean optional)
    {
        this(id, versionConstraint, optional, null);
    }

    /**
     * @param id the id (or feature) of the extension dependency
     * @param versionConstraint the version constraint of the extension dependency
     * @param properties the custom properties of the extension dependency
     */
    public AbstractExtensionDependency(String id, VersionConstraint versionConstraint, Map<String, Object> properties)
    {
        this.id = id;
        this.versionConstraint = versionConstraint;
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    /**
     * @param id the id (or feature) of the extension dependency
     * @param versionConstraint the version constraint of the extension dependency
     * @param optional true if the dependency is optional
     * @param properties the custom properties of the extension dependency
     */
    public AbstractExtensionDependency(String id, VersionConstraint versionConstraint, boolean optional,
        Map<String, Object> properties)
    {
        this.id = id;
        this.versionConstraint = versionConstraint;
        this.optional = optional;
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    @Override
    public String getId()
    {
        return this.id;
    }

    /**
     * @param id the extension id
     * @see #getId()
     */
    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public VersionConstraint getVersionConstraint()
    {
        return this.versionConstraint;
    }

    /**
     * @param versionConstraint the version constraint of the target extension
     */
    public void setVersionConstraint(VersionConstraint versionConstraint)
    {
        this.versionConstraint = versionConstraint;
    }

    @Override
    public Collection<ExtensionPattern> getExclusions()
    {
        return this.exclusions != null ? this.exclusions : Collections.emptyList();
    }

    /**
     * @param exclusions the exclusions patterns to apply to transitive dependencies
     * @since 12.2
     */
    @Unstable
    public void setExclusions(Collection<? extends ExtensionPattern> exclusions)
    {
        this.exclusions = exclusions != null ? Collections.unmodifiableList(new ArrayList<>(exclusions)) : null;
    }

    /**
     * @param exclusion an exclusion pattern to apply to transitive dependencies
     * @since 12.2
     */
    @Unstable
    public void addExclusion(ExtensionPattern exclusion)
    {
        List<ExtensionPattern> newexclusions = new ArrayList<>(getExclusions());
        newexclusions.add(exclusion);

        this.exclusions = Collections.unmodifiableList(newexclusions);
    }

    @Override
    public Collection<ExtensionRepositoryDescriptor> getRepositories()
    {
        return this.repositories != null ? this.repositories : Collections.emptyList();
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
        List<ExtensionRepositoryDescriptor> newrepositories = new ArrayList<>(getRepositories());
        newrepositories.add(repository);

        this.repositories = Collections.unmodifiableList(newrepositories);
    }

    @Override
    public boolean isOptional()
    {
        return this.optional;
    }

    @Override
    public boolean isCompatible(Extension extension)
    {
        if (isCompatible(extension.getId())) {
            return true;
        }

        for (ExtensionId extensionId : extension.getExtensionFeatures()) {
            if (isCompatible(extensionId)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isCompatible(ExtensionId extensionId)
    {
        return getId().equals(extensionId.getId()) && getVersionConstraint().isCompatible(extensionId.getVersion());
    }

    @Override
    public Map<String, Object> getProperties()
    {
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    public Object getProperty(String key)
    {
        return this.properties.get(key);
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
        this.properties.put(key, value);
    }

    /**
     * Replace existing properties with provided properties.
     *
     * @param properties the properties
     */
    public void setProperties(Map<String, Object> properties)
    {
        this.properties.clear();
        this.properties.putAll(properties);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key, T def)
    {
        return this.properties.containsKey(key) ? (T) this.properties.get(key) : def;
    }

    // Object

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();

        str.append(getId());

        if (getVersionConstraint() != null) {
            str.append('-');
            str.append(getVersionConstraint());
        }

        return str.toString();
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getId());
        builder.append(getVersionConstraint());
        builder.append(isOptional());
        builder.append(getExclusions());
        builder.append(getRepositories());
        builder.append(getProperties());

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        boolean equals;

        if (obj instanceof ExtensionDependency) {
            ExtensionDependency otherDependency = (ExtensionDependency) obj;

            EqualsBuilder builder = new EqualsBuilder();

            builder.append(getId(), otherDependency.getId());
            builder.append(getVersionConstraint(), otherDependency.getVersionConstraint());
            builder.append(isOptional(), otherDependency.isOptional());
            builder.append(getExclusions(), otherDependency.getExclusions());
            builder.append(getRepositories(), otherDependency.getRepositories());
            builder.append(getProperties(), otherDependency.getProperties());

            equals = builder.isEquals();
        } else {
            equals = false;
        }

        return equals;
    }
}
