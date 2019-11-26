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

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.test.AbstractExtensionHandlerTest;
import org.xwiki.extension.test.MockitoRepositoryUtilsExtension;
import org.xwiki.extension.test.TestExtensionHandler;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentTest
@ExtendWith(MockitoRepositoryUtilsExtension.class)
public class UninstallJobTest extends AbstractExtensionHandlerTest
{
    private TestResources resources;

    private TestExtensionHandler handler;

    @BeforeEach
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        // lookup

        this.handler = this.componentManager.getInstance(ExtensionHandler.class, "test");

        // resources

        this.resources = new TestResources();
        this.resources.init(this.installedExtensionRepository);
    }

    @Test
    public void testUninstall() throws Throwable
    {
        uninstall(TestResources.INSTALLED_ID);

        assertFalse(this.handler.getExtensions().get(null).contains(this.resources.installed));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), null));

        assertTrue(this.handler.getExtensions().get(null).contains(this.resources.installedDependency));
        assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
    }

    @Test
    public void testUninstallWithBackwarDepencency() throws Throwable
    {
        uninstall(TestResources.INSTALLED_DEPENDENCY_ID);

        assertFalse(this.handler.getExtensions().get(null).contains(this.resources.installed));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), null));

        assertFalse(this.handler.getExtensions().get(null).contains(this.resources.installedDependency));
        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
    }

    @Test
    public void testUninstallTwice() throws Throwable
    {
        uninstall(TestResources.INSTALLED_ID);

        assertThrows(UninstallException.class, () -> {
            uninstall(TestResources.INSTALLED_ID);
        });
    }

    @Test
    public void testUninstallFromNamespace() throws Throwable
    {
        // prepare

        uninstall(TestResources.INSTALLED_DEPENDENCY_ID);
        install(TestResources.INSTALLED_ID, "namespace1");
        install(TestResources.INSTALLED_ID, "namespace2");

        // actual test

        uninstall(TestResources.INSTALLED_ID, "namespace1");

        assertFalse(this.handler.getExtensions().get("namespace1").contains(this.resources.installed));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace1"));

        assertTrue(this.handler.getExtensions().get("namespace2").contains(this.resources.installed));
        assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace2"));
    }

    @Test
    public void testUninstallFromNamespaceWithBackwarDepencency() throws Throwable
    {
        // prepare

        uninstall(TestResources.INSTALLED_DEPENDENCY_ID);
        install(TestResources.INSTALLED_ID, "namespace1");
        install(TestResources.INSTALLED_ID, "namespace2");

        // actual test

        uninstall(TestResources.INSTALLED_DEPENDENCY_ID, "namespace1");

        assertFalse(this.handler.getExtensions().get("namespace1").contains(this.resources.installed));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace1"));
        assertFalse(this.handler.getExtensions().get("namespace1").contains(this.resources.installedDependency));
        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_DEPENDENCY_ID.getId(), "namespace1"));

        assertTrue(this.handler.getExtensions().get("namespace2").contains(this.resources.installed));
        assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace2"));
        assertTrue(this.handler.getExtensions().get("namespace2").contains(this.resources.installed));
        assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace2"));
    }

    @Test
    public void testUninstallFromAllNamespaces() throws Throwable
    {
        // prepare

        uninstall(TestResources.INSTALLED_DEPENDENCY_ID);
        install(TestResources.INSTALLED_ID, "namespace1");
        install(TestResources.INSTALLED_ID, "namespace2");

        // actual test

        uninstall(TestResources.INSTALLED_ID);

        assertFalse(this.handler.getExtensions().get("namespace1").contains(this.resources.installed));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace1"));

        assertFalse(this.handler.getExtensions().get("namespace2").contains(this.resources.installed));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace2"));
    }

    @Test
    public void testUninstallFromSeveralNamespaceIncludingWrong() throws Throwable
    {
        // prepare

        uninstall(TestResources.INSTALLED_DEPENDENCY_ID);
        install(TestResources.INSTALLED_ID, "namespace1");
        install(TestResources.INSTALLED_ID, "namespace2");

        // actual test

        uninstall(TestResources.INSTALLED_ID, Arrays.asList("namespace1", "nonamespace"));

        assertFalse(this.handler.getExtensions().get("namespace1").contains(this.resources.installed));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace1"));

        assertTrue(this.handler.getExtensions().get("namespace2").contains(this.resources.installed));
        assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace2"));
    }
}
