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
package org.xwiki.extension.repository.maven.internal.handler;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.eclipse.aether.artifact.ArtifactType;

/**
 * Extends ArtifactHandler to add the type.
 * 
 * @version $Id$
 * @since 16.4.0RC1
 */
public class MavenArtifactHandler implements ArtifactHandler
{
    private String type;

    private ArtifactHandler artifactHandler;

    private ArtifactType artifactType;

    /**
     * @param type the type
     * @param artifactHandler the standard {@link ArtifactHandler} associated with the type
     */
    public MavenArtifactHandler(String type, ArtifactHandler artifactHandler)
    {
        this.type = type;
        this.artifactHandler = artifactHandler;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return this.type;
    }

    @Override
    public String getExtension()
    {
        return this.artifactHandler.getExtension();
    }

    @Override
    public String getDirectory()
    {
        return this.artifactHandler.getDirectory();
    }

    @Override
    public String getClassifier()
    {
        return this.artifactHandler.getClassifier();
    }

    @Override
    public String getPackaging()
    {
        return this.artifactHandler.getPackaging();
    }

    @Override
    public boolean isIncludesDependencies()
    {
        return this.artifactHandler.isIncludesDependencies();
    }

    @Override
    public boolean isAddedToClasspath()
    {
        return this.artifactHandler.isAddedToClasspath();
    }

    @Override
    public String getLanguage()
    {
        return this.artifactHandler.getLanguage();
    }

    /**
     * @return the artifact type
     */
    public ArtifactType getArtifactType()
    {
        if (this.artifactType == null) {
            this.artifactType = RepositoryUtils.newArtifactType(getType(), this);
        }

        return this.artifactType;
    }
}
