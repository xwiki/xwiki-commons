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
import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.DefaultExtensionIssueManagement;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionAuthor;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionIssueManagement;
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
    private WeakHashMap<ExtensionDependency, ExtensionDependency> dependencies = new WeakHashMap<>();

    private WeakHashMap<ExtensionAuthor, ExtensionAuthor> authors = new WeakHashMap<>();

    private WeakHashMap<ExtensionRepositoryDescriptor, ExtensionRepositoryDescriptor> repositories =
        new WeakHashMap<>();

    private WeakHashMap<ExtensionIssueManagement, ExtensionIssueManagement> issueManagements = new WeakHashMap<>();

    private WeakHashMap<String, Version> versions = new WeakHashMap<>();

    private WeakHashMap<String, VersionConstraint> versionConstrains = new WeakHashMap<>();

    private static <T> T get(WeakHashMap<T, T> map, T entry)
    {
        // Check if we only know an equal entry
        T sharedEntry = map.get(entry);

        if (sharedEntry == null) {
            // If no entry can be found, store and return the passed one
            sharedEntry = entry;
        }

        // Make sure to remember the entry
        map.put(sharedEntry, sharedEntry);

        // Return the shared entry
        return sharedEntry;
    }

    private static <K, V> V get(WeakHashMap<K, V> map, K key, V value)
    {
        // Check if we only know an equal entry
        V sharedValue = map.get(key);

        if (sharedValue == null) {
            // If no entry can be found, store and return the passed one
            sharedValue = value;
        }

        // Make sure to remember the entry
        map.put(key, sharedValue);

        // Return the shared entry
        return sharedValue;
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionDependency}.
     * 
     * @param dependency the {@link ExtensionDependency} to find
     * @return unique instance of {@link ExtensionDependency} equals to the passed one
     */
    public ExtensionDependency getExtensionDependency(ExtensionDependency dependency)
    {
        return get(this.dependencies, dependency);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionDependency}.
     * 
     * @param id the id of the extension dependency
     * @param versionConstraint the version constraint of the extension dependency
     * @param properties the custom properties of the extension dependency
     * @return unique instance of {@link ExtensionDependency} equals to the passed one
     */
    public ExtensionDependency getExtensionDependency(String id, VersionConstraint versionConstraint,
        Map<String, Object> properties)
    {
        return getExtensionDependency(new DefaultExtensionDependency(id, versionConstraint, properties));
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionAuthor}.
     * 
     * @param author the {@link ExtensionAuthor} to find
     * @return unique instance of {@link ExtensionAuthor} equals to the passed one
     */
    public ExtensionAuthor getExtensionAuthor(ExtensionAuthor author)
    {
        return get(this.authors, author);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionAuthor}.
     * 
     * @param name the name of the author
     * @param url the URL of the author public profile
     * @return unique instance of {@link ExtensionAuthor} equals to the passed one
     */
    public ExtensionAuthor getExtensionAuthor(String name, URL url)
    {
        return getExtensionAuthor(new DefaultExtensionAuthor(name, url));
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionRepositoryDescriptor}.
     * 
     * @param repository the {@link ExtensionRepositoryDescriptor} to find
     * @return unique instance of {@link ExtensionRepositoryDescriptor} equals to the passed one
     */
    public ExtensionRepositoryDescriptor getExtensionRepositoryDescriptor(ExtensionRepositoryDescriptor repository)
    {
        return get(this.repositories, repository);
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionRepositoryDescriptor} elements.
     * 
     * @param id the unique identifier
     * @param type the repository type (maven, xwiki, etc.)
     * @param uri the repository address
     * @return unique instance of {@link ExtensionRepositoryDescriptor} equals to the passed one
     */
    public ExtensionRepositoryDescriptor getExtensionRepositoryDescriptor(String id, String type, URI uri)
    {
        return getExtensionRepositoryDescriptor(new DefaultExtensionRepositoryDescriptor(id, type, uri));
    }

    /**
     * Store and return a weak reference equals to the passed {@link ExtensionIssueManagement}.
     * 
     * @param issueManagement the {@link ExtensionIssueManagement} to find
     * @return unique instance of {@link ExtensionIssueManagement} equals to the passed one
     */
    public ExtensionIssueManagement getExtensionIssueManagement(ExtensionIssueManagement issueManagement)
    {
        return get(this.issueManagements, issueManagement);
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
        return get(this.versions, version.getValue(), version);
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
        return get(this.versionConstrains, versionConstraint.getValue(), versionConstraint);
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
