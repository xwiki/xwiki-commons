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
package org.xwiki.extension.maven.internal;

import java.net.URL;

import org.xwiki.extension.Extension;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtension;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;

/**
 * Extends {@link DefaultCoreExtension} with Maven related informations.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class MavenCoreExtension extends DefaultCoreExtension implements MavenExtension
{
    /**
     * The name of the property containing the artifact id.
     */
    private static final String PKEY_MAVEN_ARTIFACTID = "maven.artifactId";

    /**
     * The name of the property containing the group id.
     */
    private static final String PKEY_MAVEN_GROUPID = "maven.groupId";

    /**
     * @param repository the core extension repository
     * @param url the core extension URL
     * @param extension the extension to copy
     */
    public MavenCoreExtension(DefaultCoreExtensionRepository repository, URL url, Extension extension)
    {
        super(repository, url, extension);
    }

    /**
     * @return the Maven artifact id
     */
    @Override
    public String getMavenArtifactId()
    {
        return (String) getProperty(PKEY_MAVEN_ARTIFACTID);
    }

    /**
     * @return the Maven artifact id
     */
    @Override
    public String getMavenGroupId()
    {
        return (String) getProperty(PKEY_MAVEN_GROUPID);
    }
}
