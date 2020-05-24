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
package org.xwiki.extension.job.plan.internal;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.AbstractExtensionTest;
import org.xwiki.extension.AbstractExtensionTest.TestExtension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Validate {@link DefaultExtensionPlanAction}.
 *
 * @version $Id$
 */
public class DefaultExtensionPlanActionTest
{
    @Test
    void equals()
    {
        TestExtension testExtension1 = new TestExtension(new ExtensionId("id"), "type");
        TestExtension testExtension2 = new TestExtension(new ExtensionId("id2"), "type");

        assertEquals(
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.INSTALL, "namespace", true),
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.INSTALL, "namespace", true));
        assertEquals(
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.INSTALL, "namespace", true),
            new DefaultExtensionPlanAction(testExtension1, testExtension1, null, Action.INSTALL, "namespace", true));
        assertEquals(
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.INSTALL, "namespace", true),
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.UNINSTALL, "namespace", true));
        assertEquals(
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.INSTALL, "namespace", true),
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.INSTALL, "namespace", false));

        assertNotEquals(
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.INSTALL, "namespace", true),
            new DefaultExtensionPlanAction(testExtension2, testExtension2, null, Action.INSTALL, "namespace", true));
        assertNotEquals(
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.INSTALL, "namespace", true),
            new DefaultExtensionPlanAction(testExtension1, testExtension2, null, Action.INSTALL, "namespace2", true));
    }
}
