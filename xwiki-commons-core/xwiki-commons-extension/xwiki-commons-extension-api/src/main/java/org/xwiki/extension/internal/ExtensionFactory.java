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
package org.xwiki.extension.internal;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Singleton;

import org.xwiki.collection.SoftCache;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.DefaultExtensionIssueManagement;
import org.xwiki.extension.DefaultExtensionPattern;
import org.xwiki.extension.DefaultExtensionSupportPlan;
import org.xwiki.extension.DefaultExtensionSupporter;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionAuthor;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionIssueManagement;
import org.xwiki.extension.ExtensionPattern;
import org.xwiki.extension.ExtensionSupportPlan;
import org.xwiki.extension.ExtensionSupporter;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;

/**
 * Allow sharing the same instance for various part of {@link Extension}s.
 * 
 * @version $Id$
 * @since 8.4
 */
@Component(roles = ExtensionFactory.class)
@Singleton
public class ExtensionFactory
{
    private static final ExtensionPattern NULL_PATTERN = new DefaultExtensionPattern((Pattern) null);

    private final SoftCache<ExtensionDependency, ExtensionDependency> dependencies = new SoftCache<>();

    private final SoftCache<String, ExtensionPattern> patterns = new SoftCache<>();

    private final SoftCache<ExtensionAuthor, ExtensionAuthor> authors = new SoftCache<>();

    private final SoftCache<ExtensionSupportPlan, ExtensionSupportPlan> supportPlans = new SoftCache<>();

    private final SoftCache<ExtensionSupporter, ExtensionSupporter> supporters = new SoftCache<>();

    private final SoftCache<ExtensionRepositoryDescriptor, ExtensionRepositoryDescriptor> repositories =
        new SoftCache<>();

    private final SoftCache<ExtensionIssueManagement, ExtensionIssueManagement> issueManagements = new SoftCache<>();

    private final SoftCache<String, Version> versions = new SoftCache<>();

