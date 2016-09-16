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
package org.xwiki.extension.repository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.internal.RepositoryUtils;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.ExtensionQuery.COMPARISON;
import org.xwiki.extension.repository.search.ExtensionQuery.Filter;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.version.Version;

/**
 * A base class to use for a repository proxy searching in all remote extension repositories but filtering the result on
 * provided criteria.
 * 
 * @version $Id$
 * @since 8.3RC1
 */
public abstract class AbstractFilteredExtensionRepository extends AbstractAdvancedSearchableExtensionRepository
{
    @Inject
    private ExtensionRepositoryManager repositories;

    private List<Filter> filters = new ArrayList<>();

    /**
     * @param field the name of the field
     * @param value the value to compare to
     * @param comparison the comparison to apply
     */
    public void addFilter(String field, Object value, COMPARISON comparison)
    {
        this.filters.add(new Filter(field, value, comparison));
    }

    @Override
    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        for (ExtensionRepository remoteRepository : this.repositories.getRepositories()) {
            Extension extension = remoteRepository.resolve(extensionId);

            if (extension != null && RepositoryUtils.matches(this.filters, extension)) {
                return extension;
            }
        }

        return null;
    }

    @Override
    public Extension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        for (ExtensionRepository remoteRepository : this.repositories.getRepositories()) {
            Extension extension = remoteRepository.resolve(extensionDependency);

            if (extension != null && RepositoryUtils.matches(this.filters, extension)) {
                return extension;
            }
        }

        return null;
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isFilterable()
    {
        return this.repositories.isFilterable();
    }

    @Override
    public boolean isSortable()
    {
        return this.repositories.isSortable();
    }

    @Override
    public IterableResult<Extension> search(ExtensionQuery inputQuery) throws SearchException
    {
        ExtensionQuery query = new ExtensionQuery(inputQuery);

        query.addFilters(this.filters);

        return this.repositories.search(query);
    }
}
