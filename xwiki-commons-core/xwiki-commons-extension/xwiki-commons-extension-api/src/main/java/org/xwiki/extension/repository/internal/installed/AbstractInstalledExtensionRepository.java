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
package org.xwiki.extension.repository.internal.installed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.namespace.Namespace;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.internal.tree.DefaultExtensionNode;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.internal.AbstractCachedExtensionRepository;
import org.xwiki.extension.repository.internal.RepositoryUtils;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.tree.ExtensionNode;

/**
 * Base class for {@link InstalledExtensionRepository} implementations.
 *
 * @param <E> the type of the extension
 * @version $Id$
 * @since 7.0M2
 */
public abstract class AbstractInstalledExtensionRepository<E extends InstalledExtension>
    extends AbstractCachedExtensionRepository<E> implements InstalledExtensionRepository
{
    @Inject
    protected transient Logger logger;

    @Override
    public int countExtensions()
    {
        return this.extensions.size();
    }

    @Override
    public Collection<InstalledExtension> getInstalledExtensions(String namespace)
    {
        List<InstalledExtension> installedExtensions = new ArrayList<InstalledExtension>(extensions.size());
        for (InstalledExtension localExtension : this.extensions.values()) {
            if (localExtension.isInstalled(namespace)) {
                installedExtensions.add(localExtension);
            }
        }

        return installedExtensions;
    }

    @Override
    public Collection<InstalledExtension> getInstalledExtensions()
    {
        return Collections.<InstalledExtension>unmodifiableCollection(this.extensions.values());
    }

    @Override
    public InstalledExtension getInstalledExtension(ExtensionId extensionId)
    {
        return this.extensions.get(extensionId);
    }

    @Override
    public InstalledExtension installExtension(LocalExtension extension, String namespace, boolean dependency)
        throws InstallException
    {
        return installExtension(extension, namespace, dependency, Collections.<String, Object>emptyMap());
    }

    @Override
    public IterableResult<InstalledExtension> searchInstalledExtensions(String pattern, String namespace, int offset,
        int nb) throws SearchException
    {
        ExtensionQuery query = new ExtensionQuery(pattern);

        query.setOffset(offset);
        query.setLimit(nb);

        return searchInstalledExtensions(namespace, query);
    }

    @Override
    public IterableResult<InstalledExtension> searchInstalledExtensions(ExtensionQuery query)
    {
        return searchInstalledExtensions((List<String>) null, query, this.extensions.values());
    }

    @Override
    public IterableResult<InstalledExtension> searchInstalledExtensions(Collection<String> namespaces,
        ExtensionQuery query)
    {
        return searchInstalledExtensions(namespaces, query, this.extensions.values());
    }

    @Override
    public IterableResult<InstalledExtension> searchInstalledExtensions(String namespace, ExtensionQuery query)
        throws SearchException
    {
        return searchInstalledExtensions(namespace, query, this.extensions.values());
    }

    protected IterableResult<InstalledExtension> searchInstalledExtensions(String namespace, ExtensionQuery query,
        Collection<? extends InstalledExtension> installedExtensions)
    {
        return searchInstalledExtensions(Arrays.asList(namespace), query, installedExtensions);
    }

    protected IterableResult<InstalledExtension> searchInstalledExtensions(Collection<String> namespaces,
        ExtensionQuery query, Collection<? extends InstalledExtension> installedExtensions)
    {
        // Filter extension matching passed query and namespaces
        List<InstalledExtension> result = filter(namespaces, query, installedExtensions);

        // Make sure all the elements of the list are unique
        if (result.size() > 1) {
            result = new ArrayList<>(new LinkedHashSet<>(result));
        }

        // Sort
        RepositoryUtils.sort(result, query.getSortClauses());

        return RepositoryUtils.getIterableResult(query.getOffset(), query.getLimit(), result);
    }

    protected List<InstalledExtension> filter(Collection<String> namespaces, ExtensionQuery query,
        Collection<? extends InstalledExtension> installedExtensions)
    {
        Pattern patternMatcher = RepositoryUtils.createPatternMatcher(query.getQuery());

        List<InstalledExtension> result = new ArrayList<>(installedExtensions.size());

        for (InstalledExtension installedExtension : installedExtensions) {
            if (namespaces == null || namespaces.isEmpty()) {
                if (RepositoryUtils.matches(patternMatcher, query.getFilters(), installedExtension)) {
                    result.add(installedExtension);
                }
            } else {
                for (String namespace : namespaces) {
                    if (installedExtension.isInstalled(namespace)) {
                        if (RepositoryUtils.matches(patternMatcher, query.getFilters(), installedExtension)) {
                            result.add(installedExtension);
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public ExtensionNode<InstalledExtension> getOrphanedDependencies(InstalledExtension installedExtension,
        Namespace namespace)
    {
        ExtensionNode<InstalledExtension> node;

        Map<String, Set<ExtensionId>> backward = new HashMap<>();

        int count = 10;

        do {
            int backwardSize = backward.size();

            node = getOrphanedDependencies(installedExtension, namespace, backward);

            if (backward.size() == 1 || backwardSize == backward.size()) {
                break;
            }
        } while (count > 0);

        return node;
    }

    private ExtensionNode<InstalledExtension> getOrphanedDependencies(InstalledExtension installedExtension,
        Namespace namespace, Map<String, Set<ExtensionId>> backward)
    {
        String namespaceString = namespace.toString();

        // Remember the extension in the list of backward dependencies
        Set<ExtensionId> backwardNamespace = backward.get(namespaceString);
        if (backwardNamespace == null) {
            backwardNamespace = new HashSet<>();
            backward.put(namespaceString, backwardNamespace);
        }
        backwardNamespace.add(installedExtension.getId());

        // Search exclusive dependencies
        List<ExtensionNode<InstalledExtension>> orphaned = new ArrayList<>(installedExtension.getDependencies().size());
        for (ExtensionDependency dependency : installedExtension.getDependencies()) {
            InstalledExtension dependencyExtension = getInstalledExtension(dependency.getId(), namespaceString);

            if (dependencyExtension != null) {
                String dependencyNamespace = namespaceString;
                if (dependencyNamespace != null && dependencyExtension.isInstalled(null)) {
                    dependencyNamespace = null;
                }

                if (dependencyExtension.isDependency(dependencyNamespace)
                    && isExclusive(dependencyExtension, dependencyNamespace, backward)) {
                    orphaned.add(getOrphanedDependencies(dependencyExtension, namespace, backward));
                }
            }
        }

        // Create a new node
        return new DefaultExtensionNode<>(namespace, installedExtension, orphaned);
    }

    private boolean isExclusive(InstalledExtension dependencyExtension, String namespace,
        Map<String, Set<ExtensionId>> backward)
    {
        try {
            if (namespace == null) {
                Map<String, Collection<InstalledExtension>> backwardDependencies =
                    getBackwardDependencies(dependencyExtension.getId(), true);

                for (Map.Entry<String, Collection<InstalledExtension>> entry : backwardDependencies.entrySet()) {
                    if (!isExclusive(entry.getValue(), backward.get(entry.getKey()))) {
                        return false;
                    }
                }
            } else {
                Collection<InstalledExtension> backwardDependencies =
                    getBackwardDependencies(dependencyExtension.getId().getId(), namespace, true);

                if (!isExclusive(backwardDependencies, backward.get(namespace))) {
                    return false;
                }
            }

            return true;
        } catch (ResolveException e) {
            this.logger.error("Failed to get backward dependencies for id [{}] on namespace [{}]: {}",
                dependencyExtension.getId(), namespace, ExceptionUtils.getRootCauseMessage(e));
        }

        return false;
    }

    private boolean isExclusive(Collection<InstalledExtension> backwardDependencies, Set<ExtensionId> backward)
    {
        if (backward == null) {
            return false;
        }

        for (InstalledExtension backwardDependency : backwardDependencies) {
            if (!backward.contains(backwardDependency.getId())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Map<String, Collection<InstalledExtension>> getBackwardDependencies(ExtensionId extensionId)
        throws ResolveException
    {
        return getBackwardDependencies(extensionId, false);
    }

    @Override
    public Collection<InstalledExtension> getBackwardDependencies(String feature, String namespace)
        throws ResolveException
    {
        return getBackwardDependencies(feature, namespace, false);
    }
}
