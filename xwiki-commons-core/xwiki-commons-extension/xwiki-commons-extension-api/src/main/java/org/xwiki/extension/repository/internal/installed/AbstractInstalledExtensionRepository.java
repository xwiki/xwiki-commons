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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.internal.AbstractCachedExtensionRepository;
import org.xwiki.extension.repository.internal.RepositoryUtils;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.SearchException;

/**
 * Base class for {@link InstalledExtensionRepository} implementations.
 *
 * @param <E> the type of the extension
 * @version $Id$
 * @since 7.0M2
 */
public abstract class AbstractInstalledExtensionRepository<E extends InstalledExtension> extends
    AbstractCachedExtensionRepository<E> implements InstalledExtensionRepository
{
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
    public IterableResult<InstalledExtension> searchInstalledExtensions(String namespace, ExtensionQuery query)
        throws SearchException
    {
        return searchInstalledExtensions(namespace, query, this.extensions.values());
    }

    protected IterableResult<InstalledExtension> searchInstalledExtensions(String namespace, ExtensionQuery query,
        Collection<? extends InstalledExtension> installedExtensions)
    {
        Pattern patternMatcher = RepositoryUtils.createPatternMatcher(query.getQuery());

        List<InstalledExtension> result = new ArrayList<InstalledExtension>(installedExtensions.size());

        for (InstalledExtension installedExtension : installedExtensions) {
            if (installedExtension.isInstalled(namespace)) {
                if ((patternMatcher == null || RepositoryUtils.matches(patternMatcher, query.getFilters(),
                    installedExtension))) {
                    result.add(installedExtension);
                }
            }
        }

        // Make sure all the elements of the list are unique
        if (result.size() > 1) {
            result = new ArrayList<>(new LinkedHashSet<>(result));
        }

        // Sort
        RepositoryUtils.sort(result, query.getSortClauses());

        return RepositoryUtils.getIterableResult(query.getOffset(), query.getLimit(), result);
    }
}
