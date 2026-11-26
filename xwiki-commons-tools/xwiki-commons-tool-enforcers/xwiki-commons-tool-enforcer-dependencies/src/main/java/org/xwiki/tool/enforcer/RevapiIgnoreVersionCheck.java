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
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * Verifies that every Revapi difference declared in the {@code pom.xml} tells which release it targets, so that the
 * release tooling can remove exactly the differences of the version it releases and keep the others.
 * <p>
 * The target versions are given as a comma-separated list in an attachment of the difference, or of the
 * {@code revapi.differences} group containing it (the difference's own value takes precedence). A list allows keeping
 * the same difference unchanged when backporting it. For example:
 *
 * <pre><code>
 * &lt;revapi.differences&gt;
 *   &lt;justification&gt;...&lt;/justification&gt;
 *   &lt;attachments&gt;
 *     &lt;versions&gt;18.10.0-rc-1,18.4.7&lt;/versions&gt;
 *   &lt;/attachments&gt;
 *   &lt;differences&gt;
 *     &lt;item&gt;...&lt;/item&gt;
 *   &lt;/differences&gt;
 * &lt;/revapi.differences&gt;
 * </code></pre>
 * <p>
 * Revapi only adds attachments to the differences it matches, it doesn't use them for matching, so declaring them
 * doesn't change which differences are ignored.
 * <p>
 * At least one of the listed versions must be a version not released yet on the current branch: strictly greater than
 * {@link #setPreviousVersion(String) the previous version} and lower than or equal to the project version. Since the
 * release tooling removes the differences of each version it releases, a difference matching none of them is either
 * stale or doesn't list the version of the current branch.
 * <p>
 * Only the {@code pom.xml} of the current module is checked (before inheritance), so the rule can be configured once
 * for all modules: it only reports something in the modules declaring Revapi differences.
 *
 * <pre><code>
 *   &lt;rules&gt;
 *     &lt;revapiIgnoreVersionCheck&gt;
 *       &lt;previousVersion&gt;${xwiki.compatibility.previous.version}&lt;/previousVersion&gt;
 *     &lt;/revapiIgnoreVersionCheck&gt;
 *   &lt;/rules&gt;
 * </code></pre>
 *
 * @version $Id$
 * @since 19.0.0RC1
 */
@Named("revapiIgnoreVersionCheck")
public class RevapiIgnoreVersionCheck extends AbstractPomCheck
{
    private static final String REVAPI_GROUP_ID = "org.revapi";

    private static final String REVAPI_ARTIFACT_ID = "revapi-maven-plugin";

    private static final String ANALYSIS_CONFIGURATION = "analysisConfiguration";

    private static final String DIFFERENCES_GROUP = "revapi.differences";

    private static final String DIFFERENCES = "differences";

    private static final String ATTACHMENTS = "attachments";

    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    /**
     * The format of an XWiki release version, for example {@code 18.10.0}, {@code 18.10.0-rc-1} or
     * {@code 7.4.1-milestone-1}.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(\\.\\d+)+(-[a-z]+-\\d+)?");

    private String attachmentKey = "versions";

    private String previousVersion;

    /**
     * @param attachmentKey the name of the attachment holding the comma-separated list of target versions
     *     ({@code versions} by default)
     */
    public void setAttachmentKey(String attachmentKey)
    {
        this.attachmentKey = attachmentKey;
    }

    /**
     * @param previousVersion the last version released on the current branch, the one Revapi compares against
     *     (usually {@code ${xwiki.compatibility.previous.version}}); a target version must be greater than it
     */
    public void setPreviousVersion(String previousVersion)
    {
        this.previousVersion = previousVersion;
    }

    @Override
    public void execute() throws EnforcerRuleException
    {
        List<Xpp3Dom> groups = getDifferencesGroups(getModel());
        if (groups.isEmpty()) {
            return;
        }

        ComparableVersion lowerBound = getPreviousVersion();
        String currentVersion = getCurrentVersion();
        ComparableVersion upperBound = new ComparableVersion(currentVersion);

        List<String> errors = new ArrayList<>();
        for (Xpp3Dom group : groups) {
            String groupVersions = getAttachment(group);
            Xpp3Dom differences = group.getChild(DIFFERENCES);
            if (differences != null) {
                for (Xpp3Dom item : differences.getChildren()) {
                    String itemVersions = getAttachment(item);
                    String error = validate(itemVersions != null ? itemVersions : groupVersions, lowerBound,
                        upperBound);
                    if (error != null) {
                        errors.add(String.format("- %s: %s", describe(item), error));
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new EnforcerRuleException(String.format("The following Revapi differences of [%s] don't target "
                + "a version not released yet (greater than [%s] and lower than or equal to [%s]):%n%s%n"
                + "Indicate the release that will first contain the change, as a comma-separated list if the "
                + "difference is also backported, in the difference or in its revapi.differences group, e.g.:%n"
                + "  <%s>%n    <%s>%s</%s>%n  </%s>", this.project.getFile(), this.previousVersion, currentVersion,
                String.join(System.lineSeparator(), errors), ATTACHMENTS, this.attachmentKey, currentVersion,
                this.attachmentKey, ATTACHMENTS));
        }
    }

    private ComparableVersion getPreviousVersion() throws EnforcerRuleException
    {
        // An undefined property used as value (for example ${xwiki.compatibility.previous.version}) isn't
        // interpolated.
        if (this.previousVersion == null || this.previousVersion.isBlank() || this.previousVersion.startsWith("${")) {
            throw new EnforcerRuleException(String.format("The [previousVersion] parameter of the rule is mandatory "
                + "but got [%s].", this.previousVersion));
        }
        return new ComparableVersion(this.previousVersion);
    }

    private String getCurrentVersion()
    {
        String version = this.project.getVersion();
        if (version.endsWith(SNAPSHOT_SUFFIX)) {
            version = version.substring(0, version.length() - SNAPSHOT_SUFFIX.length());
        }
        return version;
    }

    private String validate(String versions, ComparableVersion lowerBound, ComparableVersion upperBound)
    {
        if (versions == null || versions.isBlank()) {
            return String.format("no [%s] attachment", this.attachmentKey);
        }

        boolean unreleased = false;
        for (String version : versions.split(",")) {
            String trimmedVersion = version.trim();
            if (!VERSION_PATTERN.matcher(trimmedVersion).matches()) {
                return String.format("[%s] is not a release version (e.g. 18.10.0 or 18.10.0-rc-1)", trimmedVersion);
            }
            ComparableVersion comparableVersion = new ComparableVersion(trimmedVersion);
            unreleased |= comparableVersion.compareTo(lowerBound) > 0 && comparableVersion.compareTo(upperBound) <= 0;
        }

        return unreleased ? null : String.format("none of the versions [%s] is still to be released", versions);
    }

    private String getAttachment(Xpp3Dom element)
    {
        Xpp3Dom attachments = element.getChild(ATTACHMENTS);
        if (attachments != null) {
            Xpp3Dom value = attachments.getChild(this.attachmentKey);
            if (value != null) {
                return value.getValue();
            }
        }
        return null;
    }

    private String describe(Xpp3Dom item)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getChildValue(item, "code"));
        for (String element : List.of("old", "new")) {
            String value = getChildValue(item, element);
            if (value != null) {
                builder.append(String.format(" %s [%s]", element, value.trim()));
            }
        }
        return builder.toString();
    }

    private String getChildValue(Xpp3Dom element, String name)
    {
        Xpp3Dom child = element.getChild(name);
        return child != null ? child.getValue() : null;
    }

    private List<Xpp3Dom> getDifferencesGroups(Model model)
    {
        List<Plugin> plugins = new ArrayList<>();
        Build build = model.getBuild();
        if (build != null) {
            addPlugins(build, plugins);
        }
        for (Profile profile : model.getProfiles()) {
            if (profile.getBuild() != null) {
                addPlugins(profile.getBuild(), plugins);
            }
        }

        List<Xpp3Dom> groups = new ArrayList<>();
        for (Plugin plugin : plugins) {
            if (REVAPI_GROUP_ID.equals(plugin.getGroupId()) && REVAPI_ARTIFACT_ID.equals(plugin.getArtifactId())) {
                addGroups(plugin.getConfiguration(), groups);
                for (PluginExecution execution : plugin.getExecutions()) {
                    addGroups(execution.getConfiguration(), groups);
                }
            }
        }
        return groups;
    }

    private void addPlugins(BuildBase build, List<Plugin> plugins)
    {
        plugins.addAll(build.getPlugins());
        PluginManagement pluginManagement = build.getPluginManagement();
        if (pluginManagement != null) {
            plugins.addAll(pluginManagement.getPlugins());
        }
    }

    private void addGroups(Object configuration, List<Xpp3Dom> groups)
    {
        if (configuration instanceof Xpp3Dom dom) {
            Xpp3Dom analysisConfiguration = dom.getChild(ANALYSIS_CONFIGURATION);
            if (analysisConfiguration != null) {
                groups.addAll(List.of(analysisConfiguration.getChildren(DIFFERENCES_GROUP)));
            }
        }
    }
}
