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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.job.plan.ExtensionPlanNode;
import org.xwiki.extension.test.AbstractExtensionHandlerTest;
import org.xwiki.extension.test.MockitoRepositoryUtilsExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@ComponentTest
@ExtendWith(MockitoRepositoryUtilsExtension.class)
public class UpgradePlanJobTest extends AbstractExtensionHandlerTest
{
    @Test
    void testUpgradePlanOnRoot() throws Throwable
    {
        // Install first version
        InstalledExtension extension = install(TestResources.REMOTE_UPGRADE10_ID);
        // Tag it as dependency
        this.installedExtensionRepository.installExtension(extension, null, true);

        // check upgrade

        ExtensionPlan plan = upgradePlan();

        // Tree

        assertEquals(2, plan.getTree().size());

        ExtensionPlanNode node = getNode(TestResources.REMOTE_UPGRADE20_ID, plan.getTree());
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertNull(action.getNamespace());
        assertEquals(0, node.getChildren().size());

        node = getNode(TestResources.INSTALLED_WITHMISSINDEPENDENCY_ID, plan.getTree());
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

        // Actions

        assertEquals(3, plan.getActions().size());

        // //////////////////////
        // Exclude extension

        assertEquals(0,
            upgradePlan(null,
                Arrays.asList(TestResources.REMOTE_UPGRADE10_ID, TestResources.INSTALLED_WITHMISSINDEPENDENCY_ID))
                .getTree().size());
        assertEquals(1, upgradePlan(null, Arrays.asList(TestResources.REMOTE_UPGRADE10_ID)).getTree().size());
        assertEquals(2, upgradePlan(null, Arrays.asList(TestResources.REMOTE_UPGRADE20_ID)).getTree().size());
    }

    @Test
    void testUpgradePlanOnNamespaceWithExtensionOnRoot() throws Throwable
    {
        // install first version
        install(TestResources.REMOTE_UPGRADE10_ID);

        // check upgrade

        ExtensionPlan plan = upgradePlan("namespace");

        // Tree

        assertEquals(0, plan.getTree().size());
    }

    @Test
    void testUpgradePlanWithDependencyOnRoot() throws Throwable
    {
        // install first version
        install(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID);

        // check upgrade

        ExtensionPlan plan = upgradePlan();

        // Tree
        // ////////

        assertEquals(2, plan.getTree().size());

        // First node
        ExtensionPlanNode node = getNode(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID, plan.getTree());
        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertNull(action.getNamespace());

        assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childNode = node.getChildren().iterator().next();

        ExtensionPlanAction childAction = childNode.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, childAction.getExtension().getId());
        assertEquals(Action.UPGRADE, childAction.getAction());
        assertNull(childAction.getNamespace());

        // Second node
        node = getNode(TestResources.INSTALLED_WITHMISSINDEPENDENCY_ID, plan.getTree());
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

        // Actions
        // ////////

        assertEquals(4, plan.getActions().size());
    }

    @Test
    void testUpgradePlanWithDependencyOnNamespace() throws Throwable
    {
        // install first version
        install(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID, "namespace");

        // check upgrade

        ExtensionPlan plan = upgradePlan("namespace");

        // Tree
        // ////////

        assertEquals(1, plan.getTree().size());

        Iterator<ExtensionPlanNode> iterator = plan.getTree().iterator();

        // First node
        ExtensionPlanNode node = iterator.next();

        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADEWITHDEPENDENCY20_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertEquals("namespace", action.getNamespace());

        assertEquals(1, node.getChildren().size());

        ExtensionPlanNode childNode = node.getChildren().iterator().next();

        ExtensionPlanAction childAction = childNode.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, childAction.getExtension().getId());
        assertEquals(Action.UPGRADE, childAction.getAction());
        assertEquals("namespace", childAction.getNamespace());

        // Second node
        assertFalse(iterator.hasNext());

        // Actions
        // ////////

        assertEquals(2, plan.getActions().size());

        Iterator<ExtensionPlanAction> actionIterator = plan.getActions().iterator();

        assertSame(childAction, actionIterator.next());
        assertSame(action, actionIterator.next());
    }

    @Test
    void testUpgradePlanOnRootWithTargetDependencyExtension() throws Throwable
    {
        // install first version
        install(TestResources.REMOTE_UPGRADEWITHDEPENDENCY10_ID);

        // check upgrade

        InstallRequest installRequest = new InstallRequest();
        installRequest.addExtension(TestResources.REMOTE_UPGRADE10_ID);
        ExtensionPlan plan = upgradePlan(installRequest);

        // Tree

        assertEquals(1, plan.getTree().size());

        ExtensionPlanNode node = plan.getTree().iterator().next();

        ExtensionPlanAction action = node.getAction();

        assertEquals(TestResources.REMOTE_UPGRADE20_ID, action.getExtension().getId());
        assertEquals(Action.UPGRADE, action.getAction());
        assertNull(action.getNamespace());

        assertEquals(0, node.getChildren().size());

        // Actions

        assertEquals(1, plan.getActions().size());

        assertSame(action, plan.getActions().iterator().next());
    }
}
