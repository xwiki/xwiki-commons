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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;

/**
 * Performs checks on the version specified for dependencies in pom.xml files.
 *
 * For example if we're in XWiki Rendering and there's a dependency on some XWiki Commons module we might want to
 * ensure that it uses a variable (such as {@code ${commons.version}}) and not {@code ${project.version}}. To achieve
 * this you would use:
 *
 * <pre><code>
 *   &lt;rules&gt;
 *     &lt;validateDependencyVersion implementation="org.xwiki.tool.enforcer.ValidateDependencyVersion"&gt;
 *       &lt;versionCheck&gt;
 *         &lt;groupIdPrefix&gt;org.xwiki.commons&lt;/groupIdPrefix&gt;
 *         &lt;allowedVersionRegex&gt;\$\{project.version\}|[^$].*&lt;/allowedVersionRege&gt;
 *       &lt;/versionCheck&gt;
 *     &lt;/validateDependencyVersion&gt;
 *   &lt;/rules&gt;
 * </code></pre>
 *
 * @version $Id$
 * @since 4.5RC1
 */
public class ValidateDependencyVersion extends AbstractPomCheck
{
    /**
     * List of checks to execute; they are configured in the pom.xml.
     */
    private List<VersionCheck> checks = new ArrayList<VersionCheck>();

    /**
     * Add a new dependency version check. Called automatically by the Maven framework when the following construct
     * is defined in a pom.xml file:
     *
     * <pre><code>
     *   &lt;versionCheck&gt;
     *     &lt;groupIdPrefix&gt;org.xwiki.commons&lt;/groupIdPrefix&gt;
     *     &lt;allowedVersionRegex&gt;.*&lt;/allowedVersionRegex&gt;
     *   &lt;/versionCheck&gt;
     * </code></pre>
     *
     * @param versionCheck the check to add
     */
    public void addVersionCheck(VersionCheck versionCheck)
    {
        this.checks.add(versionCheck);
    }

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException
    {
        Model model = getModel(helper);

        validateDependencies(model.getDependencies());

        DependencyManagement dependencyManagement = model.getDependencyManagement();
        if (dependencyManagement != null) {
            validateDependencies(dependencyManagement.getDependencies());
        }
    }

    /**
     * @param dependencies the list of dependencies to validate
     * @throws EnforcerRuleException if a dependency doesn't validate
     */
    private void validateDependencies(List dependencies) throws EnforcerRuleException
    {
        for (Object object : dependencies) {
            Dependency dependency = (Dependency) object;
            for (VersionCheck versionCheck : this.checks) {
                // Note: the version will be null if defined in a parent.
                if (dependency.getVersion() != null
                    && dependency.getGroupId().startsWith(versionCheck.getGroupIdPrefix()))
                {
                    Pattern pattern = Pattern.compile(versionCheck.getAllowedVersionRegex());
                    Matcher matcher = pattern.matcher(dependency.getVersion());
                    if (!matcher.matches()) {
                        throw new EnforcerRuleException("Was expecting a dependency version matching ["
                            + versionCheck.getAllowedVersionRegex() + "] but got instead ["
                            + dependency.getVersion() + "] for " + dependency);
                    }
                }
            }
        }
    }
}
