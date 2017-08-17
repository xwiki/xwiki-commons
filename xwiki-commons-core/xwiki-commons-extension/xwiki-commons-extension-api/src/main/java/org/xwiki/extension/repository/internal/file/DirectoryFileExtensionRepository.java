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
package org.xwiki.extension.repository.internal.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionNotFoundException;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.internal.ExtensionSerializer;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.extension.version.internal.DefaultVersion;

/**
 * @version $Id$
 * @since 9.7RC1
 */
public class DirectoryFileExtensionRepository extends AbstractExtensionRepository
{
    private ExtensionSerializer extensionSerializer;

    private File directory;

    public DirectoryFileExtensionRepository(ExtensionRepositoryDescriptor descriptor, File directory,
        ComponentManager componentManager) throws ComponentLookupException
    {
        super(descriptor);

        this.extensionSerializer = componentManager.getInstance(ExtensionSerializer.class);

        this.directory = directory;
    }

    public File getDirectory()
    {
        return this.directory;
    }

    InputStream getFileAsStream(ExtensionId extensionId, String type)
        throws FileNotFoundException, UnsupportedEncodingException
    {
        return new FileInputStream(getFile(extensionId, type));
    }

    public File getFile(ExtensionId extensionId, String type) throws UnsupportedEncodingException
    {
        File extensionFile = new File(this.directory, getEncodedPath(extensionId, type));

        return extensionFile;
    }

    String getEncodedPath(ExtensionId extensionId, String type) throws UnsupportedEncodingException
    {
        return URLEncoder.encode(getPathSuffix(extensionId, type), "UTF-8");
    }

    String getPathSuffix(ExtensionId extensionId, String type)
    {
        return extensionId.getId() + '-' + extensionId.getVersion().getValue() + '.' + type;
    }

    @Override
    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        InputStream descriptor;
        try {
            descriptor = getFileAsStream(extensionId, "xed");
        } catch (FileNotFoundException e) {
            throw new ExtensionNotFoundException("Could not find extension [" + extensionId + "]", e);
        } catch (Exception e) {
            throw new ResolveException("Invalid extension id [" + extensionId + "]", e);
        }

        if (descriptor == null) {
            throw new ExtensionNotFoundException("Extension [" + extensionId + "] not found");
        }

        try {
            DefaultLocalExtension localExtension =
                this.extensionSerializer.loadLocalExtensionDescriptor(null, descriptor);

            return new FileExtension(this, localExtension);
        } catch (Exception e) {
            throw new ResolveException("Failed to parse descriptor for extension [" + extensionId + "]", e);
        }
    }

    @Override
    public Extension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        VersionConstraint versionConstraint = extensionDependency.getVersionConstraint();

        if (versionConstraint.getVersion() != null) {
            return resolve(new ExtensionId(extensionDependency.getId(),
                new DefaultVersion(extensionDependency.getVersionConstraint().getValue())));
        }

        List<Version> versions = getVersions(extensionDependency.getId());

        if (!versions.isEmpty()) {
            for (ListIterator<Version> it = versions.listIterator(versions.size()); it.hasPrevious(); it.previous()) {
                Version version = it.previous();

                if (versionConstraint.isCompatible(version)) {
                    return resolve(new ExtensionId(extensionDependency.getId(), version));
                }
            }
        }

        throw new ExtensionNotFoundException("Extension dependency [" + extensionDependency + "] not found");
    }

    @Override
    public boolean exists(ExtensionId extensionId)
    {
        try {
            return getFile(extensionId, "xed").exists();
        } catch (Exception e) {
            return false;
        }
    }

    private List<Version> getVersions(String id) throws ResolveException
    {

        List<Version> versions = new LinkedList<Version>();

        try {
            for (File file : this.directory.listFiles(new FilenameFilter()
            {
                @Override
                public boolean accept(File dir, String name)
                {
                    return name.startsWith(id + '-') && name.endsWith(".xed");
                }
            })) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);

                    DefaultLocalExtension localExtension =
                        this.extensionSerializer.loadLocalExtensionDescriptor(null, fis);

                    if (localExtension.getId().getId().equals(id)) {
                        versions.add(localExtension.getId().getVersion());
                    }
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
            }
        } catch (Exception e) {
            throw new ResolveException("Failed to resolve versions for extenion [" + id + "]", e);
        }

        Collections.sort(versions);

        return versions;
    }

    @Override
    public IterableResult<Version> resolveVersions(final String id, int offset, int nb) throws ResolveException
    {
        List<Version> versions = getVersions(id);

        if (versions.isEmpty()) {
            throw new ExtensionNotFoundException("Extension [" + id + "] not found");
        }

        return new CollectionIterableResult<Version>(0, offset, versions);
    }
}
