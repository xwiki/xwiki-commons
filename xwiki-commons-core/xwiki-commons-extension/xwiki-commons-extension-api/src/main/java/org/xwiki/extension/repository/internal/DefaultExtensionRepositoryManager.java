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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.xwiki.extension.ExtensionNotFoundException;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryException;
import org.xwiki.extension.repository.ExtensionRepositoryFactory;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.ExtensionRepositorySource;
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
    private final Map<String, ExtensionRepositoryEntry> repositoryMap =
        Collections.synchronizedMap(new LinkedHashMap<>());

    private List<ExtensionRepository> repositories = Collections.emptyList();

    private LRUMap<ExtensionRepositoryDescriptor, ExtensionRepository> repositoriesCache = new LRUMap<>(100);

    private class ExtensionRepositoryEntry implements Comparable<ExtensionRepositoryEntry>
    {
        private ExtensionRepository repository;

        private int priority;

        public ExtensionRepositoryEntry(ExtensionRepository repository, int priority)
        {
            this.repository = repository;
            this.priority = priority;
        }

        @Override
        public int compareTo(ExtensionRepositoryEntry other)
        {
            return this.priority - other.priority;
        }

        public ExtensionRepository getRepository()
        {
            return this.repository;
        }

        @Override
        public String toString()
        {
            return getRepository().toString();
        }
    }

    @Override
    public void initialize() throws InitializationException
    {
        // Load extension repositories
        for (ExtensionRepositorySource repositoriesSource : this.repositoriesSources) {
            for (ExtensionRepositoryDescriptor repositoryDescriptor : repositoriesSource
                .getExtensionRepositoryDescriptors()) {
                try {
                    addRepository(repositoryDescriptor, repositoriesSource.getPriority());
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
        return addRepository(repositoryDescriptor, DEFAULT_PRIORITY);
    }

    @Override
    public ExtensionRepository addRepository(ExtensionRepositoryDescriptor repositoryDescriptor, int priority)
        throws ExtensionRepositoryException
    {
        ExtensionRepository repository;

        try {
            ExtensionRepositoryFactory repositoryFactory =
                this.componentManager.getInstance(ExtensionRepositoryFactory.class, repositoryDescriptor.getType());

            repository = repositoryFactory.createRepository(repositoryDescriptor);

            addRepository(repository, priority);
        } catch (ComponentLookupException e) {
            throw new ExtensionRepositoryException(
                "Unsupported repository type [" + repositoryDescriptor.getType() + "]", e);
        }

        return repository;
    }

    @Override
    public void addRepository(ExtensionRepository repository)
    {
        addRepository(repository, DEFAULT_PRIORITY);
    }

    private void updateRepositories()
    {
        // Get values
        Stream<ExtensionRepositoryEntry> entryStream = this.repositoryMap.values().stream();

        // Sort
        entryStream = entryStream.sorted();

        // Convert to list of ExtensionRepository
        this.repositories = entryStream.map(ExtensionRepositoryEntry::getRepository).collect(Collectors.toList());
    }

    @Override
    public void addRepository(ExtensionRepository repository, int priority)
    {
        // Update the map
        this.repositoryMap.put(repository.getDescriptor().getId(), new ExtensionRepositoryEntry(repository, priority));

        // Update the list
        updateRepositories();
    }

    @Override
    public void removeRepository(String repositoryId)
    {
        // Update the map
        this.repositoryMap.remove(repositoryId);

        // Update the list
        updateRepositories();
    }

    @Override
    public ExtensionRepository getRepository(String repositoryId)
    {
        ExtensionRepositoryEntry entry = this.repositoryMap.get(repositoryId);

        return entry != null ? entry.getRepository() : null;
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
                    repositoryFactory = this.componentManager.getInstance(ExtensionRepositoryFactory.class,
                        repositoryDescriptor.getType());
                } catch (ComponentLookupException e) {
                    throw new ExtensionRepositoryException(
                        "Unsupported extension repository type [{" + repositoryDescriptor.getType() + "}]", e);
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
        return this.repositories;
    }

    @Override
    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        ResolveException lastException = null;

        for (ExtensionRepository repository : this.repositories) {
            try {
                return repository.resolve(extensionId);
            } catch (ExtensionNotFoundException e1) {
                this.logger.debug("Could not find extension [{}] in repository [{}]", extensionId,
                    repository.getDescriptor(), e1);
            } catch (ResolveException e2) {
                this.logger.error("Unexpected error when trying to find extension [{}] in repository [{}]", extensionId,
                    repository.getDescriptor(), e2);

                lastException = e2;
            }
        }

        if (lastException != null) {
            throw new ResolveException(MessageFormat.format("Failed to resolve extension [{0}]", extensionId),
                lastException);
        } else {
            throw new ExtensionNotFoundException(MessageFormat.format("Could not find extension [{0}]", extensionId));
        }
    }

    @Override
    public Extension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        Set<ExtensionRepositoryDescriptor> checkedRepositories = new HashSet<>();

        Exception lastException = null;

        // Try repositories declared in the extension dependency
        for (ExtensionRepositoryDescriptor repositoryDescriptor : extensionDependency.getRepositories()) {
            if (checkedRepositories.contains(repositoryDescriptor)) {
                continue;
            }

            // Remember we tried that repository
            checkedRepositories.add(repositoryDescriptor);

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
            } catch (ExtensionNotFoundException e1) {
                this.logger.debug("Could not find extension dependency [{}] in repository [{}]", extensionDependency,
                    repository.getDescriptor(), e1);
            } catch (ResolveException e2) {
                this.logger.warn("Unexpected error when trying to find extension dependency [{}] in repository [{}]: ",
                    extensionDependency, repository.getDescriptor(), ExceptionUtils.getRootCauseMessage(e2));

                lastException = e2;
            }
        }

        // Try configured repositories
        for (ExtensionRepository repository : this.repositories) {
            if (checkedRepositories.contains(repository.getDescriptor())) {
                continue;
            }

            // Remember we tried that repository
            checkedRepositories.add(repository.getDescriptor());

            try {
                return repository.resolve(extensionDependency);
            } catch (ExtensionNotFoundException e1) {
                this.logger.debug("Could not find extension dependency [{}] in repository [{}]", extensionDependency,
                    repository.getDescriptor(), e1);
            } catch (ResolveException e2) {
                this.logger.error("Unexpected error when trying to find extension dependency [{}] in repository [{}]",
                    extensionDependency, repository.getDescriptor(), e2);

                lastException = e2;
            }
        }

        if (lastException != null) {
            throw new ResolveException(
                MessageFormat.format("Failed to resolve extension dependency [{0}]", extensionDependency),
                lastException);
        } else {
            throw new ExtensionNotFoundException(
                MessageFormat.format("Could not find extension dependency [{0}]", extensionDependency));
        }
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        SortedSet<Version> versionSet = new TreeSet<>();

        for (ExtensionRepository repository : this.repositories) {
            try {
                IterableResult<Version> versions = repository.resolveVersions(id, 0, -1);

                for (Version version : versions) {
                    versionSet.add(version);
                }
            } catch (ExtensionNotFoundException e1) {
                this.logger.debug("Could not find extension with id [{}] in repository [{}]", id,
                    repository.getDescriptor(), e1);
            } catch (ResolveException e2) {
                this.logger.error("Unexpected error when trying to find versions for extension with id [{}]", id, e2);
            }
        }

        if (versionSet.isEmpty()) {
            throw new ExtensionNotFoundException(
                MessageFormat.format("Could not find versions for extension with id [{0}]", id));
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
        IterableResult<Extension> searchResult = null;

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
                this.logger.error(
                    "Failed to search on repository [{}] with query [{}]. " + "Ignore and go to next repository.",
                    repository.getDescriptor().toString(), query, e);
            }
        }

        return searchResult != null ? (IterableResult) searchResult
            : new CollectionIterableResult<Extension>(0, query.getOffset(), Collections.<Extension>emptyList());

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
    private IterableResult<Extension> search(ExtensionRepository repository, ExtensionQuery query,
        IterableResult<Extension> previousSearchResult) throws SearchException
    {
        IterableResult<Extension> result;

        if (repository instanceof Searchable) {
            if (repository instanceof AdvancedSearchable) {
                AdvancedSearchable searchableRepository = (AdvancedSearchable) repository;

                result = searchableRepository.search(query);
            } else {
                Searchable searchableRepository = (Searchable) repository;

                result = searchableRepository.search(query.getQuery(), query.getOffset(), query.getLimit());
            }

            if (previousSearchResult != null) {
                result = RepositoryUtils.appendSearchResults(previousSearchResult, result);
            }
        } else {
            result = previousSearchResult;
        }

        return result;
    }
}
