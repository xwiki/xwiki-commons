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
import java.util.Date;

/**
 * Represent a local extension.
 * <p>
 * An installed extension.
 *
 * @version $Id$
 * @since 4.0M2
 */
public interface InstalledExtension extends LocalExtension
{
    /**
     * The prefix that should be used by all custom extension properties that are specific to installed extensions.
     * 
     * @since 7.0M2
     */
    String PKEY_PREFIX = "installed.";

    /**
     * Custom property key containing {@link #isInstalled()}.
     */
    String PKEY_INSTALLED = PKEY_PREFIX + "installed";

    /**
     * Custom property key containing {@link #getNamespaces()}.
     * <p>
     * Since 4.3M1 it's a {@code Map<String, Object>}.
     */
    String PKEY_NAMESPACES = PKEY_PREFIX + "namespaces";

    /**
     * Custom property key containing the namespace.
     */
    String PKEY_NAMESPACES_NAMESPACE = PKEY_NAMESPACES + ".namespace";

    /**
     * Custom property key containing {@link #isDependency(String)}.
     */
    String PKEY_NAMESPACES_DEPENDENCY = PKEY_NAMESPACES + ".dependency";

    /**
     * Custom property key containing {@link #isDependency(String)} with <code>null</code> namespace.
     */
    String PKEY_DEPENDENCY = PKEY_PREFIX + "dependency";

    /**
     * @see #getNamespaces()
     * @since 14.0RC1
     */
    String FIELD_INSTALLED_NAMESPACES = "installedNamespaces";

    /**
     * @return the actual extension
     */
    LocalExtension getLocalExtension();

    /**
     * @return indicate if the extension is installed
     */
    boolean isInstalled();

    /**
     * Indicate if the extension is installed in the provided namespace.
     *
     * @param namespace the namespace to look at, if null it means the extension is installed on the root namespace
     * @return true if the extension is installed in the provided namespace
     */
    boolean isInstalled(String namespace);

    /**
     * Indicate if the extension is working.
     * <p>
     * An installed extension can be invalid for example when one of the core extensions has been changed and is now
     * incompatible with this installed extension.
     *
     * @param namespace the namespace to look at, if null it means the extension is installed on the root namespace
     * @return true is valid
     * @since 4.2M1
     */
    boolean isValid(String namespace);

    /**
     * @return the namespaces in which this extension is enabled. null means root namespace (i.e all namespaces).
     */
    Collection<String> getNamespaces();

    /**
     * Indicate if the extension as been installed as a dependency of another one.
     * <p>
     * The idea is to be able to make the difference between extension specifically installed by a user so that it's
     * possible to know which extension are not really required anymore.
     *
     * @param namespace the namespace to look at, null indicate the root namespace
     * @return true if the the extension has been installed only because it was a dependency of another extension
     */
    boolean isDependency(String namespace);

    /**
     * @param namespace the namespace to look at, {@code null} indicates the root namespace
     * @return the date when this extension has been installed on the specified namespace, {@code null} if the install
     *         date is not available or if this extension is not installed on the specified namespace
     * @since 7.0M2
     */
    Date getInstallDate(String namespace);

    /**
     * An installed extension can have different values for its properties depending on the namespace where it is
     * installed. This method allows us to access the value of a specific extension property on a given namespace.
     * 
     * @param key the name of the property to look for
     * @param namespace the namespace to look at, {@code null} indicates the root namespace
     * @return the value of the specified extension property on the given namespace
     * @since 7.0M2
     */
    Object getNamespaceProperty(String key, String namespace);

    // Deprecated

    /**
     * Indicate if the extension as been installed as a dependency of another one.
     * <p>
     * The idea is to be able to make the difference between extension specifically installed by a user so that it's
     * possible to know which extension are not really required anymore.
     *
     * @return true if the the extension has been installed only because it was a dependency of another extension
     * @deprecated since 4.3M1 use {@link #isDependency(String)} with <code>null</code> namespace instead
     */
    @Deprecated
    boolean isDependency();
}
