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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xwiki.component.annotation.Role;
import org.xwiki.extension.Extension;
import org.xwiki.extension.InvalidExtensionException;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtension;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtensionRepository;

/**
 * Local repository storage serialization tool.
 *
 * @version $Id$
 * @since 6.4M1
 */
@Role
public interface ExtensionSerializer
{
    /**
     * Load an extension descriptor as local extension instance.
     *
     * @param repository the repository
     * @param descriptor the descriptor content
     * @return the {@link Extension} instance
     * @throws InvalidExtensionException error when trying to parse extension descriptor
     */
    DefaultLocalExtension loadLocalExtensionDescriptor(DefaultLocalExtensionRepository repository,
        InputStream descriptor) throws InvalidExtensionException;

    /**
     * Load an extension descriptor as core extension instance.
     *
     * @param repository the repository
     * @param url the core extension {@link URL}
     * @param descriptor the descriptor content
     * @return the {@link Extension} instance
     * @throws InvalidExtensionException error when trying to parse extension descriptor
     */
    DefaultCoreExtension loadCoreExtensionDescriptor(DefaultCoreExtensionRepository repository, URL url,
        InputStream descriptor) throws InvalidExtensionException;

    /**
     * Save local extension descriptor.
     *
     * @param extension the extension to save
     * @param os the stream where to write the serialized version of the extension descriptor
     * @throws ParserConfigurationException error when serializing
     * @throws TransformerException error when serializing
     */
    void saveExtensionDescriptor(Extension extension, OutputStream os) throws ParserConfigurationException,
        TransformerException;
}
