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
package org.xwiki.tool.extension;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.xwiki.extension.job.plan.ExtensionPlan;
import org.xwiki.extension.job.plan.ExtensionPlanAction.Action;
import org.xwiki.extension.job.plan.ExtensionPlanNode;
import org.xwiki.extension.job.plan.ExtensionPlanTree;
import org.xwiki.tool.extension.util.AbstractExtensionMojo;

/**
 * Resolve and print a dependency tree of a module according to XWiki Extension Manager resolution instead of Maven one
 * since they have some differences (for example optional extension dependencies are selected by default by Extension
 * Manager).
 * 
 * @version $Id$
 * @since 15.2RC1
 */
@Mojo(name = "dependency-tree", defaultPhase = LifecyclePhase.VERIFY, requiresProject = true, threadSafe = true)
public class DependencyTreeMojo extends AbstractExtensionMojo
{
    /**
     * The entry point to start from.
     * 
     * @version $Id$
     */
    public enum EntryPoint
    {
        /**
         * The project itself is resolved.
         */
        PROJECT,

        /**
         * The dependencies of the project mixed in the same resolution.
         */
        DEPENDENCIES,

        /**
         * The dependencies of the project each in it's own resolution.
         */
        DEPENDENCIES_ISOLATED,
    }

    @Parameter(defaultValue = "PROJECT", property = "entryPoint")
    private EntryPoint entryPoint;

    @Override
    public void executeInternal() throws MojoExecutionException
    {
        // Resolve the dependency tree
        List<ExtensionPlan> plans;
        if (this.entryPoint == EntryPoint.PROJECT) {
            plans = Collections.singletonList(this.extensionHelper.createInstallPlan(
                this.extensionHelper.toArtifactModel(this.project.getArtifact(), this.project.getModel())));
        } else {
            plans = this.extensionHelper.resolveDependencies(this.project,
                this.entryPoint == EntryPoint.DEPENDENCIES_ISOLATED);
        }

        // Print the dependency tree
        for (ExtensionPlan plan : plans) {
            print(plan.getTree());
        }
    }

    private void print(ExtensionPlanTree tree)
    {
        for (ExtensionPlanNode node : tree) {
            print(node, 0);
        }
    }

    private void print(ExtensionPlanNode node, int level)
    {
        if (node.getAction().getAction() == Action.INSTALL) {
            StringBuilder builder = new StringBuilder();

            if (level > 0) {
                builder.append(StringUtils.repeat("|  ", level));
            }
            builder.append("+- ");
            builder.append(node.getAction().getExtension().getId().getId());
            builder.append(" ");
            builder.append(node.getAction().getExtension().getId().getVersion());

            getLog().info(builder.toString());

            int childLevel = level + 1;
            for (ExtensionPlanNode child : node.getChildren()) {
                print(child, childLevel);
            }
        }
    }
}
