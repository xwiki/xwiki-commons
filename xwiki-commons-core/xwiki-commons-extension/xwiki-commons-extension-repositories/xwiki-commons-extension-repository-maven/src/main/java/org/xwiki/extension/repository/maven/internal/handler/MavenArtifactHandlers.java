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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.handler.ArtifactHandler;

/**
 * A map of {@link ArtifactHandler}s.
 * 
 * @version $Id$
 * @since 16.4.0RC1
 */
public class MavenArtifactHandlers
{
    private Map<String, MavenArtifactHandler> handlersByType = new HashMap<>();

    private Map<String, MavenArtifactHandler> handlersByPackaging = new HashMap<>();

    /**
     * Create an empty {@link MavenArtifactHandlers} instance.
     */
    public MavenArtifactHandlers()
    {
    }

    /**
     * @param handlers the initial handlers
     */
    public MavenArtifactHandlers(MavenArtifactHandlerManager handlers)
    {
        handlers.getHandlers().forEach(this::add);
    }

    /**
     * @param handlers the artifact handlers to add
     */
    public void add(Map<String, ArtifactHandler> handlers)
    {
        for (Map.Entry<String, ArtifactHandler> entry : handlers.entrySet()) {
            MavenArtifactHandler handler = new MavenArtifactHandler(entry.getKey(), entry.getValue());

            add(handler);
        }
    }

    /**
     * @param handler the artifact handler to add
     */
    public void add(MavenArtifactHandler handler)
    {
        this.handlersByType.put(handler.getType(), handler);
        this.handlersByPackaging.put(handler.getPackaging(), handler);
    }

    /**
     * @param type the Maven type
     * @return the {@link ArtifactHandler} instance corresponding to the passed type, or null if none could be found
     */
    public MavenArtifactHandler getByType(String type)
    {
        return this.handlersByType.get(type);
    }

    /**
     * @param packaging the Maven packaging in the pom
     * @return the {@link ArtifactHandler} instance corresponding to the passed packaging, or null if none could be
     *         found
     */
    public MavenArtifactHandler getByPackaging(String packaging)
    {
        return this.handlersByPackaging.get(packaging);
    }

    /**
     * @return the handlers
     */
    public Collection<MavenArtifactHandler> getHandlers()
    {
        return this.handlersByType.values();
    }

}
