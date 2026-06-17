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
package org.xwiki.extension.jar.internal.handler;

import java.util.Optional;

import javax.inject.Inject;

import jakarta.inject.Singleton;

import org.apache.commons.lang3.Strings;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.extension.ExtensionContext;
import org.xwiki.extension.ExtensionSession;
import org.xwiki.extension.job.internal.AbstractExtensionJob;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;

/**
 * Helper to manipulate the context of JAR extensions.
 * 
 * @version $Id$
 * @since 18.5.0RC1
 * @since 18.4.2
 * @since 17.10.10
 */
@Component(roles = JarExtenssionContext.class)
@Singleton
public class JarExtenssionContext
{
    private static final String SESSION_PROP_RELOAD = "xwiki.extension.jar.reload";

    @Inject
    private ExtensionContext extensionContext;

    @Inject
    private Execution execution;

    /**
     * @param namespace the namespace for which to check if a reload of JAR extensions will be triggered
     * @return true if a reload of JAR extensions will be triggered, false otherwise
     */
    public boolean isReloadRequired(String namespace)
    {
        Optional<ExtensionSession> extensionSession = this.extensionContext.getExtensionSession();

        if (extensionSession.isPresent()) {
            ExtensionSession session = extensionSession.get();

            // Check if we already know if a reload of JAR extensions will be triggered
            Boolean isReloadRequired = session.get(SESSION_PROP_RELOAD);

            if (isReloadRequired == null) {
                // Return false when the current action is isolated
                isReloadRequired = false;

                ExecutionContext econtext = this.execution.getContext();
                ExtensionPlan plan = (ExtensionPlan) econtext.getProperty(AbstractExtensionJob.CONTEXTKEY_PLAN);

                if (plan != null) {
                    isReloadRequired = isReloadRequired(namespace, plan);
                }

                // Remember the result of the evaluation
                session.set(SESSION_PROP_RELOAD, isReloadRequired);
            }

            return isReloadRequired;
        }

        return false;
    }

    private boolean isReloadRequired(String namespace, ExtensionPlan plan)
    {
        if (plan != null) {
            // Check if there is any JAR upgrade or uninstall planned for the namespace, which means that a
            // reload of JAR extensions will be triggered.
            for (ExtensionPlanAction action : plan.getActions()) {
                if (isReloadRequired(action, namespace)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isReloadRequired(ExtensionPlanAction action, String namespace)
    {
        return isReloadRequired(action.getAction()) && JarExtensionHandler.isSupported(action.getExtension().getType())
            && (action.getNamespace() == null || Strings.CS.equals(namespace, action.getNamespace()));
    }

    private boolean isReloadRequired(Action action)
    {
        return action == Action.UPGRADE || action == Action.UNINSTALL;
    }
}
