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
package org.xwiki.extension.repository.aether.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.aether.graph.Dependency;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.maven.internal.DefaultMavenExtensionDependency;
import org.xwiki.extension.maven.internal.MavenUtils;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class AetherExtensionDependency extends DefaultMavenExtensionDependency
{
    public static final String PKEY_AETHER_DEPENDENCY = "aether.Dependency";

    public AetherExtensionDependency(ExtensionDependency extensionDependency, Dependency aetherDependency,
        ExtensionRepositoryDescriptor extensionRepository)
    {
        super(extensionDependency);

        // Make sure the dependency will be resolved in the extension repository first
        if (extensionRepository != null) {
            List<ExtensionRepositoryDescriptor> newRepositories = new ArrayList<>(getRepositories().size() + 1);

            newRepositories.add(extensionRepository);
            newRepositories.addAll(getRepositories());

            setRepositories(newRepositories);
        }

        // Custom properties
        putProperty(PKEY_AETHER_DEPENDENCY, aetherDependency);
    }

    public AetherExtensionDependency(Dependency aetherDependency)
    {
        super(new DefaultExtensionDependency(MavenUtils.toExtensionId(aetherDependency.getArtifact().getGroupId(),
            aetherDependency.getArtifact().getArtifactId(), aetherDependency.getArtifact().getClassifier()),
            new DefaultVersionConstraint(aetherDependency.getArtifact().getVersion()), null));

        // custom properties
        putProperty(PKEY_AETHER_DEPENDENCY, aetherDependency);
    }

    public Dependency getAetherDependency()
    {
        return (Dependency) this.getProperty(PKEY_AETHER_DEPENDENCY);
    }
}
