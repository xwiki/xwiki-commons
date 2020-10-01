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
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.test.AbstractExtensionHandlerTest;
import org.xwiki.extension.test.MockitoRepositoryUtilsExtension;
import org.xwiki.extension.test.TestExtensionHandler;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentTest
@ExtendWith(MockitoRepositoryUtilsExtension.class)
class InstallJobTest extends AbstractExtensionHandlerTest
{
    private TestExtensionHandler handler;

    @BeforeEach
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        // lookup

        this.handler = this.componentManager.getInstance(ExtensionHandler.class, "test");
    }

    @Test
    void testInstallOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID);

        // Is extension installed
        InstalledExtension installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), null);
        assertNotNull(installedExtension);
        assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), "namespace"));
        assertFalse(installedExtension.isDependency(null));
        assertFalse(installedExtension.isDependency("namespace"));

        // Is dependency installed
        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), null);
        assertNotNull(installedExtension);
        assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(),
            "namespace"));
        assertTrue(installedExtension.isDependency(null));
        assertTrue(installedExtension.isDependency("namespace"));
    }

    @Test
    void testInstallNoType() throws Throwable
    {
        install(TestResources.REMOTE_NOTYPE_ID);

        // Is extension installed
        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_NOTYPE_ID.getId(), null);
        assertNotNull(installedExtension);
        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_NOTYPE_ID.getId(),
            "namespace"));
        assertNull(installedExtension.getType());
        assertNull(installedExtension.getFile());
    }

    @Test
    void testInstallRemoteOnNamespace() throws Throwable
    {
        install(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID, "namespace");

        // Is extension installed
        InstalledExtension installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), "namespace");
        assertNotNull(installedExtension);
        assertTrue(this.handler.getExtensions().get("namespace").contains(installedExtension));
        assertFalse(installedExtension.isDependency(null));
        assertFalse(installedExtension.isDependency("namespace"));

        // Is dependency installed
        installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), "namespace");
        assertNotNull(installedExtension);
        assertTrue(this.handler.getExtensions().get("namespace").contains(installedExtension));
        assertFalse(installedExtension.isDependency(null));
        assertTrue(installedExtension.isDependency("namespace"));
    }

    @Test
    void testInstallRemoteOnNamespaces() throws Throwable
    {
        install(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID, new String[] {"namespace1", "namespace2"});

        LocalExtension installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), "namespace1");
        assertNotNull(installedExtension);
        assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), "namespace2"));
        assertTrue(this.handler.getExtensions().get("namespace1").contains(installedExtension));
        assertTrue(this.handler.getExtensions().get("namespace2").contains(installedExtension));

        installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), "namespace1");
        assertNotNull(installedExtension);
        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(),
            "namespace2"));
        assertTrue(this.handler.getExtensions().get("namespace1").contains(installedExtension));
        assertTrue(this.handler.getExtensions().get("namespace2").contains(installedExtension));
    }

    @Test
    void testUpgradeFirstOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_UPGRADE10_ID);

        LocalExtension installedExtension;

        // Test upgrade

        install(TestResources.REMOTE_UPGRADE20_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), null);
        assertNotNull(installedExtension);
        assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(),
            "namespace"));

        assertNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID));

        // Test downgrade

        install(TestResources.REMOTE_UPGRADE10_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), null);
        assertNotNull(installedExtension);
        assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(),
            "namespace"));
    }

    @Test
    void testDowngradeFirstOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_UPGRADE20_ID);

        LocalExtension installedExtension;

        // //////////////////
        // Test downgrade

        install(TestResources.REMOTE_UPGRADE10_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), null);
        assertNotNull(installedExtension);
        assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(),
            "namespace"));

        // Test upgrade

        install(TestResources.REMOTE_UPGRADE20_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), null);
        assertNotNull(installedExtension);
        assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(),
            "namespace"));
    }

    @Test
    void testReplaceDependencyWithFeature() throws Throwable
    {
        InstalledExtension installedextension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID);

        // installedextensiondependency 1.0
        assertEquals(Arrays.asList(installedextension), this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
        assertEquals(Collections.singletonMap(null, Arrays.asList(installedextension)),
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID));

        ExtensionId installedextensiondependency2 = new ExtensionId("overrideinstalledextensiondependency", "2.0");

        // change dependency
        install(installedextensiondependency2);

        // installedextensiondependency 2.0
        assertEquals(Arrays.asList(installedextension), this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
        // overrideinstalledextensiondependency
        assertEquals(Arrays.asList(installedextension),
            this.installedExtensionRepository.getBackwardDependencies(installedextensiondependency2.getId(), null));
        assertEquals(Collections.singletonMap(null, Arrays.asList(installedextension)),
            this.installedExtensionRepository.getBackwardDependencies(installedextensiondependency2));
    }

    @Test
    void testInstallOnNamespaceThenOnRoot() throws Throwable
    {
        // Install 1.0 on "namespace"

        install(TestResources.REMOTE_UPGRADE10_ID, "namespace");

        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID);

        assertNotNull(installedExtension);
        assertNotNull(installedExtension.getNamespaces());
        assertTrue(this.handler.getExtensions().get("namespace").contains(installedExtension));

        // Install 1.0 on root

        install(TestResources.REMOTE_UPGRADE10_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), null);
        assertSame(installedExtension, this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), "namespace"));
        assertNotNull(installedExtension);
        assertNull(installedExtension.getNamespaces(), "Extension is not marked as installed on root");
        assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
    }

    @Test
    void testInstallOnNamespaceThenUpgradeOnRoot() throws Throwable
    {
        // Install 1.0 on "namespace"

        install(TestResources.REMOTE_UPGRADE10_ID, "namespace");

        InstalledExtension installedExtension1 =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID);

        assertNotNull(installedExtension1);

        // Upgrade 2.0 on root

        install(TestResources.REMOTE_UPGRADE20_ID);

        installedExtension1 =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID);

        assertNull(installedExtension1);

        LocalExtension installedExtension2 =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), null);
        assertNotNull(installedExtension2);
        assertTrue(this.handler.getExtensions().get(null).contains(installedExtension2));

        assertNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID));
    }

    @Test
    void testInstallOnNamespaceThenMoveDependencyOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_WITHRDEPENDENCY_ID, "namespace");

        assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRDEPENDENCY_ID.getId(), "namespace"));
        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(),
            "namespace"));

        install(TestResources.REMOTE_SIMPLE_ID);

        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(),
            "namespace"));
        assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), null));
        assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRDEPENDENCY_ID.getId(), "namespace"));
        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRDEPENDENCY_ID.getId(), null));
    }

    @Test
    void testInstallOnNamespaceThenOnRootWithLowerDependency() throws Throwable
    {
        // Install extension on namespace
        install(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID, "namespace");
        // Upgrade dependency
        install(TestResources.REMOTE_OTHERUPGRADEWITHDEPENDENCY20_ID, "namespace");

        assertNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID));

        // Install extension on root
        install(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID);

        assertNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID));

        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(),
            "namespace"));
        assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), null));
        assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID.getId(), "namespace"));
        assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID.getId(), null));
    }

    @Test
    void testInstallRemoteWithMissingDependency() throws Throwable
    {
        install(TestResources.REMOTE_WITHRMISSINGDEPENDENCY_ID);

        InstalledExtension installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRMISSINGDEPENDENCY_ID.getId(), "namespace");
        assertNotNull(installedExtension);
        assertEquals(TestResources.REMOTE_WITHRMISSINGDEPENDENCY_ID, installedExtension.getId());
        assertTrue(installedExtension.isValid("namespace"));

        installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_WITHMISSINDEPENDENCY_ID.getId(), "namespace");
        assertNotNull(installedExtension);
        assertEquals(TestResources.INSTALLED_WITHMISSINDEPENDENCY_ID, installedExtension.getId());
        assertTrue(installedExtension.isValid("namespace"));

        installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_MISSINGDEPENDENCY_ID.getId(), "namespace");
        assertNotNull(installedExtension);
        assertEquals(TestResources.REMOTE_MISSINGDEPENDENCY_ID, installedExtension.getId());
        assertTrue(installedExtension.isValid("namespace"));
    }

    @Test
    void testInstallWithExcludedRemoteTransitiveDependencyOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_WITHEXCLUDEDRTDEPENDENCY_ID);

        InstalledExtension installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHEXCLUDEDRTDEPENDENCY_ID.getId(), null);
        assertNotNull(installedExtension);
        assertEquals(TestResources.REMOTE_WITHEXCLUDEDRTDEPENDENCY_ID, installedExtension.getId());
        assertTrue(installedExtension.isValid(null));
        assertFalse(installedExtension.isDependency(null));

        installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRDEPENDENCY_ID.getId(), null);
        assertNotNull(installedExtension);
        assertEquals(TestResources.REMOTE_WITHRDEPENDENCY_ID, installedExtension.getId());
        assertTrue(installedExtension.isValid(null));
        assertTrue(installedExtension.isDependency(null));

        installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHROTHERDEPENDENCY_ID.getId(), null);
        assertNotNull(installedExtension);
        assertEquals(TestResources.REMOTE_WITHROTHERDEPENDENCY_ID, installedExtension.getId());
        assertTrue(installedExtension.isValid(null));
        assertTrue(installedExtension.isDependency(null));

        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), null));

        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), null));
    }
}
