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

import org.junit.jupiter.api.Test;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManager;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultExtensionManager}.
 *
 * @version $Id$
 * @since 5.3M1
 */
@ComponentTest
class DefaultExtensionManagerTest
{
    @InjectMockComponents
    private DefaultExtensionManager manager;

    @MockComponent
    private CoreExtensionRepository coreExtensionRepository;

    @MockComponent
    private LocalExtensionRepository localExtensionRepository;

    @MockComponent
    private InstalledExtensionRepository installedExtensionRepository;

    @BeforeComponent
    void setUp()
    {
        mockExtensionRepositoryDescriptor(this.coreExtensionRepository, "core");
        mockExtensionRepositoryDescriptor(this.localExtensionRepository, "local");
        mockExtensionRepositoryDescriptor(this.installedExtensionRepository, "installed");
    }

    private ExtensionRepositoryDescriptor mockExtensionRepositoryDescriptor(ExtensionRepository repository, String id)
    {
        ExtensionRepositoryDescriptor descriptor = mock(ExtensionRepositoryDescriptor.class, id);
        when(repository.getDescriptor()).thenReturn(descriptor);
        when(descriptor.getId()).thenReturn(id);
        return descriptor;
    }

    /**
     * Unit test for {@link ExtensionManager#resolveExtension(org.xwiki.extension.ExtensionDependency, String)}.
     */
    @Test
    void resolveMissingExtensionDependencyOnNamespace() throws Exception
    {
        ExtensionDependency extensionDependency = mock(ExtensionDependency.class);

        doThrow(ResolveException.class).when(this.coreExtensionRepository).resolve(extensionDependency);
        doThrow(ResolveException.class).when(this.localExtensionRepository).resolve(extensionDependency);

        assertNull(this.manager.resolveExtension(extensionDependency, "wiki:math"));
    }

    /**
     * Unit test for {@link ExtensionManager#resolveExtension(org.xwiki.extension.ExtensionDependency, String)}.
     */
    @Test
    void resolveInstalledExtensionDependencyOnNamespace() throws Exception
    {
        String namespace = "wiki:math";
        String extensionId = "test:extension";

        ExtensionDependency extensionDependency = mock(ExtensionDependency.class);
        doThrow(ResolveException.class).when(this.coreExtensionRepository).resolve(extensionDependency);

        InstalledExtension extension = mock(InstalledExtension.class);
        when(extensionDependency.getId()).thenReturn(extensionId);
        when(this.installedExtensionRepository.getInstalledExtension(extensionId, namespace)).thenReturn(extension);

        Version version = mock(Version.class);
        when(extension.getId()).thenReturn(new ExtensionId(extensionId, version));

        VersionConstraint versionConstraint = mock(VersionConstraint.class);
        when(extensionDependency.getVersionConstraint()).thenReturn(versionConstraint);
        when(versionConstraint.containsVersion(version)).thenReturn(true);

        assertSame(extension, this.manager.resolveExtension(extensionDependency, namespace));
    }

    @Test
    void resolveInstalledExtensionId() throws Exception
    {
        ExtensionId extensionId = new ExtensionId("extension", "version");

        doThrow(ResolveException.class).when(this.coreExtensionRepository).resolve(extensionId);

        InstalledExtension extension = mock(InstalledExtension.class);
        when(this.installedExtensionRepository.resolve(extensionId)).thenReturn(extension);

        when(extension.getId()).thenReturn(extensionId);

        assertSame(extension, this.manager.resolveExtension(extensionId));
    }
}
