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
package org.xwiki.extension.repository.internal.local;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionManagerConfiguration;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepositoryException;
import org.xwiki.extension.repository.internal.RepositoryUtils;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.repository.search.Searchable;
import org.xwiki.extension.version.Version;

/**
 * Default implementation of {@link LocalExtensionRepository}.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
// TODO: make it threadsafe bulletproofs
public class DefaultLocalExtensionRepository extends AbstractExtensionRepository implements LocalExtensionRepository,
    Initializable, Searchable
{
    /**
     * Used to get repository path.
     */
    @Inject
    private ExtensionManagerConfiguration configuration;

    /**
     * The logger to log.
     */
    @Inject
    private Logger logger;

    /**
     * The component manager.
     */
    @Inject
    private ComponentManager componentManager;

    /**
     * Used to manipulate filesystem repository storage.
     */
    private ExtensionStorage storage;

    /**
     * The local extensions.
     */
    private Map<ExtensionId, DefaultLocalExtension> extensions =
        new ConcurrentHashMap<ExtensionId, DefaultLocalExtension>();

    /**
     * The local extensions grouped by ids and ordered by version DESC.
     * <p>
     * <extension id, extensions>
     */
    private Map<String, List<DefaultLocalExtension>> extensionsById =
        new ConcurrentHashMap<String, List<DefaultLocalExtension>>();

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.storage = new ExtensionStorage(this, this.configuration.getLocalRepository(), this.componentManager);
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to intialize local extension storage", e);
        }

        setId(new ExtensionRepositoryId("local", "xwiki", this.storage.getRootFolder().toURI()));

        this.storage.loadExtensions();
    }

    /**
     * Register a new local extension.
     * 
     * @param localExtension the new local extension
     */
    protected void addLocalExtension(DefaultLocalExtension localExtension)
    {
        // extensions
        this.extensions.put(localExtension.getId(), localExtension);

        // versions
        List<DefaultLocalExtension> versions = this.extensionsById.get(localExtension.getId().getId());

        if (versions == null) {
            versions = new ArrayList<DefaultLocalExtension>();
            this.extensionsById.put(localExtension.getId().getId(), versions);

            versions.add(localExtension);
        } else {
            int index = 0;
            while (index < versions.size()
                && localExtension.getId().getVersion().compareTo(versions.get(index).getId().getVersion()) < 0) {
                ++index;
            }

            versions.add(index, localExtension);
        }
    }

    // ExtensionRepository

    @Override
    public LocalExtension resolve(ExtensionId extensionId) throws ResolveException
    {
        LocalExtension localExtension = this.extensions.get(extensionId);

        if (localExtension == null) {
            throw new ResolveException("Can't find extension [" + extensionId + "]");
        }

        return localExtension;
    }

    @Override
    public LocalExtension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        List<DefaultLocalExtension> versions = this.extensionsById.get(extensionDependency.getId());

        if (versions != null) {
            for (DefaultLocalExtension extension : versions) {
                if (extensionDependency.getVersionConstraint().containsVersion(extension.getId().getVersion())) {
                    // Return the higher version which satisfy the version constraint
                    return extension;
                }
            }
        }

        throw new ResolveException("Can't find extension dependency [" + extensionDependency + "]");
    }

    @Override
    public boolean exists(ExtensionId extensionId)
    {
        return this.extensions.containsKey(extensionId);
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        List<DefaultLocalExtension> versions = this.extensionsById.get(id);

        if (versions == null) {
            throw new ResolveException("Can't find extension with id [" + id + "]");
        }

        if (nb == 0 || offset >= versions.size()) {
            return new CollectionIterableResult<Version>(versions.size(), offset, Collections.<Version> emptyList());
        }

        int fromId = offset < 0 ? 0 : offset;
        int toId = offset + nb > versions.size() || nb < 0 ? versions.size() - 1 : offset + nb;

        List<Version> result = new ArrayList<Version>(toId - fromId);

        // Invert to sort in ascendent order
        for (int i = toId - 1; i >= fromId; --i) {
            result.add(versions.get(i).getId().getVersion());
        }

        return new CollectionIterableResult<Version>(versions.size(), offset, result);
    }

    // LocalRepository

    @Override
    public Collection<LocalExtension> getLocalExtensions()
    {
        return Collections.<LocalExtension> unmodifiableCollection(this.extensions.values());
    }

    /**
     * Create a new local extension from a remote extension.
     * 
     * @param extension the extension to copy
     * @return the new local extension
     */
    private DefaultLocalExtension createExtension(Extension extension)
    {
        DefaultLocalExtension localExtension = new DefaultLocalExtension(this, extension);

        localExtension.setFile(this.storage.getNewExtensionFile(localExtension.getId(), localExtension.getType()));

        return localExtension;
    }

    @Override
    public int countExtensions()
    {
        return this.extensions.size();
    }

    @Override
    public LocalExtension storeExtension(Extension extension) throws LocalExtensionRepositoryException
    {
        DefaultLocalExtension localExtension = this.extensions.get(extension.getId());

        if (localExtension == null) {
            try {
                localExtension = createExtension(extension);

                InputStream is = extension.getFile().openStream();
                try {
                    FileUtils.copyInputStreamToFile(is, localExtension.getFile().getFile());
                } finally {
                    is.close();
                }
                this.storage.saveDescriptor(localExtension);

                // Cache extension
                addLocalExtension(localExtension);
            } catch (Exception e) {
                // TODO: clean

                throw new LocalExtensionRepositoryException("Failed to save extensoin [" + extension + "] descriptor",
                    e);
            }
        } else {
            throw new LocalExtensionRepositoryException("Extension [" + extension
                + "] already exists in local repository");
        }

        return localExtension;
    }

    @Override
    public void setProperties(LocalExtension localExtension, Map<String, Object> properties)
        throws LocalExtensionRepositoryException
    {
        DefaultLocalExtension extension = this.extensions.get(localExtension.getId());

        if (extension != null) {
            extension.setProperties(properties);
            try {
                this.storage.saveDescriptor(extension);
            } catch (Exception e) {
                throw new LocalExtensionRepositoryException("Failed to save descriptor for extension ["
                    + localExtension + "]", e);
            }
        }
    }

    @Override
    public void removeExtension(LocalExtension extension) throws ResolveException
    {
        DefaultLocalExtension localExtension = (DefaultLocalExtension) resolve(extension.getId());

        try {
            this.storage.removeExtension(localExtension);
        } catch (IOException e) {
            // Should not happen if the local extension exists

            this.logger.error("Failed to remove extension [" + extension + "]", e);
        }
    }

    // Searchable

    @Override
    public IterableResult<Extension> search(String pattern, int offset, int nb) throws SearchException
    {
        Pattern patternMatcher =
            Pattern.compile(RepositoryUtils.SEARCH_PATTERN_SUFFIXNPREFIX + pattern
                + RepositoryUtils.SEARCH_PATTERN_SUFFIXNPREFIX);

        List<Extension> result = new ArrayList<Extension>();

        if (nb != 0 && offset < this.extensionsById.size()) {
            int i = 0;
            for (List<DefaultLocalExtension> versions : this.extensionsById.values()) {
                if (i >= offset) {
                    Extension extension = versions.get(0);

                    if (RepositoryUtils.matches(patternMatcher, extension)) {
                        result.add(extension);
                    }
                }
            }
        }

        return new CollectionIterableResult<Extension>(this.extensionsById.size(), offset, result);
    }
}
