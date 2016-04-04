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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.test.AbstractExtensionHandlerTest;
import org.xwiki.extension.test.TestExtensionHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InstallJobTest extends AbstractExtensionHandlerTest
{
    private TestExtensionHandler handler;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        // lookup

        this.handler = (TestExtensionHandler) this.mocker.getInstance(ExtensionHandler.class, "test");
    }

    @Test
    public void testInstallOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID);

        // Is extension installed
        InstalledExtension installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), null);
        Assert.assertNotNull(installedExtension);
        Assert.assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), "namespace"));
        Assert.assertFalse(installedExtension.isDependency(null));
        Assert.assertFalse(installedExtension.isDependency("namespace"));

        // Is dependency installed
        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), null);
        Assert.assertNotNull(installedExtension);
        Assert.assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), "namespace"));
        Assert.assertTrue(installedExtension.isDependency(null));
        Assert.assertTrue(installedExtension.isDependency("namespace"));
    }

    @Test
    public void testInstallRemoteOnNamespace() throws Throwable
    {
        install(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID, "namespace");

        // Is extension installed
        InstalledExtension installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), "namespace");
        Assert.assertNotNull(installedExtension);
        Assert.assertTrue(this.handler.getExtensions().get("namespace").contains(installedExtension));
        Assert.assertFalse(installedExtension.isDependency(null));
        Assert.assertFalse(installedExtension.isDependency("namespace"));

        // Is dependency installed
        installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), "namespace");
        Assert.assertNotNull(installedExtension);
        Assert.assertTrue(this.handler.getExtensions().get("namespace").contains(installedExtension));
        Assert.assertFalse(installedExtension.isDependency(null));
        Assert.assertTrue(installedExtension.isDependency("namespace"));
    }

    @Test
    public void testInstallRemoteOnNamespaces() throws Throwable
    {
        install(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID, new String[] { "namespace1", "namespace2" });

        LocalExtension installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), "namespace1");
        Assert.assertNotNull(installedExtension);
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRANDCDEPENDENCIES_ID.getId(), "namespace2"));
        Assert.assertTrue(this.handler.getExtensions().get("namespace1").contains(installedExtension));
        Assert.assertTrue(this.handler.getExtensions().get("namespace2").contains(installedExtension));

        installedExtension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), "namespace1");
        Assert.assertNotNull(installedExtension);
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), "namespace2"));
        Assert.assertTrue(this.handler.getExtensions().get("namespace1").contains(installedExtension));
        Assert.assertTrue(this.handler.getExtensions().get("namespace2").contains(installedExtension));
    }

    @Test
    public void testUpgradeFirstOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_UPGRADE10_ID);

        LocalExtension installedExtension;

        // Test upgrade

        install(TestResources.REMOTE_UPGRADE20_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), null);
        Assert.assertNotNull(installedExtension);
        Assert.assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), "namespace"));

        Assert.assertNull(installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID));

        // Test downgrade

        install(TestResources.REMOTE_UPGRADE10_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), null);
        Assert.assertNotNull(installedExtension);
        Assert.assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), "namespace"));
    }

    @Test
    public void testDowngradeFirstOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_UPGRADE20_ID);

        LocalExtension installedExtension;

        // //////////////////
        // Test downgrade

        install(TestResources.REMOTE_UPGRADE10_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), null);
        Assert.assertNotNull(installedExtension);
        Assert.assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), "namespace"));

        // Test upgrade

        install(TestResources.REMOTE_UPGRADE20_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), null);
        Assert.assertNotNull(installedExtension);
        Assert.assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), "namespace"));
    }

    @Test
    public void testReplaceDependencyWithFeature() throws Throwable
    {
        InstalledExtension installedextension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID);

        // installedextensiondependency 1.0
        Assert.assertEquals(Arrays.asList(installedextension), this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
        Assert.assertEquals(Collections.singletonMap(null, Arrays.asList(installedextension)),
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID));

        ExtensionId installedextensiondependency2 = new ExtensionId("overrideinstalledextensiondependency", "2.0");

        // change dependency
        install(installedextensiondependency2);

        // installedextensiondependency 2.0
        Assert.assertEquals(Arrays.asList(installedextension), this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
        // overrideinstalledextensiondependency
        Assert.assertEquals(Arrays.asList(installedextension),
            this.installedExtensionRepository.getBackwardDependencies(installedextensiondependency2.getId(), null));
        Assert.assertEquals(Collections.singletonMap(null, Arrays.asList(installedextension)),
            this.installedExtensionRepository.getBackwardDependencies(installedextensiondependency2));
    }

    @Test
    public void testInstallOnNamespaceThenOnRoot() throws Throwable
    {
        // Install 1.0 on "namespace"

        install(TestResources.REMOTE_UPGRADE10_ID, "namespace");

        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID);

        Assert.assertNotNull(installedExtension);
        Assert.assertNotNull(installedExtension.getNamespaces());
        Assert.assertTrue(this.handler.getExtensions().get("namespace").contains(installedExtension));

        // Install 1.0 on root

        install(TestResources.REMOTE_UPGRADE10_ID);

        installedExtension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), null);
        Assert.assertSame(installedExtension, this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID.getId(), "namespace"));
        Assert.assertNotNull(installedExtension);
        Assert.assertNull("Extension is not marked as installed on root", installedExtension.getNamespaces());
        Assert.assertTrue(this.handler.getExtensions().get(null).contains(installedExtension));
    }

    @Test
    public void testInstallOnNamespaceThenUpgradeOnRoot() throws Throwable
    {
        // Install 1.0 on "namespace"

        install(TestResources.REMOTE_UPGRADE10_ID, "namespace");

        InstalledExtension installedExtension1 =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID);

        Assert.assertNotNull(installedExtension1);

        // Upgrade 2.0 on root

        install(TestResources.REMOTE_UPGRADE20_ID);

        installedExtension1 =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID);

        Assert.assertNull(installedExtension1);

        LocalExtension installedExtension2 =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), null);
        Assert.assertNotNull(installedExtension2);
        Assert.assertTrue(this.handler.getExtensions().get(null).contains(installedExtension2));

        Assert.assertNull(installedExtension2 =
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID));
    }

    @Test
    public void testInstallOnNamespaceThenMoveDependencyOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_WITHRDEPENDENCY_ID, "namespace");

        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRDEPENDENCY_ID.getId(), "namespace"));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), "namespace"));

        install(TestResources.REMOTE_SIMPLE_ID);

        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), "namespace"));
        Assert.assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), null));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRDEPENDENCY_ID.getId(), "namespace"));
        Assert.assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_WITHRDEPENDENCY_ID.getId(), null));
    }

    @Test
    public void testInstallOnNamespaceThenOnRootWithLowerDependency() throws Throwable
    {
        // Install extension on namespace
        install(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID, "namespace");
        // Upgrade dependency
        install(TestResources.REMOTE_OTHERUPGRADEWITHDEPENDENCY20_ID, "namespace");

        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID));
        Assert.assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID));

        // Install extension on root
        install(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID);

        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE10_ID));
        Assert.assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID));

        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), "namespace"));
        Assert.assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_UPGRADE20_ID.getId(), null));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID.getId(), "namespace"));
        Assert.assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID.getId(), null));
    }

    @Test
    public void testInstallRemoteWithMissingDependency() throws Throwable
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
}
