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
import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.job.plan.ExtensionPlanNode;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.test.AbstractExtensionHandlerTest;
import org.xwiki.extension.test.ConfigurableDefaultCoreExtensionRepository;
import org.xwiki.extension.test.MockitoRepositoryUtilsExtension;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentTest
@ExtendWith(MockitoRepositoryUtilsExtension.class)
public class InstallPlanJobTest extends AbstractExtensionHandlerTest
{
    private ConfigurableDefaultCoreExtensionRepository coreRepository;

    @BeforeEach
    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.coreRepository = this.componentManager.getInstance(CoreExtensionRepository.class);
    }

    @Test
    public void testInstallPlanWithSimpleRemoteExtensionOnRoot() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_SIMPLE_ID);

        // Tree

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_SIMPLE_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, node.getAction().getAction());
        assertNull(action.getPreviousExtension());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());

        // Actions

        assertEquals(1, plan.getActions().size());

        assertSame(action, plan.getActions().iterator().next());
    }

    @Test
    public void testInstallPlanWithRemoteDependencyOnRoot() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITHRDEPENDENCY_ID);

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        assertEquals(TestResources.REMOTE_WITHRDEPENDENCY_ID, node.getAction().getExtension().getId());
        assertEquals(Action.INSTALL, node.getAction().getAction());
        assertNull(node.getAction().getPreviousExtension());
        assertNull(node.getAction().getNamespace());
        assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childnode = node.getChildren().iterator().next();

        assertEquals(TestResources.REMOTE_SIMPLE_ID, childnode.getAction().getExtension().getId());
        assertEquals(Action.INSTALL, node.getAction().getAction());
        assertNull(node.getAction().getPreviousExtension());
        assertNull(childnode.getAction().getNamespace());
        assertTrue(childnode.getChildren().isEmpty());
    }

    @Test
    public void testInstallPlanWithCoreDependencyOnRoot() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITHCDEPENDENCY_ID);

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        assertEquals(TestResources.REMOTE_WITHCDEPENDENCY_ID, node.getAction().getExtension().getId());
        assertEquals(Action.INSTALL, node.getAction().getAction());
        assertNull(node.getAction().getPreviousExtension());
        assertNull(node.getAction().getNamespace());
        assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childnode = node.getChildren().iterator().next();

        assertTrue(childnode.getAction().getExtension() instanceof CoreExtension);
        assertEquals(TestResources.CORE_ID, childnode.getAction().getExtension().getId());
        assertEquals(Action.NONE, childnode.getAction().getAction());
        assertNull(node.getAction().getPreviousExtension());
        assertTrue(childnode.getChildren().isEmpty());
    }

    @Test
    public void testInstallPlanWithInstalledDependencyOnRoot() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITHLDEPENDENCY_ID);

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        assertEquals(TestResources.REMOTE_WITHLDEPENDENCY_ID, node.getAction().getExtension().getId());
        assertEquals(Action.INSTALL, node.getAction().getAction());
        assertNull(node.getAction().getPreviousExtension());
        assertNull(node.getAction().getNamespace());
        assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childnode = node.getChildren().iterator().next();

        assertTrue(childnode.getAction().getExtension() instanceof LocalExtension);
        assertEquals(TestResources.INSTALLED_ID, childnode.getAction().getExtension().getId());
        assertEquals(Action.NONE, childnode.getAction().getAction());
        assertNull(node.getAction().getPreviousExtension());
        assertTrue(childnode.getChildren().isEmpty());
    }

    @Test
    public void testInstallPlanWithUpgradeOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_UPGRADE10_ID);

        // //////////////////
        // Test upgrade

        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADE20_ID);

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getPreviousExtension().getId());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallPlanWithUpgradeOnDifferentId() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_NOTINSTALLED_ID);

        Iterator<ExtensionPlanNode> it = plan.getTree().iterator();

        ExtensionPlanNode node = it.next();
        ExtensionPlanAction action = node.getAction();
        assertEquals(TestResources.INSTALLED_ID, action.getExtension().getId());
        assertEquals(Action.UNINSTALL, action.getAction());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());

        node = it.next();
        action = node.getAction();
        assertEquals(TestResources.INSTALLED_DEPENDENCY_ID, action.getExtension().getId());
        assertEquals(Action.UNINSTALL, action.getAction());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());

        node = it.next();
        action = node.getAction();
        assertEquals(TestResources.REMOTE_NOTINSTALLED_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertEquals(TestResources.INSTALLED_ID, action.getPreviousExtension().getId());
        assertNull(action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();
        assertEquals(TestResources.REMOTE_NOTINSTALLED_DEPENDENCY_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertEquals(TestResources.INSTALLED_DEPENDENCY_ID, action.getPreviousExtension().getId());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallPlanWithUpgradeFeatureWithDifferentVersion() throws Throwable
    {
        install(TestResources.REMOTE_UPGRADEFEATURE20_ID);

        // //////////////////
        // Test upgrade

        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADEWITHFEATURE10_ID);

        assertEquals(2, plan.getTree().size());

        Iterator<ExtensionPlanNode> it = plan.getTree().iterator();

        // upgrade-feature

        ExtensionPlanNode node1 = it.next();

        ExtensionPlanAction action1 = node1.getAction();

        assertEquals(TestResources.REMOTE_UPGRADEFEATURE20_ID, action1.getExtension().getId());
        assertEquals(Action.UNINSTALL, action1.getAction());
        assertEquals(TestResources.REMOTE_UPGRADEFEATURE20_ID, action1.getPreviousExtension().getId());
        assertNull(action1.getNamespace());
        assertEquals(0, node1.getChildren().size());

        // upgrade-withfeature

        ExtensionPlanNode node2 = it.next();

        ExtensionPlanAction action2 = node2.getAction();

        assertEquals(TestResources.REMOTE_UPGRADEWITHFEATURE10_ID, action2.getExtension().getId());
        assertEquals(Action.UPGRADE, action2.getAction());
        assertEquals(TestResources.REMOTE_UPGRADEFEATURE20_ID, action2.getPreviousExtension().getId());
        assertNull(action2.getNamespace());
        assertEquals(0, node2.getChildren().size());
    }

    @Test
    public void testInstallPlanWithDowngradeOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_UPGRADE20_ID);

        // //////////////////
        // Test downgrade

        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADE10_ID);

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        assertEquals(Action.DOWNGRADE, action.getAction());
        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getPreviousExtension().getId());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallOnNamespaceThenOnRoot() throws Throwable
    {
        // Install 1.0 on namespace
        install(TestResources.REMOTE_UPGRADE10_ID, "namespace");

        // Move 1.0 on root
        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADE10_ID);

        assertEquals(2, plan.getTree().size());

        Iterator<ExtensionPlanNode> it = plan.getTree().iterator();

        ExtensionPlanNode node = it.next();
        ExtensionPlanAction action = node.getAction();
        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        assertEquals(Action.UNINSTALL, action.getAction());
        assertEquals(1, action.getPreviousExtensions().size());
        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getPreviousExtension().getId());
        assertEquals("namespace", action.getNamespace());
        assertEquals(0, node.getChildren().size());

        node = it.next();
        action = node.getAction();
        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallOnNamespaceThenUnpgradeOnRoot() throws Throwable
    {
        // Install 1.0 on namespace
        install(TestResources.REMOTE_UPGRADE10_ID, "namespace");

        // Upgrade 2.0 on namespace
        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADE20_ID);

        assertEquals(2, plan.getTree().size());

        Iterator<ExtensionPlanNode> it = plan.getTree().iterator();

        ExtensionPlanNode node = it.next();
        ExtensionPlanAction action = node.getAction();
        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        assertEquals(Action.UNINSTALL, action.getAction());
        assertEquals(1, action.getPreviousExtensions().size());
        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getPreviousExtension().getId());
        assertEquals("namespace", action.getNamespace());
        assertEquals(0, node.getChildren().size());

        node = it.next();
        action = node.getAction();
        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallWithTwoDependenciesFeaturesInTheSameRequest() throws Throwable
    {
        ExtensionPlan plan = installPlan(
            Arrays.asList(TestResources.REMOTE_WITHRDEPENDENCY_ID, TestResources.REMOTE_WITHRDEPENDENCYFEATURE_ID));

        assertEquals(2, plan.getTree().size());

        Iterator<ExtensionPlanNode> it = plan.getTree().iterator();

        ExtensionPlanNode node = it.next();
        ExtensionPlanAction action = node.getAction();
        assertEquals(TestResources.REMOTE_WITHRDEPENDENCY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childnode = node.getChildren().iterator().next();
        action = childnode.getAction();
        assertEquals(TestResources.REMOTE_SIMPLE_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(0, childnode.getChildren().size());

        node = it.next();
        action = node.getAction();
        assertEquals(TestResources.REMOTE_WITHRDEPENDENCYFEATURE_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(1, node.getChildren().size());

        childnode = node.getChildren().iterator().next();
        action = childnode.getAction();
        assertEquals(TestResources.REMOTE_SIMPLE_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(0, childnode.getChildren().size());
    }

    // Errors

    @Test
    public void testInstallPlanWithUnsupportedType()
    {
        assertThrows(InstallException.class, () -> {
            installPlan(TestResources.REMOTE_UNSUPPORTED_ID);
        });
    }

    @Test
    public void testInstallPlanWithCoreExtension()
    {
        assertThrows(InstallException.class, () -> {
            this.coreRepository.addExtensions(TestResources.REMOTE_SIMPLE_ID.getId(), new DefaultVersion("version"));

            installPlan(TestResources.REMOTE_SIMPLE_ID);
        });
    }

    @Test
    public void testInstallPlanWithFeatureAsCoreExtension()
    {
        assertThrows(InstallException.class, () -> {
            this.coreRepository.addExtensions("rsimple-feature", new DefaultVersion("version"));

            installPlan(TestResources.REMOTE_SIMPLE_ID);
        });
    }

    @Test
    public void testInstallPlanWithFeatureAsCoreExtensionFeature()
    {
        assertThrows(InstallException.class, () -> {
            this.coreRepository.addExtensions("coreextension", new DefaultVersion("version"),
                new ExtensionId("rsimple-feature"));

            installPlan(TestResources.REMOTE_SIMPLE_ID);
        });
    }

    @Test
    public void testInstallPlanWithCoreExtensionFeature()
    {
        assertThrows(InstallException.class, () -> {
            this.coreRepository.addExtensions("coreextension", new DefaultVersion("version"),
                new ExtensionId(TestResources.REMOTE_SIMPLE_ID.getId()));

            installPlan(TestResources.REMOTE_SIMPLE_ID);
        });
    }

    @Test
    public void testInstallPlanWithLowerCoreDependencyFeature()
    {
        assertThrows(InstallException.class, () -> {
            this.coreRepository.addExtensions("coreextension", new DefaultVersion("3.0"),
                TestResources.REMOTE_UPGRADE10_ID);

            installPlan(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID);
        });
    }

    @Test
    public void testInstallPlanWithHigherCoreDependencyFeature() throws Throwable
    {
        this.coreRepository.addExtensions("coreextension", new DefaultVersion("0.5"),
            TestResources.REMOTE_UPGRADE20_ID);

        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID);

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childnode = node.getChildren().iterator().next();

        assertTrue(childnode.getAction().getExtension() instanceof CoreExtension);
        assertEquals(new ExtensionId("coreextension", "0.5"), childnode.getAction().getExtension().getId());
        assertEquals(Action.NONE, childnode.getAction().getAction());
        assertNull(node.getAction().getPreviousExtension());
        assertTrue(childnode.getChildren().isEmpty());
    }

    @Test
    public void testReInstalledWithMissingDependency() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.INSTALLED_WITHMISSINDEPENDENCY_ID);

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.INSTALLED_WITHMISSINDEPENDENCY_ID, action.getExtension().getId());
        assertEquals(Action.REPAIR, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_MISSINGDEPENDENCY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallRemoteWithMissingDependency() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITHRMISSINGDEPENDENCY_ID);

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITHRMISSINGDEPENDENCY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.INSTALLED_WITHMISSINDEPENDENCY_ID, action.getExtension().getId());
        assertEquals(Action.REPAIR, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_MISSINGDEPENDENCY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallNameSpaceExtensionWithExistingRootExtension() throws Throwable
    {
        // Install 1.0 on root
        install(TestResources.REMOTE_UPGRADE10_ID);
        // Try to upgrade 2.0 on namespace
        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADE20_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getPreviousExtension().getId());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallNameSpaceExtensionWithDependencyAllowedOnRootOnly() throws Throwable
    {
        // Try to install remote extension with only root allowed dependency on namespace
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITH_ROOT_DEPENDENY10_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITH_ROOT_DEPENDENY10_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_ROOTEXTENSION10_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testUpgradeExtensionOnNamespaceWithDependencyAllowedOnRootOnly() throws Throwable
    {
        // Instance version 1.0
        install(TestResources.REMOTE_WITH_ROOT_DEPENDENY10_ID, "namespace");

        // Try to upgrade remote extension with only root allowed dependency on namespace
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITH_ROOT_DEPENDENY20_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITH_ROOT_DEPENDENY20_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertEquals(TestResources.REMOTE_WITH_ROOT_DEPENDENY10_ID, action.getPreviousExtension().getId());
        assertEquals("namespace", action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_ROOTEXTENSION20_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertEquals(TestResources.REMOTE_ROOTEXTENSION10_ID, action.getPreviousExtension().getId());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallWithOverwrittenManagedDependency() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITH_OVERWRITTEN_MANAGED_DEPENDENY_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallWithManagedTransitiveDependency() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITH_MANAGED_TRANSITIVEDEPENDENY_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallWithManagedDependency() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallWithRecommendedManagedDependency() throws Throwable
    {
        // Change the version of the dependency trough recommended version
        this.coreRepository.getConfigurableEnvironmentExtension().putProperty("xwiki.extension.recommendedVersions",
            "upgrade/2.0");

        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallWithStrictRecommendedManagedDependency() throws Throwable
    {
        // Change the version of the dependency trough recommended version
        this.coreRepository.getConfigurableEnvironmentExtension().putProperty("xwiki.extension.recommendedVersions",
            "upgrade/[2.0]");

        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallWithInvalidRecommendedManagedDependency() throws Throwable
    {
        // Change the version of the dependency trough recommended version
        this.coreRepository.getConfigurableEnvironmentExtension().putProperty("xwiki.extension.recommendedVersions",
            "upgrade/10.0");

        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITH_MANAGED_DEPENDENY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallWithInvalidOptionalDependency() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITHRMISSINGOPTIONALDEPENDENCY_ID, "namespace");

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_WITHRMISSINGOPTIONALDEPENDENCY_ID, action.getExtension().getId());
        assertEquals(Action.INSTALL, action.getAction());
        assertEquals(0, action.getPreviousExtensions().size());
        assertEquals("namespace", action.getNamespace());
        assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallPlanWithCrossDependencies() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_CROSSDEPENDENCY1);

        // Tree

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_CROSSDEPENDENCY1, action.getExtension().getId());
        assertEquals(Action.INSTALL, node.getAction().getAction());
        assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childnode = node.getChildren().iterator().next();

        assertEquals(TestResources.REMOTE_CROSSDEPENDENCY2, childnode.getAction().getExtension().getId());
        assertEquals(Action.INSTALL, node.getAction().getAction());
    }

    // Failures

    @Test
    public void testForbiddenInstallLowerVersionOfDependencyOnRoot()
    {
        assertThrows(InstallException.class, () -> {
            // Install extension 2.0 on namespace
            install(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID, "namespace");
            // Install dependency 1.0 on root
            installPlan(TestResources.REMOTE_UPGRADE10_ID, false);
        });
    }

    @Test
    public void testForbiddenInstallNameSpaceExtensionWithExistingRootExtension()
    {
        assertThrows(InstallException.class, () -> {
            // Install 1.0 on root
            install(TestResources.REMOTE_UPGRADE10_ID);
            // Try to upgrade 2.0 on namespace
            installPlan(TestResources.REMOTE_UPGRADE20_ID, "namespace", false);
        });
    }

    @Test
    public void testForbiddenInstallNameSpaceExtensionWithIncompatibleRootDependency()
    {
        assertThrows(InstallException.class, () -> {
            // Install 1.0 on root
            install(TestResources.REMOTE_UPGRADE10_ID);
            // Install extension 2.0 on namespace
            installPlan(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID, "namespace", false);
        });
    }

    @Test
    public void testForbiddenInstallExtensionOnIncompatibleNamespace() throws Throwable
    {
        // Install 1.0 on root
        install(TestResources.REMOTE_ROOTEXTENSION10_ID);
        uninstall(TestResources.REMOTE_ROOTEXTENSION10_ID);

        // Install 1.0 on incompatible namespace
        assertThrows(InstallException.class, () -> {
            install(TestResources.REMOTE_ROOTEXTENSION10_ID, "namespace", false);
        });
    }

    @Test
    public void testInstallPlanWithUpgradeOnDifferentIdNotAllowed()
    {
        assertThrows(InstallException.class, () -> {
            InstallRequest installRequest = createInstallRequest(TestResources.REMOTE_NOTINSTALLED_ID);
            installRequest.setUninstallAllowed(false);

            installPlan(installRequest);
        });
    }
}
