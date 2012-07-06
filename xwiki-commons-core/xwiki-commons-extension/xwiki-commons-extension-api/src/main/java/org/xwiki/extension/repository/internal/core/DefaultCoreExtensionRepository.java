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
package org.xwiki.extension.repository.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.internal.RepositoryUtils;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.repository.search.Searchable;
import org.xwiki.extension.version.Version;

/**
 * Default implementation of {@link CoreExtensionRepository}.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultCoreExtensionRepository extends AbstractExtensionRepository implements CoreExtensionRepository,
    Initializable, Searchable
{
    /**
     * The core extensions.
     */
    protected transient Map<String, DefaultCoreExtension> extensions;

    /**
     * The extension associated to the environment.
     */
    protected transient DefaultCoreExtension environmentExtension;

    /**
     * The logger to log.
     */
    @Inject
    private transient Logger logger;

    /**
     * Used to scan jars to find extensions.
     */
    @Inject
    private transient CoreExtensionScanner scanner;

    /**
     * Default constructor.
     */
    public DefaultCoreExtensionRepository()
    {
        super(new ExtensionRepositoryId("core", "xwiki-core", null));
    }

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.extensions = new ConcurrentHashMap<String, DefaultCoreExtension>(this.scanner.loadExtensions(this));

            this.environmentExtension = this.scanner.loadEnvironmentExtensions(this);
            if (this.environmentExtension != null) {
                this.extensions.put(this.environmentExtension.getId().getId(), this.environmentExtension);
            }

            // Start a background thread to get more details about the found extensions
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    scanner.updateExtensions(extensions.values());
                }
            });

            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);
            thread.setName("Core extension repository updater");
            thread.start();
        } catch (Exception e) {
            this.logger.warn("Failed to load core extensions", e);
        }
    }

    // Repository

    @Override
    public CoreExtension resolve(ExtensionId extensionId) throws ResolveException
    {
        CoreExtension extension = getCoreExtension(extensionId.getId());

        if (extension == null
            || (extensionId.getVersion() != null && !extension.getId().getVersion().equals(extensionId.getVersion()))) {
            throw new ResolveException("Could not find extension [" + extensionId + "]");
        }

        return extension;
    }

    @Override
    public CoreExtension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        CoreExtension extension = getCoreExtension(extensionDependency.getId());

        if (extension == null
            || (!extensionDependency.getVersionConstraint().containsVersion(extension.getId().getVersion()))) {
            throw new ResolveException("Could not find extension dependency [" + extensionDependency + "]");
        }

        return extension;
    }

    @Override
    public boolean exists(ExtensionId extensionId)
    {
        Extension extension = getCoreExtension(extensionId.getId());

        if (extension == null
            || (extensionId.getVersion() != null && !extension.getId().getVersion().equals(extensionId.getVersion()))) {
            return false;
        }

        return true;
    }

    @Override
    public boolean exists(String id)
    {
        return this.extensions.containsKey(id);
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        Extension extension = getCoreExtension(id);

        if (extension == null) {
            throw new ResolveException("Could not find extension with id [" + id + "]");
        }

        Collection<Version> versions;
        if (nb == 0 || offset > 0) {
            versions = Collections.emptyList();
        } else {
            versions = Arrays.asList(extension.getId().getVersion());
        }

        return new CollectionIterableResult<Version>(1, offset, versions);
    }

    // CoreExtensionRepository

    @Override
    public CoreExtension getEnvironmentExtension()
    {
        return this.environmentExtension;
    }

    @Override
    public int countExtensions()
    {
        return this.extensions.size();
    }

    @Override
    public Collection<CoreExtension> getCoreExtensions()
    {
        return new ArrayList<CoreExtension>(this.extensions.values());
    }

    @Override
    public CoreExtension getCoreExtension(String id)
    {
        return this.extensions.get(id);
    }

    // Searchable

    @Override
    public IterableResult<Extension> search(String pattern, int offset, int nb) throws SearchException
    {
        return RepositoryUtils.searchInCollection(pattern, offset, nb, this.extensions.values());
    }
}
