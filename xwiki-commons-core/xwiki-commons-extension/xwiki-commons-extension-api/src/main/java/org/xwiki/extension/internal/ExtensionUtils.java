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
package org.xwiki.extension.internal;

import java.util.HashMap;
import java.util.Map;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;

/**
 * Various extension related utilities.
 * 
 * @version $Id$
 * @since 8.2RC1
 */
public final class ExtensionUtils
{
    private ExtensionUtils()
    {
        // Utility class
    }

    /**
     * @param dependency the initial dependency
     * @param managedDependencies the managed dependencies
     * @param extension the extension with the passed dependency
     * @return the actual dependency to resolve
     */
    public static ExtensionDependency getDependency(ExtensionDependency dependency,
        Map<String, ExtensionDependency> managedDependencies, Extension extension)
    {
        ExtensionDependency managedDependency = managedDependencies.get(dependency.getId());

        // If the dependency does not have any version try to find it in extension managed dependencies
        if (managedDependency == null && dependency.getVersionConstraint() == null) {
            for (ExtensionDependency extensionManagedDependency : extension.getManagedDependencies()) {
                if (extensionManagedDependency.getId().equals(dependency.getId())) {
                    managedDependency = extensionManagedDependency;
                }
            }
        }

        return managedDependency != null ? managedDependency : dependency;
    }

    /**
     * @param managedDependencies the managed dependencies
     * @param extension the extension for which to append managed dependencies
     * @return the new map of managed dependencies
     */
    public static Map<String, ExtensionDependency> append(Map<String, ExtensionDependency> managedDependencies,
        Extension extension)
    {
        Map<String, ExtensionDependency> newManagedDependencies =
            managedDependencies != null ? new HashMap<>(managedDependencies) : new HashMap<>();

        for (ExtensionDependency dependency : extension.getManagedDependencies()) {
            newManagedDependencies.put(dependency.getId(), dependency);
        }

        return newManagedDependencies;
    }
}
