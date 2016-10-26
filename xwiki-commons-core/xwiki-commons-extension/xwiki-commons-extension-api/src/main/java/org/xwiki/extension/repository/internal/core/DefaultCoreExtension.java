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
package org.xwiki.extension.repository.internal.core;

import java.net.URL;
import java.util.Collection;

import org.xwiki.extension.AbstractExtension;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;

/**
 * Default implementation of {@link CoreExtension}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultCoreExtension extends AbstractExtension implements CoreExtension
{
    /**
     * @see #getDescriptorURL()
     */
    private static final String PKEY_DESCRIPTORURL = "core.descriptorurl";

    /**
     * @see #isComplete()
     */
    private boolean complete;

    /**
     * @param repository the core extension repository
     * @param url the core extension URL
     * @param id the id/version combination which makes the extension unique
     * @param type the type of the extension
     */
    public DefaultCoreExtension(DefaultCoreExtensionRepository repository, URL url, ExtensionId id, String type)
    {
        super(repository, id, type);

        setURL(url);
    }

    // Extension

    /**
     * Copy the passed {@link Extension} but filter useless stuff from {@link CoreExtension} point of view like managed
     * dependencies.
     * 
     * @param repository the core extension repository
     * @param url the core extension URL
     * @param extension the extension to copy
     */
    public DefaultCoreExtension(DefaultCoreExtensionRepository repository, URL url, Extension extension)
    {
        super(repository, extension);

        setURL(url);
    }

    @Override
    public void setAllowedNamespaces(Collection<String> namespaces)
    {
        // Filter useless stuff from {@link CoreExtension} point of view that could take a lot of memory in the end
        // TODO: dynamically load it from the cache when possible
    }

    @Override
    public void setManagedDependencies(Collection<? extends ExtensionDependency> managedDependencies)
    {
        // Filter useless stuff from {@link CoreExtension} point of view that could take a lot of memory in the end
        // TODO: dynamically load it from the cache when possible
    }

    /**
     * @return true if the extension come from a complete descriptor (xed file, the cache, etc.)
     * @since 8.4RC1
     */
    public boolean isComplete()
    {
        return this.complete;
    }

    /**
     * @param complete true if the extension come from a complete descriptor (xed file, the cache, etc.)
     * @since 8.4RC1
     */
    public void setComplete(boolean complete)
    {
        this.complete = complete;
    }

    @Override
    public void setId(ExtensionId id)
    {
        super.setId(id);
    }

    @Override
    public void setType(String type)
    {
        super.setType(type);
    }

    // CoreExtension

    @Override
    public URL getURL()
    {
        return getProperty(PKEY_URL, null);
    }

    /**
     * @param url the {@link URL} pointing to the core extension file
     */
    public void setURL(URL url)
    {
        setFile(new DefaultCoreExtensionFile(url));

        putProperty(PKEY_URL, url);
    }

    /**
     * @return the {@link URL} pointing to the core extension descriptor (usually inside the extension file)
     */
    public URL getDescriptorURL()
    {
        return getProperty(PKEY_DESCRIPTORURL, null);
    }

    /**
     * @param descriptorURL the {@link URL} pointing to the core extension descriptor (usually inside the extension
     *            file)
     */
    public void setDescriptorURL(URL descriptorURL)
    {
        putProperty(PKEY_DESCRIPTORURL, descriptorURL);
    }

    @Override
    public boolean isGuessed()
    {
        return getProperty(PKEY_GUESSED, false);
    }

    /**
     * @param guessed true if the extension is "guessed"
     */
    public void setGuessed(boolean guessed)
    {
        putProperty(PKEY_GUESSED, guessed);
    }

    // Object

    @Override
    public String toString()
    {
        return getId().toString();
    }

    @Override
    public void set(Extension extension)
    {
        URL url = getURL();
        URL descriptorURL = getDescriptorURL();

        super.set(extension);

        setURL(url);
        setDescriptorURL(descriptorURL);
    }
}
