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
import java.util.Map;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionAuthor;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionFile;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionIssueManagement;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.ExtensionScm;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.script.wrap.AbstractWrappingObject;

/**
 * Wrap an extension.
 *
 * @param <E> the extension type
 * @version $Id$
 * @since 4.0M1
 */
public class WrappingExtension<E extends Extension> extends AbstractWrappingObject<E> implements Extension
{
    /**
     * @param extension the wrapped extension
     */
    public WrappingExtension(E extension)
    {
        super(extension);
    }

    // Extension
    
    @Override
    public <T> T get(String fieldName)
    {
        return getWrapped().get(fieldName);
    }

    @Override
    public ExtensionId getId()
    {
        return getWrapped().getId();
    }

    @Override
    public Collection<String> getFeatures()
    {
        return getWrapped().getFeatures();
    }

    @Override
    public String getType()
    {
        return getWrapped().getType();
    }

    @Override
    public String getName()
    {
        return getWrapped().getName();
    }

    @Override
    public Collection<ExtensionLicense> getLicenses()
    {
        return getWrapped().getLicenses();
    }

    @Override
    public String getSummary()
    {
        return getWrapped().getSummary();
    }

    @Override
    public String getDescription()
    {
        return getWrapped().getDescription();
    }

    @Override
    public String getWebSite()
    {
        return getWrapped().getWebSite();
    }

    @Override
    public Collection<ExtensionAuthor> getAuthors()
    {
        return getWrapped().getAuthors();
    }

    @Override
    public Collection<? extends ExtensionDependency> getDependencies()
    {
        return getWrapped().getDependencies();
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
        return getWrapped().getScm();
    }

    @Override
    public ExtensionIssueManagement getIssueManagement()
    {
        return getWrapped().getIssueManagement();
    }

    @Override
    public String getCategory()
    {
        return getWrapped().getCategory();
    }

    @Override
    public Map<String, Object> getProperties()
    {
        return getWrapped().getProperties();
    }

    @Override
    public <T> T getProperty(String key)
    {
        return getWrapped().getProperty(key);
    }

    @Override
    public <T> T getProperty(String key, T def)
    {
        return getWrapped().getProperty(key, def);
    }
}
