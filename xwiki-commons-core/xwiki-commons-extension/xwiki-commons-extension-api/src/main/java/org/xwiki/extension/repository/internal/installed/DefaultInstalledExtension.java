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
package org.xwiki.extension.repository.internal.installed;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.xwiki.extension.AbstractExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.LocalExtensionFile;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.InstalledExtensionRepository;

/**
 * Default implementation of {@link LocalExtension}.
 * 
 * @version $Id$
 */
public class DefaultInstalledExtension extends AbstractExtension implements InstalledExtension
{
    /**
     * @see #getLocalExtension()
     */
    private LocalExtension localExtension;

    /**
     * @see #isValid(String)
     */
    private Map<String, Boolean> valid = new HashMap<String, Boolean>();

    /**
     * @param localExtension the wrapped local extension
     * @param repository the repository
     */
    public DefaultInstalledExtension(LocalExtension localExtension, InstalledExtensionRepository repository)
    {
        super(repository, localExtension);

        this.localExtension = localExtension;
    }

    /**
     * @param extension the extension
     * @return true if the extension is installed
     */
    static boolean isInstalled(Extension extension)
    {
        return extension.getProperty(PKEY_INSTALLED, false);
    }

    /**
     * @param extension the extension
     * @param namespace the namespace to look at, if null it means the extension is installed for all the namespaces
     * @return true if the extension is installed in the provided namespace
     */
    public static boolean isInstalled(Extension extension, String namespace)
    {
        return isInstalled(extension)
            && (getNamespaces(extension) == null || getNamespaces(extension).contains(namespace));
    }

    /**
     * @param extension the extension
     * @return the namespaces in which this extension is enabled. Null means root namespace (i.e all namespaces).
     */
    public static Collection<String> getNamespaces(Extension extension)
    {
        return extension.getProperty(PKEY_NAMESPACES, (Collection<String>) null);
    }

    // Extension

    @Override
    public ExtensionRepository getRepository()
    {
        return this.repository;
    }

    // InstalledExtension

    @Override
    public LocalExtension getLocalExtension()
    {
        return this.localExtension;
    }

    /**
     * @param create if true it create and add a new collection of namespaces if there is none
     * @return the namespaces
     */
    private Collection<String> getNamespaces(boolean create)
    {
        Collection<String> namespaces = getProperty(PKEY_NAMESPACES, (Collection<String>) null);

        if (namespaces == null && create) {
            namespaces = new HashSet<String>();
            putProperty(PKEY_NAMESPACES, namespaces);
        }

        return namespaces;
    }

    @Override
    public Collection<String> getNamespaces()
    {
        return getNamespaces(false);
    }

    /**
     * @param namespaces the namespaces in which this extension is enabled. Null means root namespace (i.e all
     *            namespaces).
     * @see #getNamespaces()
     */
    public void setNamespaces(Collection<String> namespaces)
    {
        putProperty(PKEY_NAMESPACES, namespaces != null ? new HashSet<String>(namespaces) : null);
    }

    /**
     * @param namespace the namespace
     * @see #getNamespaces()
     */
    public void addNamespace(String namespace)
    {
        getNamespaces(true).add(namespace);
    }

    @Override
    public boolean isInstalled()
    {
        return isInstalled(this);
    }

    @Override
    public boolean isInstalled(String namespace)
    {
        return isInstalled(this, namespace);
    }

    /**
     * @param installed indicate if the extension is installed
     * @see #isInstalled()
     */
    public void setInstalled(boolean installed)
    {
        putProperty(PKEY_INSTALLED, installed);
    }

    /**
     * @param installed indicate if the extension is installed
     * @param namespace the namespace to look at, if null it means the extension is installed for all the namespaces
     * @see #isInstalled(String)
     */
    public void setInstalled(boolean installed, String namespace)
    {
        if (namespace == null) {
            if (installed && !isInstalled()) {
                setValid(namespace, true);
            }

            setInstalled(installed);
            setNamespaces(null);
        } else {
            if (installed) {
                if (!isInstalled(namespace)) {
                    setValid(namespace, true);
                }

                setInstalled(true);
                addNamespace(namespace);
            } else {
                Collection<String> namespaces = getNamespaces(false);
                if (namespaces != null) {
                    namespaces.remove(namespace);

                    if (getNamespaces().isEmpty()) {
                        setInstalled(false);
                        setNamespaces(null);
                    }
                }
            }
        }

        if (!installed) {
            this.valid.remove(namespace);
        }
    }

    @Override
    public boolean isValid(String namespace)
    {
        Boolean isvalid = this.valid.get(namespace);

        return isvalid != null ? isvalid : true;
    }

    /**
     * @param valid indicate of the installed extension is valid
     * @param namespace the namespace to look at, if null it means the extension is installed for all the namespaces
     */
    public void setValid(String namespace, boolean valid)
    {
        this.valid.put(namespace, valid);
    }

    @Override
    public boolean isDependency()
    {
        return getProperty(PKEY_DEPENDENCY, false);
    }

    /**
     * @param dependency indicate if the extension as been installed as a dependency of another one.
     * @see #isDependency()
     */
    public void setDependency(boolean dependency)
    {
        putProperty(PKEY_DEPENDENCY, dependency);
    }

    // LocalExtension

    @Override
    public LocalExtensionFile getFile()
    {
        return getLocalExtension().getFile();
    }
}
