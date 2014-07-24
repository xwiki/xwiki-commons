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
import java.util.Collections;
import java.util.HashMap;
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
     * Cache namespaces since they are used a lot.
     */
    private Collection<String> namespacesCache;

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
        Collection<String> namespaces;

        Object namespacesObject = extension.getProperty(PKEY_NAMESPACES);

        // RETRO-COMPATIBILITY: used to be a String collection with just the actual namespaces
        if (namespacesObject == null) {
            namespaces = null;
        } else if (namespacesObject instanceof Collection) {
            namespaces = (Collection<String>) namespacesObject;
        } else {
            namespaces = ((Map<String, Map<String, Object>>) namespacesObject).keySet();
        }

        return namespaces;
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

    @Override
    public Collection<String> getNamespaces()
    {
        if (this.namespacesCache == null) {
            Map<String, Map<String, Object>> installedNamespaces = getInstalledNamespaces(false);

            if (installedNamespaces != null) {
                this.namespacesCache = Collections.unmodifiableSet(installedNamespaces.keySet());
            }
        }

        return this.namespacesCache;
    }

    /**
     * @param namespaces the namespaces in which this extension is enabled. Null means root namespace (i.e all
     *            namespaces).
     * @see #getNamespaces()
     */
    public void setNamespaces(Collection<String> namespaces)
    {
        if (namespaces == null) {
            this.namespacesCache = null;
            putProperty(PKEY_NAMESPACES, null);
        } else {
            Map<String, Map<String, Object>> installedNamespaces = new HashMap<String, Map<String, Object>>();
            for (String namespace : namespaces) {
                Map<String, Object> namespaceData = new HashMap<String, Object>();
                namespaceData.put(PKEY_NAMESPACES_NAMESPACE, namespace);
                installedNamespaces.put(namespace, namespaceData);
                putProperty(PKEY_NAMESPACES, installedNamespaces);
            }
        }
    }

    /**
     * @param namespace the namespace
     * @see #getNamespaces()
     */
    public void addNamespace(String namespace)
    {
        getInstalledNamespace(namespace, true);
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
                Map<String, Map<String, Object>> installedNamespaces = getInstalledNamespaces(false);
                if (installedNamespaces != null) {
                    installedNamespaces.remove(namespace);

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

    /**
     * @return the installed namespaces
     * @param create indicate if the map should be create if it does not exists
     */
    private Map<String, Map<String, Object>> getInstalledNamespaces(boolean create)
    {
        Object namespacesObject = getProperty(PKEY_NAMESPACES);

        Map<String, Map<String, Object>> installedNamespaces = null;
        // RETRO-COMPATIBILITY: used to be a String collection with just the actual namespaces
        if (namespacesObject instanceof Collection) {
            Collection<String> namespaces = (Collection<String>) namespacesObject;
            setNamespaces(namespaces);
        } else {
            installedNamespaces = (Map<String, Map<String, Object>>) namespacesObject;

        }

        if (installedNamespaces == null && create) {
            installedNamespaces = new HashMap<String, Map<String, Object>>();
            putProperty(PKEY_NAMESPACES, installedNamespaces);
        }

        return installedNamespaces;
    }

    /**
     * @param namespace the namespace
     * @param create indicate if the {@link InstalledNamespace} should be create if it does not exists
     * @return the corresponding {@link InstalledNamespace}
     */
    private Map<String, Object> getInstalledNamespace(String namespace, boolean create)
    {
        Map<String, Map<String, Object>> namespaces = getInstalledNamespaces(create);

        if (namespaces == null) {
            return null;
        }

        Map<String, Object> installedNamespace = namespaces.get(namespace);

        if (installedNamespace == null && create) {
            installedNamespace = new HashMap<String, Object>();
            namespaces.put(namespace, installedNamespace);
        }

        return installedNamespace;
    }

    @Override
    public boolean isDependency()
    {
        return isDependency(null);
    }

    @Override
    public boolean isDependency(String namespace)
    {
        boolean isDependency;

        if (namespace == null) {
            isDependency = getProperty(PKEY_DEPENDENCY, false);
        } else {
            Map<String, Object> installedNamespace = getInstalledNamespace(namespace, false);

            isDependency =
                installedNamespace != null ? (installedNamespace.get(PKEY_NAMESPACES_DEPENDENCY) == Boolean.TRUE)
                    : isDependency(null);
        }

        return isDependency;
    }

    /**
     * @param dependency indicate if the extension has been installed as a dependency of another one
     * @see #isDependency()
     * @deprecated
     */
    @Deprecated
    public void setDependency(boolean dependency)
    {
        putProperty(PKEY_DEPENDENCY, dependency);
    }

    /**
     * @param dependency indicate if the extension has been installed as a dependency of another one
     * @param namespace the namespace
     * @see #isDependency(String)
     */
    public void setDependency(boolean dependency, String namespace)
    {
        if (namespace == null) {
            putProperty(PKEY_DEPENDENCY, dependency);
        } else {
            Map<String, Object> installedNamespace = getInstalledNamespace(namespace, false);

            if (installedNamespace != null) {
                installedNamespace.put(PKEY_NAMESPACES_DEPENDENCY, dependency);
            }
        }
    }

    // LocalExtension

    @Override
    public LocalExtensionFile getFile()
    {
        return getLocalExtension().getFile();
    }
}
