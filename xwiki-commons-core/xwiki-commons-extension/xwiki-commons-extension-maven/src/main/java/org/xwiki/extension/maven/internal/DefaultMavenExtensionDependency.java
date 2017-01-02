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
package org.xwiki.extension.maven.internal;

import org.apache.maven.model.Dependency;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.version.VersionConstraint;

/**
 * Default implementation of {@link MavenExtension}.
 * 
 * @version $Id$
 * @since 7.3M1
 */
public class DefaultMavenExtensionDependency extends DefaultExtensionDependency implements MavenExtensionDependency
{
    /**
     * The key associated to the Maven dependency object.
     */
    public static final String PKEY_MAVEN_DEPENDENCY = "maven.Dependency";

    /**
     * The key associated to the Maven dependency object.
     * 
     * @since 8.1M1
     */
    public static final String PKEY_MAVEN_DEPENDENCY_SCOPE = "maven.dependency.scope";

    /**
     * The key associated to the Maven dependency object.
     * 
     * @since 8.1M1
     */
    public static final String PKEY_MAVEN_DEPENDENCY_OPTIONAL = "maven.dependency.optional";

    /**
     * Create new instance by cloning the provided one.
     *
     * @param dependency the extension dependency to copy
     */
    public DefaultMavenExtensionDependency(ExtensionDependency dependency)
    {
        super(dependency);
    }

    /**
     * @param extensionId the id of the extension dependency
     * @param constraint the version constraint of the extension dependency
     * @param mavenDependency the Maven dependency object
     */
    public DefaultMavenExtensionDependency(String extensionId, VersionConstraint constraint, Dependency mavenDependency)
    {
        super(extensionId, constraint);

        if (mavenDependency != null) {
            // custom properties lost when saving
            putProperty(PKEY_MAVEN_DEPENDENCY, mavenDependency);
            // custom properties to remember
            putProperty(PKEY_MAVEN_DEPENDENCY_SCOPE, mavenDependency.getScope());
            putProperty(PKEY_MAVEN_DEPENDENCY_OPTIONAL, mavenDependency.isOptional());
        }
    }

    /**
     * @param dependency the generic dependency
     * @return the scope of dependency
     * @since 8.1M1
     */
    public static String getScope(ExtensionDependency dependency)
    {
        return (String) dependency.getProperty(PKEY_MAVEN_DEPENDENCY_SCOPE);
    }

    /**
     * @param dependency the generic dependency
     * @return true is the dependency is optional
     * @since 8.1M1
     */
    public static boolean isOptional(ExtensionDependency dependency)
    {
        return dependency.getProperty(PKEY_MAVEN_DEPENDENCY_OPTIONAL, false);
    }

    @Override
    public Dependency getMavenDependency()
    {
        return (Dependency) getProperty(PKEY_MAVEN_DEPENDENCY);
    }

    @Override
    public String getScope()
    {
        return getScope(this);
    }

    @Override
    public boolean isOptional()
    {
        return isOptional(this);
    }
}
