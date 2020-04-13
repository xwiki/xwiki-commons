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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionNotFoundException;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.AdvancedSearchable;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.version.Version;

/**
 * Base class for {@link org.xwiki.extension.repository.ExtensionRepository} implementations maintaining a cache of all
 * extensions.
 *
 * @param <E> the type of the extension
 * @version $Id$
 * @since 5.4M1
 */
public abstract class AbstractCachedExtensionRepository<E extends Extension> extends AbstractExtensionRepository
    implements AdvancedSearchable
{
    /**
     * The cached extensions.
     */
    protected transient Map<ExtensionId, E> extensions = new ConcurrentHashMap<>();

    /**
     * The cached extensions grouped by ids and ordered by version DESC.
     * <p>
     * <extension id, extensions>
     */
    protected Map<String, List<E>> extensionsVersions = new ConcurrentHashMap<>();

    /**
     * Indicate features should be used map key at the same levels than the actual ids.
     */
    private boolean strictId;

    protected AbstractCachedExtensionRepository()
    {
        this(false);
    }

    protected AbstractCachedExtensionRepository(boolean strict)
    {
        this.strictId = strict;
    }

    @Override
    public boolean isFilterable()
    {
        return true;
    }

    @Override
    public boolean isSortable()
    {
        return true;
    }

    /**
     * Register a new extension.
     *
     * @param extension the new extension
     */
    protected void addCachedExtension(E extension)
    {
        if (!this.extensions.containsKey(extension.getId())) {
            // extensions
            this.extensions.put(extension.getId(), extension);

            // versions
            addCachedExtensionVersion(extension.getId().getId(), extension);
            if (!this.strictId) {
                for (String feature : extension.getFeatures()) {
                    addCachedExtensionVersion(feature, extension);
                }
            }
        }
    }

    /**
     * Register extension in all caches.
     *
     * @param feature the feature
     * @param extension the extension
     */
    protected void addCachedExtensionVersion(String feature, E extension)
    {
        // versions
        List<E> versions = this.extensionsVersions.get(feature);

        if (versions == null) {
            versions = new ArrayList<>();
            this.extensionsVersions.put(feature, versions);

            versions.add(extension);
        } else {
            int index = 0;
            while (index < versions.size()
                && extension.getId().getVersion().compareTo(versions.get(index).getId().getVersion()) < 0) {
                ++index;
            }

            versions.add(index, extension);
        }
    }

    /**
     * Remove extension from all caches.
     *
     * @param extension the extension
     */
    protected void removeCachedExtension(E extension)
    {
        // Remove the extension from the memory.
        this.extensions.remove(extension.getId());

        // versions
        removeCachedExtensionVersion(extension.getId().getId(), extension);
        if (!this.strictId) {
            for (String feature : extension.getFeatures()) {
                removeCachedExtensionVersion(feature, extension);
            }
        }
    }

    /**
     * Remove passed extension associated to passed feature from the cache.
     *
     * @param feature the feature associated to the extension
     * @param extension the extension
     */
    protected void removeCachedExtensionVersion(String feature, E extension)
    {
        // versions
        List<E> extensionVersions = this.extensionsVersions.get(feature);
        extensionVersions.remove(extension);
        if (extensionVersions.isEmpty()) {
            this.extensionsVersions.remove(feature);
        }
    }

    // ExtensionRepository

    @Override
    public E resolve(ExtensionId extensionId) throws ResolveException
    {
        E extension = this.extensions.get(extensionId);

        if (extension == null) {
            throw new ExtensionNotFoundException("Can't find extension [" + extensionId + "]");
        }

        return extension;
    }

    @Override
    public E resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        List<E> versions = this.extensionsVersions.get(extensionDependency.getId());

        if (versions != null) {
            for (E extension : versions) {
                if (extensionDependency.getVersionConstraint().containsVersion(extension.getId().getVersion())) {
                    // Return the higher version which satisfy the version constraint
                    return extension;
                }
            }
        }

        throw new ExtensionNotFoundException("Can't find extension dependency [" + extensionDependency + "]");
    }

    @Override
    public boolean exists(ExtensionId extensionId)
    {
        return this.extensions.containsKey(extensionId);
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        if (id == null) {
            return new CollectionIterableResult<>(0, offset, Collections.<Version>emptyList());
        }

        List<E> extensionVersions = this.extensionsVersions.get(id);

        if (extensionVersions == null) {
            throw new ExtensionNotFoundException("Can't find extension with id [" + id + "]");
        }

        if (nb == 0 || offset >= extensionVersions.size()) {
            return new CollectionIterableResult<>(extensionVersions.size(), offset, Collections.<Version>emptyList());
        }

        List<Version> versions = new ArrayList<>(extensionVersions.size());
        for (E extension : extensionVersions) {
            versions.add(extension.getId().getVersion());
        }

        return RepositoryUtils.getIterableResult(offset, nb, versions);
    }

    // Searchable

    @Override
    public IterableResult<Extension> search(String pattern, int offset, int nb) throws SearchException
    {
        ExtensionQuery query = new ExtensionQuery(pattern);

        query.setOffset(offset);
        query.setLimit(nb);

        return search(query);
    }

    @Override
    public IterableResult<Extension> search(ExtensionQuery query)
    {
        Pattern patternMatcher = RepositoryUtils.createPatternMatcher(query.getQuery());

        Set<Extension> set = new HashSet<>();
        List<Extension> result = new ArrayList<>(this.extensionsVersions.size());

        for (List<E> versions : this.extensionsVersions.values()) {
            E extension = versions.get(0);

            if (RepositoryUtils.matches(patternMatcher, query.getFilters(), extension) && !set.contains(extension)) {
                result.add(extension);
                set.add(extension);
            }
        }

        // Sort
        RepositoryUtils.sort(result, query.getSortClauses());

        return RepositoryUtils.getIterableResult(query.getOffset(), query.getLimit(), result);
    }
}