    private final SoftCache<String, VersionConstraint> versionConstrains = new SoftCache<>();

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionAuthor}.
     * 
     * @param factory the factory to use or null
     * @param name the name of the author
     * @param url the url of the author public profile
     * @return unique instance of {@link ExtensionAuthor} equals to the passed one
     * @since 16.7.0RC1
     */
    public static ExtensionAuthor getExtensionAuthor(ExtensionFactory factory, String name, String url)
    {
        return factory != null ? factory.getExtensionAuthor(name, url) : new DefaultExtensionAuthor(name, url);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionSupporter}.
     * 
     * @param factory the factory to use or null
     * @param name the name of the supporter
     * @param url the url leading to more detail about the supporter
     * @return unique instance of {@link ExtensionSupporter} equals to the passed one
     * @since 16.7.0RC1
     */
    public static ExtensionSupporter getExtensionSupporter(ExtensionFactory factory, String name, URL url)
    {
        return factory != null ? factory.getExtensionSupporter(name, url) : new DefaultExtensionSupporter(name, url);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionSupportPlan}.
     * 
     * @param factory the factory to use or null
     * @param supporter the supporter
     * @param name the name of the author
     * @param url the URL of the author public profile
     * @param paying indicate if the plan is paying or free of charge
     * @return unique instance of {@link ExtensionSupportPlan} equals to the passed one
     * @since 16.7.0RC1
     */
    public static ExtensionSupportPlan getExtensionSupportPlan(ExtensionFactory factory, ExtensionSupporter supporter,
        String name, URL url, boolean paying)
    {
        return factory != null ? factory.getExtensionSupportPlan(supporter, name, url, paying)
            : new DefaultExtensionSupportPlan(supporter, name, url, paying);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionDependency}.
     * 
     * @param dependency the {@link ExtensionDependency} to find
     * @return unique instance of {@link ExtensionDependency} equals to the passed one
     */
    public ExtensionDependency getExtensionDependency(ExtensionDependency dependency)
    {
        return this.dependencies.get(dependency, dependency);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionDependency}.
     * 
     * @param id the id of the extension dependency
     * @param versionConstraint the version constraint of the extension dependency
     * @param optional true if the dependency is optional
     * @param exclusions the exclusions patterns to apply to transitive dependencies
     * @param repositories the custom repositories provided by the extension (usually to resolve dependencies)
     * @param properties the custom properties of the extension dependency
     * @return unique instance of {@link ExtensionDependency} equals to the passed one
     * @since 12.2
     */
    public ExtensionDependency getExtensionDependency(String id, VersionConstraint versionConstraint, boolean optional,
        Collection<ExtensionPattern> exclusions, Collection<ExtensionRepositoryDescriptor> repositories,
        Map<String, Object> properties)
    {
        DefaultExtensionDependency dependency =
            new DefaultExtensionDependency(id, versionConstraint, optional, properties);
        dependency.setRepositories(repositories);
        dependency.setExclusions(exclusions);

        return getExtensionDependency(dependency);
    }

    /**
     * @param idPattern a regular expression matching all the ids to exclude
     * @return unique instance of {@link ExtensionPattern} equals to the passed one
     * @since 12.2
     */
    public ExtensionPattern getExtensionPattern(Pattern idPattern)
    {
        if (idPattern == null) {
            return NULL_PATTERN;
        }

        String key = idPattern.pattern();

        ExtensionPattern pattern = this.patterns.get(key);

        if (pattern == null) {
            pattern = new DefaultExtensionPattern(idPattern);

            this.patterns.put(key, pattern);
        }

        return pattern;
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionAuthor}.
     * 
     * @param author the {@link ExtensionAuthor} to find
     * @return unique instance of {@link ExtensionAuthor} equals to the passed one
     */
    public ExtensionAuthor getExtensionAuthor(ExtensionAuthor author)
    {
        return this.authors.get(author, author);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionAuthor}.
     * 
     * @param name the name of the author
     * @param url the url of the author public profile
     * @return unique instance of {@link ExtensionAuthor} equals to the passed one
     */
    public ExtensionAuthor getExtensionAuthor(String name, String url)
    {
        return getExtensionAuthor(new DefaultExtensionAuthor(name, url));
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionSupporter}.
     * 
     * @param supporter the {@link ExtensionSupporter} to find
     * @return unique instance of {@link ExtensionSupporter} equals to the passed one
     * @since 16.7.0RC1
     */
    public ExtensionSupporter getExtensionSupporter(ExtensionSupporter supporter)
    {
        return this.supporters.get(supporter, supporter);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionSupporter}.
     * 
     * @param name the name of the supporter
     * @param url the url leading to more detail about the supporter
     * @return unique instance of {@link ExtensionSupporter} equals to the passed one
     * @since 16.7.0RC1
     */
    public ExtensionSupporter getExtensionSupporter(String name, URL url)
    {
        return getExtensionSupporter(new DefaultExtensionSupporter(name, url));
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionSupportPlan}.
     * 
     * @param supportPlan the {@link ExtensionSupportPlan} to find
     * @return unique instance of {@link ExtensionSupportPlan} equals to the passed one
     * @since 16.7.0RC1
     */
    public ExtensionSupportPlan getExtensionSupportPlan(ExtensionSupportPlan supportPlan)
    {
        return this.supportPlans.get(supportPlan, supportPlan);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionSupportPlan}.
     * 
     * @param supporter the supporter
     * @param name the name of the author
     * @param url the URL of the author public profile
     * @param paying indicate if the plan is paying or free of charge
     * @return unique instance of {@link ExtensionSupportPlan} equals to the passed one
     * @since 16.7.0RC1
     */
    public ExtensionSupportPlan getExtensionSupportPlan(ExtensionSupporter supporter, String name, URL url,
        boolean paying)
    {
        return getExtensionSupportPlan(new DefaultExtensionSupportPlan(supporter, name, url, paying));
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionRepositoryDescriptor}.
     * 
     * @param repository the {@link ExtensionRepositoryDescriptor} to find
     * @return unique instance of {@link ExtensionRepositoryDescriptor} equals to the passed one
     */
    public ExtensionRepositoryDescriptor getExtensionRepositoryDescriptor(ExtensionRepositoryDescriptor repository)
    {
        return this.repositories.get(repository, repository);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionRepositoryDescriptor} elements.
     * 
     * @param id the unique identifier
     * @param type the repository type (maven, xwiki, etc.)
     * @param uri the repository address
     * @param properties the properties
     * @return unique instance of {@link ExtensionRepositoryDescriptor} equals to the passed one
     */
    public ExtensionRepositoryDescriptor getExtensionRepositoryDescriptor(String id, String type, URI uri,
        Map<String, String> properties)
    {
        return getExtensionRepositoryDescriptor(new DefaultExtensionRepositoryDescriptor(id, type, uri, properties));
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionIssueManagement}.
     * 
     * @param issueManagement the {@link ExtensionIssueManagement} to find
     * @return unique instance of {@link ExtensionIssueManagement} equals to the passed one
     */
    public ExtensionIssueManagement getExtensionIssueManagement(ExtensionIssueManagement issueManagement)
    {
        return this.issueManagements.get(issueManagement, issueManagement);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionIssueManagement} elements.
     * 
     * @param system the name of the issue management system (jira, bugzilla, etc.)
     * @param url the URL of that extension in the issues management system
     * @return unique instance of {@link ExtensionIssueManagement} equals to the passed one
     */
    public ExtensionIssueManagement getExtensionIssueManagement(String system, String url)
    {
        return getExtensionIssueManagement(new DefaultExtensionIssueManagement(system, url));
    }

    /**
     * Store and return a weak reference equals to the passed {@link Version}.
     * 
     * @param version the {@link Version} to find
     * @return unique instance of {@link Version} equals to the passed one
     */
    public Version getVersion(Version version)
    {
        // Use the initial value as key because it's displayed and for example displaying "1.0" instead of "1.0.0.GA"
        // might produce a WTF effect even if it's exactly the same version.
        return this.versions.get(version.getValue(), version);
    }

    /**
     * Store and return a weak reference equals to the passed version.
     * 
     * @param rawVersion the version to find
     * @return unique instance of {@link VersionConstraint} equals to the passed one
     */
    public Version getVersion(String rawVersion)
    {
        Version version = this.versions.get(rawVersion);

        if (version == null) {
            version = new DefaultVersion(rawVersion);

            this.versions.put(rawVersion, version);
        }

        return version;
    }

    /**
     * Store and return a weak reference equals to the passed {@link VersionConstraint}.
     * 
     * @param versionConstraint the {@link VersionConstraint} to find
     * @return unique instance of {@link VersionConstraint} equals to the passed one
     */
    public VersionConstraint getVersionConstraint(VersionConstraint versionConstraint)
    {
        // Use the initial value as key because it's displayed and for example displaying "[1.0]" instead of
        // "[1.0.0.GA]" might produce a WTF effect even if it's exactly the same version constraint.
        return this.versionConstrains.get(versionConstraint.getValue(), versionConstraint);
    }

    /**
     * Store and return a weak reference equals to the passed version constraint.
     * 
     * @param rawConstraint the version constraint to find
     * @return unique instance of {@link VersionConstraint} equals to the passed one
     */
    public VersionConstraint getVersionConstraint(String rawConstraint)
    {
        VersionConstraint constraint = this.versionConstrains.get(rawConstraint);

        if (constraint == null) {
            constraint = new DefaultVersionConstraint(rawConstraint);

            this.versionConstrains.put(rawConstraint, constraint);
        }

        return constraint;
    }
}
