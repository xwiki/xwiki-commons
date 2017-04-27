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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
     * Custom property key containing the properties associated with the root namespace (when the extension is installed
     * on all namespaces).
     */
    private static final String PKEY_ROOT_NAMESPACE = PKEY_PREFIX + "root";

    /**
     * Custom property key containing {@link #getInstallDate(String)}.
     */
    private static final String PKEY_DATE = "date";

    /**
     * @see #getLocalExtension()
     */
    private LocalExtension localExtension;

    /**
     * @see #isValid(String)
     */
    private Map<String, Boolean> valid;

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
     * @see InstalledExtension#isInstalled()
     */
    public static boolean isInstalled(Extension extension)
    {
        return extension.getProperty(PKEY_INSTALLED, false);
    }

    /**
     * @param extension the extension
     * @param namespace the namespace to look at, if null it means the extension is installed for all the namespaces
     * @return true if the extension is installed in the provided namespace
     * @see InstalledExtension#isInstalled(String)
     */
    public static boolean isInstalled(Extension extension, String namespace)
    {
        return isInstalled(extension)
            && (getNamespaces(extension) == null || getNamespaces(extension).contains(namespace));
    }

    /**
     * Indicate if the extension as been installed as a dependency of another one.
     *
     * @param extension the extension
     * @param namespace the namespace to look at, null indicate the root namespace
     * @return true if the the extension has been installed only because it was a dependency of another extension
     * @see InstalledExtension#isDependency(String)
     * @since 8.2RC1
     */
    public static boolean isDependency(Extension extension, String namespace)
    {
        boolean isDependency = false;

        if (namespace == null) {
            isDependency = extension.getProperty(PKEY_DEPENDENCY, false);
        } else {
            Object namespacesObject = extension.getProperty(PKEY_NAMESPACES);

            // RETRO-COMPATIBILITY: used to be a String collection with just the actual namespaces
            if (namespacesObject instanceof Map) {
                Map<String, Object> installedNamespace =
                    ((Map<String, Map<String, Object>>) namespacesObject).get(namespace);

                isDependency =
                    installedNamespace != null ? (installedNamespace.get(PKEY_NAMESPACES_DEPENDENCY) == Boolean.TRUE)
                        : isDependency(extension, null);
            } else {
                isDependency = isDependency(extension, null);
            }
        }

        return isDependency;
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
            Map<String, Map<String, Object>> installedNamespaces = getInstalledNamespaces();

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
        try {
            this.propertiesLock.lock();

            if (namespaces == null) {
                putProperty(PKEY_ROOT_NAMESPACE, isInstalled() ? new HashMap<String, Object>() : null);
                putProperty(PKEY_NAMESPACES, null);
            } else {
                putProperty(PKEY_ROOT_NAMESPACE, null);
                Map<String, Map<String, Object>> installedNamespaces =
                    new ConcurrentHashMap<String, Map<String, Object>>();
                putProperty(PKEY_NAMESPACES, installedNamespaces);
                for (String namespace : namespaces) {
                    Map<String, Object> namespaceData = new HashMap<String, Object>();
                    namespaceData.put(PKEY_NAMESPACES_NAMESPACE, namespace);
                    installedNamespaces.put(namespace, namespaceData);
                }
            }
        } finally {
            this.propertiesLock.unlock();
        }

        this.namespacesCache = null;
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
        try {
            this.propertiesLock.lock();

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
                    Map<String, Map<String, Object>> installedNamespaces = getInstalledNamespaces();
                    if (installedNamespaces != null) {
                        installedNamespaces.remove(namespace);

                        if (getNamespaces().isEmpty()) {
                            setInstalled(false);
                            setNamespaces(null);
                        }
                    }
                }
            }
        } finally {
            this.propertiesLock.unlock();
        }

        if (!installed) {
            removeValid(namespace);
        }
    }

    private void removeValid(String namespace)
    {
        if (this.valid != null) {
            this.valid.remove(namespace);
        }
    }

    /**
     * @param namespace the namespace
     * @return true if the extension has been explicitly indicated as valid or invalid
     * @since 8.2.1
     * @since 8.3M1
     */
    public boolean isValidated(String namespace)
    {
        if (this.valid == null) {
            return false;
        }

        return this.valid.get(namespace) != null;
    }

    @Override
    public boolean isValid(String namespace)
    {
        Boolean isvalid = this.valid != null ? this.valid.get(namespace) : null;

        return isvalid != null ? isvalid : true;
    }

    /**
     * @param valid indicate of the installed extension is valid
     * @param namespace the namespace to look at, if null it means the extension is installed for all the namespaces
     */
    public void setValid(String namespace, boolean valid)
    {
        Map<String, Boolean> validMap =
            this.valid != null ? new HashMap<String, Boolean>(this.valid) : new HashMap<String, Boolean>();
        validMap.put(namespace, valid);

        this.valid = validMap;
    }

    /**
     * @return the installed namespaces
     */
    private Map<String, Map<String, Object>> getInstalledNamespaces()
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

        return installedNamespaces;
    }

    /**
     * @param namespace the namespace
     * @param create indicate if the {@link InstalledNamespace} should be create if it does not exists
     * @return the corresponding {@link InstalledNamespace}
     */
    private Map<String, Object> getInstalledNamespace(String namespace, boolean create)
    {
        Map<String, Map<String, Object>> namespaces = getInstalledNamespaces();

        if (namespaces != null) {
            Map<String, Object> installedNamespace = namespaces.get(namespace);

            if (installedNamespace != null) {
                return installedNamespace;
            }
        }

        return create ? maybeCreateInstalledNamespace(namespaces, namespace) : null;
    }

    private Map<String, Object> maybeCreateInstalledNamespace(Map<String, Map<String, Object>> namespaces,
        String namespace)
    {
        try {
            this.propertiesLock.lock();

            // Can't use ConcurrentHashMap because we have null key (root namespace)
            Map<String, Map<String, Object>> newNamespaces;
            if (namespaces != null) {
                newNamespaces = new HashMap<>(namespaces);
            } else {
                newNamespaces = new HashMap<>();
            }

            // Create the map for the namespace
            Map<String, Object> installedNamespace = new ConcurrentHashMap<String, Object>();
            newNamespaces.put(namespace, installedNamespace);

            // Set the new map of properties
            putProperty(PKEY_NAMESPACES, newNamespaces);

            // Reset the cache
            this.namespacesCache = null;

            return installedNamespace;
        } finally {
            this.propertiesLock.unlock();
        }
    }

    @Override
    @Deprecated
    public boolean isDependency()
    {
        return isDependency(null);
    }

    @Override
    public boolean isDependency(String namespace)
    {
        return isDependency(this, namespace);
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
        try {
            this.propertiesLock.unlock();

            if (namespace == null) {
                putProperty(PKEY_DEPENDENCY, dependency);
            } else {
                Map<String, Object> installedNamespace = getInstalledNamespace(namespace, false);

                if (installedNamespace != null) {
                    installedNamespace.put(PKEY_NAMESPACES_DEPENDENCY, dependency);
                }
            }
        } finally {
            this.propertiesLock.unlock();
        }
    }

    @Override
    public Date getInstallDate(String namespace)
    {
        return (Date) getNamespaceProperty(PKEY_DATE, namespace);
    }

    /**
     * Sets the date when this extension has been installed on the specified namespace.
     * 
     * @param date the install date
     * @param namespace the namespace for which to set the install date
     * @since 7.0M2
     */
    public void setInstallDate(Date date, String namespace)
    {
        setNamespaceProperty(PKEY_DATE, date, namespace);
    }

    @Override
    public Object getNamespaceProperty(String key, String namespace)
    {
        Object value = null;
        if (namespace != null) {
            Map<String, Object> installedNamespace = getInstalledNamespace(namespace, false);
            if (installedNamespace != null) {
                value = installedNamespace.get(key);
            }
        }
        if (value == null) {
            // Fallback on the root namespace.
            // Note that we don't pass an empty map as default value because the value mapped to the key can be null.
            Map<String, Object> rootNamespace = getProperty(PKEY_ROOT_NAMESPACE, (Map<String, Object>) null);
            if (rootNamespace != null) {
                value = rootNamespace.get(key);
            }
        }
        return value;
    }

    /**
     * Sets the value of the specified extension property on the given namespace.
     * 
     * @param key the extension property to set
     * @param value the property value
     * @param namespace the namespace to associate the property with, {@code null} for the root namespace
     * @since 7.0M2
     */
    public void setNamespaceProperty(String key, Object value, String namespace)
    {
        try {
            this.propertiesLock.lock();

            Map<String, Object> namespaceProperties = getNamespaceProperties(namespace);
            if (namespaceProperties != null) {
                namespaceProperties.put(key, value);
            }
        } finally {
            this.propertiesLock.unlock();
        }
    }

    /**
     * @param namespace the namespace to look for
     * @return the custom extension properties associated with the specified namespace
     * @since 7.0M2
     */
    public Map<String, Object> getNamespaceProperties(String namespace)
    {
        if (namespace == null) {
            return getProperty(PKEY_ROOT_NAMESPACE, (Map<String, Object>) null);
        } else {
            return getInstalledNamespace(namespace, false);
        }
    }

    // LocalExtension

    @Override
    public LocalExtensionFile getFile()
    {
        return getLocalExtension().getFile();
    }
}
