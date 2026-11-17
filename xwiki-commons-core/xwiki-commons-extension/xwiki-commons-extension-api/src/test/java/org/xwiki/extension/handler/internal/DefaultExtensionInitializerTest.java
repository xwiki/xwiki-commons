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
package org.xwiki.extension.handler.internal;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.handler.ExtensionHandlerManager;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Validate {@link DefaultExtensionInitializer}.
 *
 * @version $Id$
 */
@ComponentTest
class DefaultExtensionInitializerTest
{
    private static final String NAMESPACE = "namespace";

    private static final String ROOT_EXTENSION_ID = "rootextension";

    @InjectMockComponents
    private DefaultExtensionInitializer initializer;

    @MockComponent
    private InstalledExtensionRepository installedExtensionRepository;

    @MockComponent
    private ExtensionHandlerManager extensionHandlerManager;

    @MockComponent
    private CoreExtensionRepository coreExtensionRepository;

    private InstalledExtension rootExtension;

    private InstalledExtension namespaceExtension;

    @BeforeEach
    void beforeEach()
    {
        // An extension installed on the root namespace (so available in all namespaces)
        this.rootExtension = mock(InstalledExtension.class);
        when(this.rootExtension.getNamespaces()).thenReturn(null);
        when(this.rootExtension.isValid(any())).thenReturn(true);

        // An extension installed on a namespace, which depends on the extension installed on the root namespace
        ExtensionDependency dependency = mock(ExtensionDependency.class);
        when(dependency.getId()).thenReturn(ROOT_EXTENSION_ID);
        this.namespaceExtension = mock(InstalledExtension.class);
        when(this.namespaceExtension.getNamespaces()).thenReturn(List.of(NAMESPACE));
        when(this.namespaceExtension.isValid(NAMESPACE)).thenReturn(true);
        when(this.namespaceExtension.getDependencies()).thenReturn(List.of(dependency));

        when(this.installedExtensionRepository.getInstalledExtension(ROOT_EXTENSION_ID, NAMESPACE))
            .thenReturn(this.rootExtension);
    }

    @Test
    void initializeAllWhenNamespaceExtensionComesFirst() throws Exception
    {
        when(this.installedExtensionRepository.getInstalledExtensions())
            .thenReturn(List.of(this.namespaceExtension, this.rootExtension));

        this.initializer.initialize(null, null);

        // The dependency installed on the root namespace must be initialized (only once) before the extension which
        // needs it
        InOrder order = inOrder(this.extensionHandlerManager);
        order.verify(this.extensionHandlerManager).initialize(this.rootExtension, null);
        order.verify(this.extensionHandlerManager).initialize(this.namespaceExtension, NAMESPACE);
        order.verifyNoMoreInteractions();
    }

    @Test
    void initializeNamespace() throws Exception
    {
        when(this.installedExtensionRepository.getInstalledExtensions(NAMESPACE))
            .thenReturn(List.of(this.namespaceExtension));

        this.initializer.initialize(NAMESPACE, null);

        verify(this.extensionHandlerManager).initialize(this.namespaceExtension, NAMESPACE);
        // The dependency installed on the root namespace is not part of the namespace to initialize
        verify(this.extensionHandlerManager, never()).initialize(this.rootExtension, null);
        verify(this.extensionHandlerManager, never()).initialize(this.rootExtension, NAMESPACE);
    }
}
