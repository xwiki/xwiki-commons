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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.repository.search.Searchable;
import org.xwiki.extension.version.Version;

/**
 * Default implementation of {@link InstalledExtensionRepository}.
 * 
 * @version $Id$
 * @since 4.0M2
 */
@Component
@Singleton
// TODO: move all installation related code from local repository to here
public class DefaultInstalledExtensionRepository extends AbstractExtensionRepository implements
    InstalledExtensionRepository, Initializable, Searchable
{
    @Inject
    private LocalExtensionRepository localRepository;

    @Override
    public void initialize() throws InitializationException
    {
        setId(new ExtensionRepositoryId("installed", "installed", this.localRepository.getId().getURI()));
    }

    private InstalledExtension toInstalledExtension(LocalExtension localExtension)
    {
        return localExtension != null ? new DefaultInstalledExtension(localExtension, this) : null;
    }

    private Collection<InstalledExtension> toInstalledExtensions(Collection<LocalExtension> localExtensions)
    {
        List<InstalledExtension> installedExtensions = null;

        if (localExtensions != null) {
            installedExtensions = new ArrayList<InstalledExtension>(localExtensions.size());

            for (LocalExtension localExtension : localExtensions) {
                installedExtensions.add(toInstalledExtension(localExtension));
            }
        }

        return installedExtensions;
    }

    // ExtensionRepository

    @Override
    public InstalledExtension resolve(ExtensionId extensionId) throws ResolveException
    {
        LocalExtension extension = this.localRepository.resolve(extensionId);

        if (!extension.isInstalled()) {
            throw new ResolveException("Extension [" + extensionId + "] is not installed");
        }

        return toInstalledExtension(extension);
    }

    @Override
    public InstalledExtension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        LocalExtension extension = this.localRepository.resolve(extensionDependency);

        if (!extension.isInstalled()) {
            throw new ResolveException("Extension dependency [" + extensionDependency + "] is not installed");
        }

        return toInstalledExtension(extension);
    }

    @Override
    public boolean exists(ExtensionId extensionId)
    {
        try {
            return this.localRepository.exists(extensionId) && this.localRepository.resolve(extensionId).isInstalled();
        } catch (ResolveException e) {
            // Not supposed to happen but anyway lets say it's false
            return false;
        }
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        return this.localRepository.resolveVersions(id, offset, nb);
    }

    // InstalledExtensionRepository

    @Override
    public int countExtensions()
    {
        int count = 0;

        for (LocalExtension extension : this.localRepository.getLocalExtensions()) {
            if (extension.isInstalled()) {
                ++count;
            }
        }

        return count;
    }

    @Override
    public Collection<InstalledExtension> getInstalledExtensions(String namespace)
    {
        Collection<LocalExtension> localExtensions = this.localRepository.getLocalExtensions();

        List<InstalledExtension> installedExtensions = new ArrayList<InstalledExtension>(localExtensions.size());

        for (LocalExtension localExtension : localExtensions) {
            if (localExtension.isInstalled(namespace)) {
                installedExtensions.add(toInstalledExtension(localExtension));
            }
        }

        return installedExtensions;
    }

    @Override
    public Collection<InstalledExtension> getInstalledExtensions()
    {
        Collection<LocalExtension> localExtensions = this.localRepository.getLocalExtensions();

        List<InstalledExtension> installedExtensions = new ArrayList<InstalledExtension>(localExtensions.size());

        for (LocalExtension localExtension : localExtensions) {
            if (localExtension.isInstalled()) {
                installedExtensions.add(toInstalledExtension(localExtension));
            }
        }

        return installedExtensions;
    }

    @Override
    public InstalledExtension getInstalledExtension(String id, String namespace)
    {
        return toInstalledExtension(this.localRepository.getInstalledExtension(id, namespace));
    }

    @Override
    public InstalledExtension installExtension(LocalExtension localExtension, String namespace, boolean dependency)
        throws InstallException
    {
        this.localRepository.installExtension(localExtension, namespace, dependency);

        return toInstalledExtension(localExtension);
    }

    @Override
    public void uninstallExtension(InstalledExtension extension, String namespace) throws UninstallException
    {
        this.localRepository.uninstallExtension(extension, namespace);
    }

    @Override
    public Collection<InstalledExtension> getBackwardDependencies(String id, String namespace) throws ResolveException
    {
        return toInstalledExtensions(this.localRepository.getBackwardDependencies(id, namespace));
    }

    @Override
    public Map<String, Collection<InstalledExtension>> getBackwardDependencies(ExtensionId extensionId)
        throws ResolveException
    {
        Map<String, Collection<LocalExtension>> localExtensions =
            this.localRepository.getBackwardDependencies(extensionId);

        Map<String, Collection<InstalledExtension>> installedExtensions =
            new HashMap<String, Collection<InstalledExtension>>(localExtensions.size());

        for (Map.Entry<String, Collection<LocalExtension>> entry : localExtensions.entrySet()) {
            installedExtensions.put(entry.getKey(), toInstalledExtensions(entry.getValue()));
        }

        return installedExtensions;
    }

    @Override
    public IterableResult<Extension> search(String pattern, int offset, int nb) throws SearchException
    {
        Pattern patternMatcher = Pattern.compile(".*" + pattern + ".*");

        List<Extension> result = new ArrayList<Extension>();

        for (LocalExtension extension : this.localRepository.getLocalExtensions()) {
            if (extension.isInstalled()) {
                // Split the test to avoid exceeding the boolean expression complexity limit.
                boolean matches = patternMatcher.matcher(extension.getId().getId()).matches();

                String name = extension.getName();
                matches = matches || (name != null && patternMatcher.matcher(name).matches());

                String summary = extension.getSummary();
                matches = matches || (summary != null && patternMatcher.matcher(summary).matches());

                String description = extension.getDescription();
                matches = matches || (description != null && patternMatcher.matcher(description).matches());

                matches = matches || patternMatcher.matcher(extension.getFeatures().toString()).matches();

                if (matches) {
                    result.add(toInstalledExtension(extension));
                }
            }
        }

        return new CollectionIterableResult<Extension>(result.size(), offset, result.subList(offset,
            Math.min(result.size(), offset + nb)));
    }
}
