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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.ExtensionFeature;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.job.plan.ExtensionPlanNode;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.test.AbstractExtensionHandlerTest;
import org.xwiki.extension.test.ConfigurableDefaultCoreExtensionRepository;
import org.xwiki.extension.version.internal.DefaultVersion;

public class InstallPlanJobTest extends AbstractExtensionHandlerTest
{
    private ConfigurableDefaultCoreExtensionRepository coreRepository;

    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.coreRepository = this.mocker.getInstance(CoreExtensionRepository.class);
    }

    @Test
    public void testInstallPlanWithSimpleRemoteExtensionOnRoot() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_SIMPLE_ID);

        // Tree

        Assert.assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        Assert.assertEquals(TestResources.REMOTE_SIMPLE_ID, action.getExtension().getId());
        Assert.assertEquals(Action.INSTALL, node.getAction().getAction());
        Assert.assertNull(action.getPreviousExtension());
        Assert.assertNull(action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());

        // Actions

        Assert.assertEquals(1, plan.getActions().size());

        Assert.assertSame(action, plan.getActions().iterator().next());
    }

    @Test
    public void testInstallPlanWithRemoteDependencyOnRoot() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITHRDEPENDENCY_ID);

        Assert.assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        Assert.assertEquals(TestResources.REMOTE_WITHRDEPENDENCY_ID, node.getAction().getExtension().getId());
        Assert.assertEquals(Action.INSTALL, node.getAction().getAction());
        Assert.assertNull(node.getAction().getPreviousExtension());
        Assert.assertNull(node.getAction().getNamespace());
        Assert.assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childnode = node.getChildren().iterator().next();

        Assert.assertEquals(TestResources.REMOTE_SIMPLE_ID, childnode.getAction().getExtension().getId());
        Assert.assertEquals(Action.INSTALL, node.getAction().getAction());
        Assert.assertNull(node.getAction().getPreviousExtension());
        Assert.assertNull(childnode.getAction().getNamespace());
        Assert.assertTrue(childnode.getChildren().isEmpty());
    }

    @Test
    public void testInstallPlanWithCoreDependencyOnRoot() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITHCDEPENDENCY_ID);

        Assert.assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        Assert.assertEquals(TestResources.REMOTE_WITHCDEPENDENCY_ID, node.getAction().getExtension().getId());
        Assert.assertEquals(Action.INSTALL, node.getAction().getAction());
        Assert.assertNull(node.getAction().getPreviousExtension());
        Assert.assertNull(node.getAction().getNamespace());
        Assert.assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childnode = node.getChildren().iterator().next();

        Assert.assertTrue(childnode.getAction().getExtension() instanceof CoreExtension);
        Assert.assertEquals(TestResources.CORE_ID, childnode.getAction().getExtension().getId());
        Assert.assertEquals(Action.NONE, childnode.getAction().getAction());
        Assert.assertNull(node.getAction().getPreviousExtension());
        Assert.assertTrue(childnode.getChildren().isEmpty());
    }

    @Test
    public void testInstallPlanWithInstalledDependencyOnRoot() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_WITHLDEPENDENCY_ID);

        Assert.assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        Assert.assertEquals(TestResources.REMOTE_WITHLDEPENDENCY_ID, node.getAction().getExtension().getId());
        Assert.assertEquals(Action.INSTALL, node.getAction().getAction());
        Assert.assertNull(node.getAction().getPreviousExtension());
        Assert.assertNull(node.getAction().getNamespace());
        Assert.assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childnode = node.getChildren().iterator().next();

        Assert.assertTrue(childnode.getAction().getExtension() instanceof LocalExtension);
        Assert.assertEquals(TestResources.INSTALLED_ID, childnode.getAction().getExtension().getId());
        Assert.assertEquals(Action.NONE, childnode.getAction().getAction());
        Assert.assertNull(node.getAction().getPreviousExtension());
        Assert.assertTrue(childnode.getChildren().isEmpty());
    }

    @Test
    public void testInstallPlanWithUpgradeOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_UPGRADE10_ID);

        // //////////////////
        // Test upgrade

        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADE20_ID);

        Assert.assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        Assert.assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        Assert.assertEquals(Action.UPGRADE, action.getAction());
        Assert.assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getPreviousExtension().getId());
        Assert.assertNull(action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallPlanWithUpgradeOnDifferentId() throws Throwable
    {
        ExtensionPlan plan = installPlan(TestResources.REMOTE_NOTINSTALLED_ID);

        Iterator<ExtensionPlanNode> it = plan.getTree().iterator();

        ExtensionPlanNode node = it.next();
        ExtensionPlanAction action = node.getAction();
        Assert.assertEquals(TestResources.INSTALLED_ID, action.getExtension().getId());
        Assert.assertEquals(Action.UNINSTALL, action.getAction());
        Assert.assertNull(action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());

        node = it.next();
        action = node.getAction();
        Assert.assertEquals(TestResources.INSTALLED_DEPENDENCY_ID, action.getExtension().getId());
        Assert.assertEquals(Action.UNINSTALL, action.getAction());
        Assert.assertNull(action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());

        node = it.next();
        action = node.getAction();
        Assert.assertEquals(TestResources.REMOTE_NOTINSTALLED_ID, action.getExtension().getId());
        Assert.assertEquals(Action.UPGRADE, action.getAction());
        Assert.assertEquals(TestResources.INSTALLED_ID, action.getPreviousExtension().getId());
        Assert.assertNull(action.getNamespace());
        Assert.assertEquals(1, node.getChildren().size());

        node = node.getChildren().iterator().next();
        action = node.getAction();
        Assert.assertEquals(TestResources.REMOTE_NOTINSTALLED_DEPENDENCY_ID, action.getExtension().getId());
        Assert.assertEquals(Action.UPGRADE, action.getAction());
        Assert.assertEquals(TestResources.INSTALLED_DEPENDENCY_ID, action.getPreviousExtension().getId());
        Assert.assertNull(action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallPlanWithDowngradeOnRoot() throws Throwable
    {
        install(TestResources.REMOTE_UPGRADE20_ID);

        // //////////////////
        // Test downgrade

        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADE10_ID);

        Assert.assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        Assert.assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        Assert.assertEquals(Action.DOWNGRADE, action.getAction());
        Assert.assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getPreviousExtension().getId());
        Assert.assertNull(action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallOnNamespaceThenOnRoot() throws Throwable
    {
        // Install 1.0 on namespace
        install(TestResources.REMOTE_UPGRADE10_ID, "namespace");

        // Move 1.0 on root
        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADE10_ID);

        Assert.assertEquals(2, plan.getTree().size());

        Iterator<ExtensionPlanNode> it = plan.getTree().iterator();
        
        ExtensionPlanNode node = it.next();
        ExtensionPlanAction action = node.getAction();
        Assert.assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        Assert.assertEquals(Action.UNINSTALL, action.getAction());
        Assert.assertEquals(1, action.getPreviousExtensions().size());
        Assert.assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getPreviousExtension().getId());
        Assert.assertEquals("namespace", action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());
        
        node = it.next();
        action = node.getAction();
        Assert.assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        Assert.assertEquals(Action.INSTALL, action.getAction());
        Assert.assertEquals(0, action.getPreviousExtensions().size());
        Assert.assertEquals(null, action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());
    }

    @Test
    public void testInstallOnNamespaceThenUnpgradeOnRoot() throws Throwable
    {
        // Install 1.0 on namespace
        install(TestResources.REMOTE_UPGRADE10_ID, "namespace");

        // Upgrade 2.0 on namespace
        ExtensionPlan plan = installPlan(TestResources.REMOTE_UPGRADE20_ID);

        Assert.assertEquals(2, plan.getTree().size());

        Iterator<ExtensionPlanNode> it = plan.getTree().iterator();
        
        ExtensionPlanNode node = it.next();
        ExtensionPlanAction action = node.getAction();
        Assert.assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getExtension().getId());
        Assert.assertEquals(Action.UNINSTALL, action.getAction());
        Assert.assertEquals(1, action.getPreviousExtensions().size());
        Assert.assertEquals(TestResources.REMOTE_UPGRADE10_ID, action.getPreviousExtension().getId());
        Assert.assertEquals("namespace", action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());
        
        node = it.next();
        action = node.getAction();
        Assert.assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        Assert.assertEquals(Action.INSTALL, action.getAction());
        Assert.assertEquals(0, action.getPreviousExtensions().size());
        Assert.assertEquals(null, action.getNamespace());
        Assert.assertEquals(0, node.getChildren().size());
    }
    
    // Errors

    @Test(expected = InstallException.class)
    public void testInstallPlanWithUnsupportedType() throws Throwable
    {
        installPlan(TestResources.REMOTE_UNSUPPORTED_ID);
    }

    @Test(expected = InstallException.class)
    public void testInstallPlanWithCoreExtension() throws Throwable
    {
        this.coreRepository.addExtensions(TestResources.REMOTE_SIMPLE_ID.getId(), new DefaultVersion("version"));

        installPlan(TestResources.REMOTE_SIMPLE_ID);
    }

    @Test(expected = InstallException.class)
    public void testInstallPlanWithFeatureAsCoreExtension() throws Throwable
    {
        this.coreRepository.addExtensions("rsimple-feature", new DefaultVersion("version"));

        installPlan(TestResources.REMOTE_SIMPLE_ID);
    }

    @Test(expected = InstallException.class)
    public void testInstallPlanWithFeatureAsCoreExtensionFeature() throws Throwable
    {
        this.coreRepository.addExtensions("coreextension", new DefaultVersion("version"),
            Arrays.asList(new ExtensionFeature("rsimple-feature")));

        installPlan(TestResources.REMOTE_SIMPLE_ID);
    }

    @Test(expected = InstallException.class)
    public void testInstallPlanWithCoreExtensionFeature() throws Throwable
    {
        this.coreRepository.addExtensions("coreextension", new DefaultVersion("version"),
            Arrays.asList(new ExtensionFeature(TestResources.REMOTE_SIMPLE_ID.getId())));

        installPlan(TestResources.REMOTE_SIMPLE_ID);
    }

    @Test(expected = InstallException.class)
    public void testInstallLowerVersionOfDependencyOnRoot() throws Throwable
    {
        // Install extension 2.0 on namespace
        install(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID, "namespace");
        // Install dependendy 1.0 on root
        installPlan(TestResources.REMOTE_UPGRADE10_ID);
    }

    @Test(expected = InstallException.class)
    public void testInstallNameSpaceExtensionWithExistingRootExtension() throws Throwable
    {
        // Install 1.0 on root
        install(TestResources.REMOTE_UPGRADE10_ID);
        // Try to upgrade 2.0 on namespace
        installPlan(TestResources.REMOTE_UPGRADE20_ID, "namespace");
    }

    @Test(expected = InstallException.class)
    public void testInstallNameSpaceExtensionWithIncompatibleRootDependency() throws Throwable
    {
        // Install 1.0 on root
        install(TestResources.REMOTE_UPGRADE10_ID);
        // Install extension 2.0 on namespace
        installPlan(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID, "namespace");
    }
}
