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
package org.xwiki.extension.job.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionPattern;
import org.xwiki.extension.internal.ExtensionUtils;

/**
 * Used to maintain a context during extension install plan.
 * 
 * @version $Id$
 * @since 12.2
 */
public class ExtensionPlanContext
{
    private final Map<String, ExtensionDependency> managedDependencies;

    private final List<ExtensionPattern> excludes;

    /**
     * Empty context.
     */
    public ExtensionPlanContext()
    {
        this.managedDependencies = Collections.emptyMap();

        this.excludes = Collections.emptyList();
    }

    /**
     * @param extensionContext the extension context to copy
     * @param rewrittenExtension the current extension
     */
    public ExtensionPlanContext(ExtensionPlanContext extensionContext, Extension rewrittenExtension)
    {
        this.managedDependencies = ExtensionUtils.append(extensionContext.managedDependencies, rewrittenExtension);
        this.excludes = extensionContext.excludes;
    }

    /**
     * @param extensionContext the extension context to copy
     * @param dependency the current dependency
     */
    public ExtensionPlanContext(ExtensionPlanContext extensionContext, ExtensionDependency dependency)
    {
        this.managedDependencies = extensionContext.managedDependencies;
        this.excludes = ExtensionUtils.appendExclusions(extensionContext.excludes, dependency);
    }

    /**
     * @param dependency the initial dependency
     * @param extension the extension with the passed dependency
     * @return the actual dependency to resolve
     */
    public ExtensionDependency getDependency(ExtensionDependency dependency, Extension extension)
    {
        ExtensionDependency managedDependency = this.managedDependencies.get(dependency.getId());

        // If the dependency does not have any version try to find it in extension managed dependencies
        if (managedDependency == null && dependency.getVersionConstraint() == null) {
            for (ExtensionDependency extensionManagedDependency : extension.getManagedDependencies()) {
                if (extensionManagedDependency.getId().equals(dependency.getId())) {
                    managedDependency = extensionManagedDependency;
                }
            }
        }

        // If a managed dependency was found change the dependency version constraint
        if (managedDependency != null) {
            return new DefaultExtensionDependency(dependency, managedDependency.getVersionConstraint());
        }

        return dependency;
    }

    /**
     * @param dependency the dependency to match
     * @return true if the passed dependency should be excluded in the current context
     */
    public boolean isExcluded(ExtensionDependency dependency)
    {
        for (ExtensionPattern pattern : this.excludes) {
            if (pattern.matches(dependency)) {
                return true;
            }
        }

        return false;
    }
}
