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
package org.xwiki.tool.enforcer;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

/**
 * Performs checks on the version specified for dependencies in pom.xml files. For example if we're in XWiki Rendering
 * and there's a dependency on some XWiki Commons module we might want to ensure that it uses a variable (such as
 * {@code ${commons.version}}) and not {@code ${project.version}}. To achieve this you would use:
 *
 * <pre>
 * <code>
 *   &lt;rules&gt;
 *     &lt;validateDependencyVersion implementation="org.xwiki.tool.enforcer.BannedDependencyType"&gt;
 *       &lt;versionCheck&gt;
 *         &lt;projectPackaging&gt;jar&lt;/projectPackaging&gt;
 *         &lt;dependencyType&gt;xar&lt;/dependencyType&gt;
 *       &lt;/versionCheck&gt;
 *     &lt;/validateDependencyVersion&gt;
 *   &lt;/rules&gt;
 * </code>
 * </pre>
 *
 * @version $Id$
 * @since 14.5RC1
 * @since 14.4.3
 * @since 13.10.8
 */
public class BannedDependencyType extends AbstractPomCheck
{
    /**
     * The packaging of the project to check.
     */
    private String projectPackaging;

    private String dependencyType;

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException
    {
        Model model = getModel(helper);

        if (this.projectPackaging == null || model.getPackaging().equals(projectPackaging)) {
            for (Dependency dependency : model.getDependencies()) {
                if (isRuntime(dependency) && getType(dependency).equals(this.dependencyType)) {
                    StringBuilder builder = new StringBuilder("Found dependency with banned type [");
                    builder.append(this.dependencyType);
                    builder.append("]");
                    if (this.projectPackaging != null) {
                        builder.append(" for a project with packaging [");
                        builder.append(this.projectPackaging);
                        builder.append("]");
                    }
                    builder.append(": ");
                    builder.append(dependency);
                    throw new EnforcerRuleException(builder.toString());
                }
            }
        }
    }

    private boolean isRuntime(Dependency dependency)
    {
        return dependency.getScope() == null || dependency.getScope().equals("runtime");
    }

    private String getType(Dependency dependency)
    {
        return dependency.getType() != null ? dependency.getType() : "jar";
    }
}
