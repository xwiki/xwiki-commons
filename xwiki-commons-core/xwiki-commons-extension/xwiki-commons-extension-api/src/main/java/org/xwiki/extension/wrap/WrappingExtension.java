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
package org.xwiki.extension.wrap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionAuthor;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionFile;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionIssueManagement;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.ExtensionScm;
import org.xwiki.extension.internal.converter.ExtensionIdConverter;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;

/**
 * Wrap an extension.
 *
 * @param <E> the extension type
 * @version $Id$
 * @since 4.0M1
 */
public class WrappingExtension<E extends Extension> extends AbstractWrappingObject<E> implements Extension
{
    protected Map<String, Object> overwrites = new HashMap<>();

    /**
     * @param extension the wrapped extension
     */
    public WrappingExtension(E extension)
    {
        super(extension);
    }

    /**
     * @param key the key associated to the Extension field (usually found in {@link Extension} constants) to overwrite
     * @param value the value to overwrite
     * @since 9.0RC1
     * @since 8.4.2
     */
    public void setOverwrite(String key, Object value)
    {
        this.overwrites.put(key, value);
    }

    // Extension

    @Override
    public <T> T get(String fieldName)
    {
        if (this.overwrites.containsKey(fieldName)) {
            return (T) this.overwrites.get(fieldName);
        }

        return getWrapped().get(fieldName);
    }

    @Override
    public ExtensionId getId()
    {
        return getWrapped().getId();
    }

    @Override
    @Deprecated
    public Collection<String> getFeatures()
    {
        return ExtensionIdConverter.toStringList(getExtensionFeatures());
    }

    @Override
    public Collection<ExtensionId> getExtensionFeatures()
    {
        if (this.overwrites.containsKey(Extension.FIELD_EXTENSIONFEATURES)) {
            return ObjectUtils.defaultIfNull(
                (Collection<ExtensionId>) this.overwrites.get(Extension.FIELD_EXTENSIONFEATURES),
                Collections.emptyList());
        }

        return getWrapped().getExtensionFeatures();
    }

    @Override
    public ExtensionId getExtensionFeature(String featureId)
    {
        return getWrapped().getExtensionFeature(featureId);
    }

    @Override
    public String getType()
    {
        if (this.overwrites.containsKey(Extension.FIELD_TYPE)) {
            return (String) this.overwrites.get(Extension.FIELD_TYPE);
        }

        return getWrapped().getType();
    }

    @Override
    public String getName()
    {
        if (this.overwrites.containsKey(Extension.FIELD_NAME)) {
            return (String) this.overwrites.get(Extension.FIELD_NAME);
        }

        return getWrapped().getName();
    }

    @Override
    public Collection<ExtensionLicense> getLicenses()
    {
        if (this.overwrites.containsKey(Extension.FIELD_LICENSES)) {
            return (Collection<ExtensionLicense>) this.overwrites.get(Extension.FIELD_LICENSES);
        }

        return getWrapped().getLicenses();
    }

    @Override
    public String getSummary()
    {
        if (this.overwrites.containsKey(Extension.FIELD_SUMMARY)) {
            return (String) this.overwrites.get(Extension.FIELD_SUMMARY);
        }

        return getWrapped().getSummary();
    }

    @Override
    public String getDescription()
    {
        if (this.overwrites.containsKey(Extension.FIELD_DESCRIPTION)) {
            return (String) this.overwrites.get(Extension.FIELD_DESCRIPTION);
        }

        return getWrapped().getDescription();
    }

    @Override
    public String getWebSite()
    {
        if (this.overwrites.containsKey(Extension.FIELD_WEBSITE)) {
            return (String) this.overwrites.get(Extension.FIELD_WEBSITE);
        }

        return getWrapped().getWebSite();
    }

    @Override
    public Collection<ExtensionAuthor> getAuthors()
    {
        if (this.overwrites.containsKey(Extension.FIELD_AUTHORS)) {
            return (Collection<ExtensionAuthor>) this.overwrites.get(Extension.FIELD_AUTHORS);
        }

        return getWrapped().getAuthors();
    }

    @Override
    public Collection<String> getAllowedNamespaces()
    {
        if (this.overwrites.containsKey(Extension.FIELD_ALLOWEDNAMESPACES)) {
            return (Collection<String>) this.overwrites.get(Extension.FIELD_ALLOWEDNAMESPACES);
        }

        return getWrapped().getAllowedNamespaces();
    }

    @Override
    public Collection<ExtensionDependency> getDependencies()
    {
        if (this.overwrites.containsKey(Extension.FIELD_DEPENDENCIES)) {
            return (Collection<ExtensionDependency>) this.overwrites.get(Extension.FIELD_DEPENDENCIES);
        }

        return getWrapped().getDependencies();
    }

    @Override
    public Collection<ExtensionDependency> getManagedDependencies()
    {
        if (this.overwrites.containsKey(Extension.FIELD_MANAGEDDEPENDENCIES)) {
            return (Collection<ExtensionDependency>) this.overwrites.get(Extension.FIELD_MANAGEDDEPENDENCIES);
        }

        return getWrapped().getManagedDependencies();
    }

    @Override
    public ExtensionFile getFile()
    {
        return getWrapped().getFile();
    }

    @Override
    public ExtensionRepository getRepository()
    {
        return getWrapped().getRepository();
    }

    @Override
    public ExtensionScm getScm()
    {
        if (this.overwrites.containsKey(Extension.FIELD_SCM)) {
            return (ExtensionScm) this.overwrites.get(Extension.FIELD_SCM);
        }

        return getWrapped().getScm();
    }

    @Override
    public ExtensionIssueManagement getIssueManagement()
    {
        if (this.overwrites.containsKey(Extension.FIELD_ISSUEMANAGEMENT)) {
            return (ExtensionIssueManagement) this.overwrites.get(Extension.FIELD_ISSUEMANAGEMENT);
        }

        return getWrapped().getIssueManagement();
    }

    @Override
    public String getCategory()
    {
        if (this.overwrites.containsKey(Extension.FIELD_CATEGORY)) {
            return (String) this.overwrites.get(Extension.FIELD_CATEGORY);
        }

        return getWrapped().getCategory();
    }

    @Override
    public Collection<ExtensionRepositoryDescriptor> getRepositories()
    {
        if (this.overwrites.containsKey(Extension.FIELD_REPOSITORIES)) {
            return (Collection<ExtensionRepositoryDescriptor>) this.overwrites.get(Extension.FIELD_REPOSITORIES);
        }

        return getWrapped().getRepositories();
    }

    @Override
    public Map<String, Object> getProperties()
    {
        if (this.overwrites.containsKey(Extension.FIELD_PROPERTIES)) {
            return (Map<String, Object>) this.overwrites.get(Extension.FIELD_PROPERTIES);
        }

        return getWrapped().getProperties();
    }

    @Override
    public <T> T getProperty(String key)
    {
        if (this.overwrites.containsKey(Extension.FIELD_PROPERTIES + '_' + key)) {
            return (T) this.overwrites.get(Extension.FIELD_PROPERTIES + '_' + key);
        }

        return getWrapped().getProperty(key);
    }

    @Override
    public <T> T getProperty(String key, T def)
    {
        if (this.overwrites.containsKey(Extension.FIELD_PROPERTIES + '_' + key)) {
            return (T) this.overwrites.get(Extension.FIELD_PROPERTIES + '_' + key);
        }

        return getWrapped().getProperty(key, def);
    }

    @Override
    public int compareTo(Extension o)
    {
        return getWrapped().compareTo(o);
    }
}
