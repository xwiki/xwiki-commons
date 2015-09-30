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
package org.xwiki.extension.repository.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryException;
import org.xwiki.extension.repository.ExtensionRepositoryFactory;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.ExtensionRepositorySource;
import org.xwiki.extension.repository.result.AggregatedIterableResult;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.AdvancedSearchable;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.repository.search.Searchable;
import org.xwiki.extension.version.Version;

/**
 * Default implementation of {@link ExtensionRepositoryManager}.
 *
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultExtensionRepositoryManager implements ExtensionRepositoryManager, Initializable
{
    /**
     * Used to lookup {@link ExtensionRepositoryFactory}s.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * Used to initialize {@link #repositoryManager}.
     */
    @Inject
    private List<ExtensionRepositorySource> repositoriesSources;

    /**
     * The registered repositories.
     */
    private final Map<String, ExtensionRepository> repositoryMap = Collections
        .synchronizedMap(new LinkedHashMap<String, ExtensionRepository>());

    private Collection<ExtensionRepository> repositories = Collections.emptyList();

    private LRUMap<ExtensionRepositoryDescriptor, ExtensionRepository> repositoriesCache = new LRUMap<>(100);

    @Override
    public void initialize() throws InitializationException
    {
        // Load extension repositories
        for (ExtensionRepositorySource repositoriesSource : this.repositoriesSources) {
            for (ExtensionRepositoryDescriptor repositoryDescriptor : repositoriesSource
                .getExtensionRepositoryDescriptors()) {
                try {
                    addRepository(repositoryDescriptor);
                } catch (ExtensionRepositoryException e) {
                    this.logger.error("Failed to add repository [" + repositoryDescriptor + "]", e);
                }
            }
        }
    }

    @Override
    @Deprecated
    public ExtensionRepository addRepository(ExtensionRepositoryId repositoryId) throws ExtensionRepositoryException
    {
        return addRepository((ExtensionRepositoryDescriptor) repositoryId);
    }

    @Override
    public ExtensionRepository addRepository(ExtensionRepositoryDescriptor repositoryDescriptor)
        throws ExtensionRepositoryException
    {
        ExtensionRepository repository;

        try {
            ExtensionRepositoryFactory repositoryFactory =
                this.componentManager.getInstance(ExtensionRepositoryFactory.class, repositoryDescriptor.getType());

            repository = repositoryFactory.createRepository(repositoryDescriptor);

            addRepository(repository);
        } catch (ComponentLookupException e) {
            throw new ExtensionRepositoryException("Unsupported repository type [" + repositoryDescriptor.getType()
                + "]", e);
        }

        return repository;
    }

    @Override
    public void addRepository(ExtensionRepository repository)
    {
        this.repositoryMap.put(repository.getDescriptor().getId(), repository);
        this.repositories = new ArrayList<>(this.repositoryMap.values());
    }

    @Override
    public void removeRepository(String repositoryId)
    {
        this.repositoryMap.remove(repositoryId);
        this.repositories = new ArrayList<>(this.repositoryMap.values());
    }

    @Override
    public ExtensionRepository getRepository(String repositoryId)
    {
        return this.repositoryMap.get(repositoryId);
    }

    private ExtensionRepository getRepository(ExtensionRepositoryDescriptor repositoryDescriptor)
        throws ExtensionRepositoryException
    {
        // Try in the cache
        ExtensionRepository repository = this.repositoriesCache.get(repositoryDescriptor);

        if (repository == null) {
            // Try in the registered repositories
            if (repositoryDescriptor.getId() != null) {
                repository = getRepository(repositoryDescriptor.getId());
            }

            if (repository == null || !repository.getDescriptor().equals(repositoryDescriptor)) {
                // Create one
                ExtensionRepositoryFactory repositoryFactory;
                try {
                    repositoryFactory =
                        this.componentManager.getInstance(ExtensionRepositoryFactory.class,
                            repositoryDescriptor.getType());
                } catch (ComponentLookupException e) {
                    throw new ExtensionRepositoryException("Unsupported extension repository type [{"
                        + repositoryDescriptor.getType() + "}]", e);
                }

                repository = repositoryFactory.createRepository(repositoryDescriptor);
            }

            this.repositoriesCache.put(repositoryDescriptor, repository);
        }

        return repository;
    }

    @Override
    public Collection<ExtensionRepository> getRepositories()
    {
        return Collections.unmodifiableCollection(this.repositories);
    }

    @Override
    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        ResolveException lastExtension = null;

        for (ExtensionRepository repository : this.repositories) {
            try {
                return repository.resolve(extensionId);
            } catch (ResolveException e) {
                this.logger.debug("Could not find extension [{}] in repository [{}]", extensionId,
                    repository.getDescriptor(), e);

                lastExtension = e;
            }
        }

        throw new ResolveException(MessageFormat.format("Could not find extension [{0}]", extensionId), lastExtension);
    }

    @Override
    public Extension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        Exception lastExtension = null;

        for (ExtensionRepositoryDescriptor repositoryDescriptor : extensionDependency.getRepositories()) {
            ExtensionRepository repository;
            try {
                repository = getRepository(repositoryDescriptor);
            } catch (ExtensionRepositoryException e) {
                this.logger.warn("Invalid repository [{}] in extension dependency",
                    extensionDependency.getRepositories(), extensionDependency, ExceptionUtils.getRootCauseMessage(e));

                continue;
            }

            try {
                return repository.resolve(extensionDependency);
            } catch (ResolveException e) {
                this.logger.debug("Could not find extension dependency [{}] in repository [{}]", extensionDependency,
                    repository.getDescriptor(), e);

                lastExtension = e;
            }
        }

        for (ExtensionRepository repository : this.repositories) {
            try {
                return repository.resolve(extensionDependency);
            } catch (ResolveException e) {
                this.logger.debug("Could not find extension dependency [{}] in repository [{}]", extensionDependency,
                    repository.getDescriptor(), e);

                lastExtension = e;
            }
        }

        throw new ResolveException(MessageFormat.format("Could not find extension dependency [{0}]",
            extensionDependency), lastExtension);
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        SortedSet<Version> versionSet = new TreeSet<Version>();

        for (ExtensionRepository repository : this.repositories) {
            try {
                IterableResult<Version> versions = repository.resolveVersions(id, 0, -1);

                for (Version version : versions) {
                    versionSet.add(version);
                }
            } catch (ResolveException e) {
                this.logger.debug("Could not find versions for extension with id [{}]", id, e);
            }
        }

        if (versionSet.isEmpty()) {
            throw new ResolveException(MessageFormat.format("Could not find versions for extension with id [{0}]", id));
        }

        return RepositoryUtils.getIterableResult(offset, nb, versionSet);
    }

    @Override
    public IterableResult<Extension> search(String pattern, int offset, int nb)
    {
        ExtensionQuery query = new ExtensionQuery(pattern);

        query.setOffset(offset);
        query.setLimit(nb);

        return search(query);
    }

    @Override
    public IterableResult<Extension> search(ExtensionQuery query)
    {
        IterableResult<? extends Extension> searchResult = null;

        int currentOffset = query.getOffset() > 0 ? query.getOffset() : 0;
        int currentNb = query.getLimit();

        // A local index would avoid things like this...
        for (ExtensionRepository repository : this.repositories) {
            try {
                ExtensionQuery customQuery = query;
                if (currentOffset != customQuery.getOffset() && currentNb != customQuery.getLimit()) {
                    customQuery = new ExtensionQuery(query);
                    customQuery.setOffset(currentOffset);
                    customQuery.setLimit(currentNb);
                }

                searchResult = search(repository, customQuery, searchResult);

                if (searchResult != null) {
                    if (currentOffset > 0) {
                        currentOffset = query.getOffset() - searchResult.getTotalHits();
                        if (currentOffset < 0) {
                            currentOffset = 0;
                        }
                    }

                    if (currentNb > 0) {
                        currentNb = query.getLimit() - searchResult.getSize();
                        if (currentNb < 0) {
                            currentNb = 0;
                        }
                    }
                }
            } catch (SearchException e) {
                this.logger.error("Failed to search on repository [{}] with query [{}]. "
                    + "Ignore and go to next repository.", repository.getDescriptor().toString(), query, e);
            }
        }

        return searchResult != null ? (IterableResult) searchResult : new CollectionIterableResult<Extension>(0,
            query.getOffset(), Collections.<Extension>emptyList());

    }

    /**
     * Search one repository.
     *
     * @param repository the repository to search
     * @param query the search query
     * @param previousSearchResult the current search result merged from all previous repositories
     * @return the updated maximum number of search results to return
     * @throws SearchException error while searching on provided repository
     */
    private IterableResult<? extends Extension> search(ExtensionRepository repository, ExtensionQuery query,
        IterableResult<? extends Extension> previousSearchResult) throws SearchException
    {
        IterableResult<? extends Extension> result;

        if (repository instanceof Searchable) {
            if (repository instanceof AdvancedSearchable) {
                AdvancedSearchable searchableRepository = (AdvancedSearchable) repository;

                result = searchableRepository.search(query);
            } else {
                Searchable searchableRepository = (Searchable) repository;

                result = searchableRepository.search(query.getQuery(), query.getOffset(), query.getLimit());
            }

            if (previousSearchResult != null) {
                result = appendSearchResults(previousSearchResult, result);
            }
        } else {
            result = previousSearchResult;
        }

        return result;
    }

    /**
     * @param previousSearchResult all the previous search results
     * @param result the new search result to append
     * @return the new aggregated search result
     */
    private AggregatedIterableResult<Extension> appendSearchResults(
        IterableResult<? extends Extension> previousSearchResult, IterableResult<? extends Extension> result)
    {
        AggregatedIterableResult<Extension> newResult;

        if (previousSearchResult instanceof AggregatedIterableResult) {
            newResult = ((AggregatedIterableResult<Extension>) previousSearchResult);
        } else {
            newResult = new AggregatedIterableResult<Extension>(previousSearchResult.getOffset());
            newResult.addSearchResult((IterableResult) previousSearchResult);
        }

        newResult.addSearchResult((IterableResult) result);

        return newResult;
    }
}
