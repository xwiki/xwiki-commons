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
 * Performs checks on the type specified for dependencies in pom.xml files. For example in XWiki Standard we want to
 * prevent extension with package {@code jar} and {@code webjar} to depend on {@code xar} extensions but only if the
 * module does not ends with -test-docker or -test-tests. To achieve this you would use:
 *
 * <pre>
 * <code>
 *   &lt;rules&gt;
 *     &lt;validateDependencyVersion implementation="org.xwiki.tool.enforcer.BannedDependencyType"&gt;
 *       &lt;projectPackaging&gt;jar&lt;/projectPackaging&gt;
 *       &lt;dependencyType&gt;xar&lt;/dependencyType&gt;
 *     &lt;/validateDependencyVersion&gt;
 *     &lt;validateDependencyVersion implementation="org.xwiki.tool.enforcer.BannedDependencyType"&gt;
 *       &lt;projectPackaging&gt;webjar&lt;/projectPackaging&gt;
 *       &lt;dependencyType&gt;xar&lt;/dependencyType&gt;
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
    private static final String JAR = "jar";

    /**
     * The group id pattern of the project to check.
     */
    private String projectGroupId;

    /**
     * The artifact id pattern of the project to check.
     */
    private String projectArtifactId;

    /**
     * The packaging of the project to check.
     */
    private String projectPackaging;

    private String dependencyType;

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException
    {
        Model model = getModel(helper);

        // Check the packaging
        if (skipProjectPackaging(model)) {
            helper.getLog().info("Skipping as the packaging does not match [" + this.projectPackaging + "]");

            return;
        }

        // Check the group id
        if (skipProjectGroupId(model)) {
            helper.getLog().info("Skipping as the group id does not match [" + this.projectGroupId + "]");

            return;
        }

        // Check the artifact id
        if (skiphProjectArtifactId(model)) {
            helper.getLog().info("Skipping as the artifact id does not match [" + this.projectArtifactId + "]");

            return;
        }

        for (Dependency dependency : model.getDependencies()) {
            if (isRuntime(dependency) && getType(dependency).equals(this.dependencyType)) {
                throw new EnforcerRuleException(
                    "Found dependency with banned type [" + this.dependencyType + "]: " + dependency);
            }
        }
    }

    private boolean skipPattern(String value, String pattern)
    {
        return pattern != null && !value.matches(pattern);
    }

    private boolean skipProjectPackaging(Model model)
    {
        return skipPattern(getPackaging(model), this.projectPackaging);
    }

    private boolean skipProjectGroupId(Model model)
    {
        return skipPattern(model.getGroupId(), this.projectGroupId);
    }

    private boolean skiphProjectArtifactId(Model model)
    {
        return skipPattern(model.getArtifactId(), this.projectArtifactId);
    }

    private boolean isRuntime(Dependency dependency)
    {
        return dependency.getScope() == null || dependency.getScope().equals("runtime")
            || dependency.getScope().equals("build");
    }

    private String getPackaging(Model model)
    {
        return model.getPackaging() != null ? model.getPackaging() : JAR;
    }

    private String getType(Dependency dependency)
    {
        return dependency.getType() != null ? dependency.getType() : JAR;
    }
}
