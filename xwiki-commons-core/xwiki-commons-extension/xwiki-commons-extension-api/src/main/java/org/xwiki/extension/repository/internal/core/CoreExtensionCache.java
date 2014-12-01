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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

import javax.inject.Inject;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.environment.Environment;
import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.internal.PathUtils;
import org.xwiki.extension.repository.internal.ExtensionSerializer;

/**
 * Store resolve core extension to not have to resolve it again at next restart.
 * 
 * @version $Id$
 * @since 6.4M1
 */
@Component(roles = CoreExtensionCache.class)
public class CoreExtensionCache implements Initializable
{
    @Inject
    private Environment environment;

    @Inject
    private ExtensionSerializer serializer;

    private File folder;

    @Override
    public void initialize() throws InitializationException
    {
        this.folder = new File(this.environment.getTemporaryDirectory(), "cache/extension/core/");
    }

    public void store(CoreExtension extension) throws Exception
    {
        File file = getFile(extension.getURL());

        // Make sure the file parents exist
        if (!file.exists()) {
            file.mkdirs();
        }

        try (FileOutputStream stream = new FileOutputStream(file)) {
            this.serializer.saveExtensionDescriptor(extension, stream);
        }
    }

    public DefaultCoreExtension getExtension(DefaultCoreExtensionRepository repository, URL url) throws Exception
    {
        File file = getFile(url);

        if (!file.exists()) {
            return null;
        }

        try (FileInputStream stream = new FileInputStream(file)) {
            return this.serializer.loadCoreExtensionDescriptor(repository, url, stream);
        }
    }

    public File getFile(URL url)
    {
        String fileName = PathUtils.encode(url.toExternalForm());

        return new File(this.folder, fileName + ".xed");
    }
}
